package me.tehpicix.rusherhack.cockpit;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class Cockpit {

	private static Minecraft mc = Minecraft.getInstance();

	private static Thread thread;
	private static volatile boolean isRunning = false;

	private static double lastX = 0D, lastY = 0D, lastZ = 0D;
	private static boolean hasAnchor = false;

	private static double speed = 0D; // blocks/sec (avg over last ~1s)
	private static double elytraDurability = 0D;

	private static final int PR = 20;   // samples per second (thread sleep target)
	private static final int SR = 1000; // (unused by you; keeping as-is)

	// ===== New: robust 1s rolling window =====
	private static final long WINDOW_NS = 1_000_000_000L; // 1 second
	private static final long STALE_NS = 300_000_000L;    // if a single dt > 300ms, reset window (teleport/lag)
	private static final double MAX_SEG_DIST = 200.0;     // clamp insane segment distances (teleport safeguard)

	private static long lastT = 0L;
	private static final Deque<Sample> window = new ArrayDeque<>();
	private static double sumDist = 0D; // total path length inside window (blocks)
	private static long sumDtNs = 0L;   // total time inside window (ns)

	private static final class Sample {
		final double dist; // segment distance in blocks
		final long dtNs;   // segment time in ns

		Sample(double dist, long dtNs) {
			this.dist = dist;
			this.dtNs = dtNs;
		}
	}

	/** Starts a new thread that updates the speed variable every tick. */
	public static void begin() {
		speed = 0D;
		lastX = lastY = lastZ = 0D;
		hasAnchor = false;

		// reset window
		window.clear();
		sumDist = 0D;
		sumDtNs = 0L;
		lastT = 0L;

		isRunning = true;
		thread = new Thread(() -> {
			try {
				while (isRunning) {
					tick();
					Thread.sleep(1000 / PR);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				isRunning = false;
			}
		});
		thread.start();
	}

	/** Stops the thread safely. */
	public static void end() {
		isRunning = false;
		if (thread != null && thread.isAlive()) thread.interrupt();
	}

	/** Gets the current speed in meters/blocks per second (averaged over last ~1s). */
	public static double getSpeed() {
		return speed;
	}

	/** Gets the current elytra flight time in seconds. */
	public static double getFlightTime() {
		return elytraDurability;
	}

	private static int calculateElytraDurability(ItemStack item) {
		if (item.isEmpty()) return 0;
		Registry<Enchantment> enchantmentRegistry = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> unbreakingEnchantment = enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING);
		int unbreakingLevel = item.getEnchantments().getLevel(unbreakingEnchantment);
		return ((item.getMaxDamage() - item.getDamageValue()) * (unbreakingLevel + 1)) - 1;
	}

	private static void tick() {
		if (mc.player == null || mc.level == null) {
			resetWindow();
			return;
		}

		long now = System.nanoTime();

		// Prime anchor to avoid bogus first delta
		if (!hasAnchor) {
			lastX = mc.player.getX();
			lastY = mc.player.getY();
			lastZ = mc.player.getZ();
			lastT = now;
			hasAnchor = true;
			speed = 0D;
			return;
		}

		long dtNs = now - lastT;
		if (dtNs <= 0) dtNs = 1; // guard

		// If dt is huge (lag/teleport), reset window to avoid spikes
		if (dtNs > STALE_NS) {
			resetWindow();
			lastX = mc.player.getX();
			lastY = mc.player.getY();
			lastZ = mc.player.getZ();
			lastT = now;
			speed = 0D;
			return;
		}

		// Segment distance (3D). If you want horizontal-only, drop Y from this calc.
		double dx = mc.player.getX() - lastX;
		double dz = mc.player.getZ() - lastZ;
		double segDist = Math.sqrt(dx * dx + dz * dz);
		if (segDist > MAX_SEG_DIST) segDist = 0; // discard insane jump

		// Advance anchor
		lastX = mc.player.getX();
		lastY = mc.player.getY();
		lastZ = mc.player.getZ();
		lastT = now;

		// Push new sample
		window.addLast(new Sample(segDist, dtNs));
		sumDist += segDist;
		sumDtNs += dtNs;

		// Trim to last 1s
		while (sumDtNs > WINDOW_NS && !window.isEmpty()) {
			Sample old = window.removeFirst();
			sumDist -= old.dist;
			sumDtNs -= old.dtNs;
		}

		// Compute average speed over effective window
		if (sumDtNs > 0L) {
			double seconds = sumDtNs / 1_000_000_000.0;
			speed = sumDist / seconds; // blocks per second
		} else {
			speed = 0D;
		}

		// Elytra durability (unchanged)
		elytraDurability = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack item = mc.player.getInventory().getItem(i);
			if (item.getItem() == Items.ELYTRA) {
				elytraDurability += calculateElytraDurability(item);
			}
		}
		ItemStack chestArmor = mc.player.getInventory().getArmor(2);
		if (chestArmor.getItem() == Items.ELYTRA) {
			elytraDurability += calculateElytraDurability(chestArmor);
		}
		ItemStack offhand = mc.player.getOffhandItem();
		if (offhand.getItem() == Items.ELYTRA) {
			elytraDurability += calculateElytraDurability(offhand);
		}
	}

	private static void resetWindow() {
		window.clear();
		sumDist = 0D;
		sumDtNs = 0L;
		hasAnchor = false;
		lastT = 0L;
	}
}
