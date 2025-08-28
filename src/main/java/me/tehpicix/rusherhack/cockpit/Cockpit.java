package me.tehpicix.rusherhack.cockpit;

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

	private static double lastX = 0D;
	private static double lastY = 0D;
	private static double lastZ = 0D;
	private static double speed = 0D;

	private static double elytraDurability = 0D;
	private static final int PR = 20;

	/**
     * Starts a new thread that updates the speed variable every tick.
     */
	public static void begin() {
		speed = 0D;
		lastX = 0D;
		lastY = 0D;
		lastZ = 0D;
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

	/**
     * Stops the thread safely.
     */
	public static void end() {
		isRunning = false;
		if (thread != null && thread.isAlive()) thread.interrupt();
	}

	/**
     * Gets the current speed in meters per second.
     * @return
     */
	public static double getSpeed() {
		return speed;
	}

	/**
	 * Gets the current elytra flight time in seconds.
	 * @return
	 */
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
		if (mc.player == null) return;

		// Get the speed
		double deltaX = mc.player.getX() - lastX;
		double deltaY = mc.player.getY() - lastY;
		double deltaZ = mc.player.getZ() - lastZ;
		speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * PR;
		lastX = mc.player.getX();
		lastY = mc.player.getY();
		lastZ = mc.player.getZ();

		// Get the elytra durability
		elytraDurability = 0;

		// Check all inventory slots including armor slots for Elytra
		for (int i = 0; i < 36; i++) {
			ItemStack item = mc.player.getInventory().getItem(i);
			if (item.getItem() != Items.ELYTRA) continue;
			elytraDurability += calculateElytraDurability(item);
		}

		// Check the chest armor slot specifically for Elytra
		ItemStack chestArmor = mc.player.getInventory().getArmor(2);
		if (chestArmor.getItem() == Items.ELYTRA) {
			elytraDurability += calculateElytraDurability(chestArmor);
		}

		ItemStack offhand = mc.player.getOffhandItem();
		if (offhand.getItem() == Items.ELYTRA) {
			elytraDurability += calculateElytraDurability(offhand);
		}
	}
}
