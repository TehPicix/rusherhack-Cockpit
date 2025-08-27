package me.tehpicix.rusherhack.cockpit;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Utils {

	private static Minecraft mc = Minecraft.getInstance();

	/**
     * Formats a duration given in total seconds into a human-readable string.
     * @param totalSeconds
     * @return A formatted duration string.
     */
	public static String formatDuration(double totalSeconds) {
		if (totalSeconds == 0) return "0s";

		boolean negative = totalSeconds < 0;
		int s = (int)Math.abs(totalSeconds);

		int days = s / 86_400;
		s %= 86_400;
		int hours = s / 3_600;
		s %= 3_600;
		int minutes = s / 60;
		s %= 60;
		int seconds = s;

		StringBuilder sb = new StringBuilder();
		appendUnit(sb, days, "d");
		appendUnit(sb, hours, "h");
		appendUnit(sb, minutes, "m");
		appendUnit(sb, seconds, "s");

		if (negative) sb.insert(0, "-");
		return sb.toString();
	}

	private static void appendUnit(StringBuilder sb, int value, String suffix) {
		if (value <= 0) return;
		if (sb.length() > 0) sb.append(' ');
		sb.append(value).append(suffix);
	}

	/**
	 * Counts the total number of Elytra items in the player's inventory, including armor and offhand slots.
	 * @return The total count of Elytra items.
	 */
	public static int getTotalElytra() {

		int elytra = 0;

		// Check all inventory slots including armor slots for Elytra
		for (int i = 0; i < 36; i++) {
			ItemStack item = mc.player.getInventory().getItem(i);
			if (item.getItem() != Items.ELYTRA) continue;
			elytra++;
		}

		// Check the chest armor slot specifically for Elytra
		ItemStack chestArmor = mc.player.getInventory().getArmor(2);
		if (chestArmor.getItem() == Items.ELYTRA) elytra++;

		ItemStack offhand = mc.player.getOffhandItem();
		if (offhand.getItem() == Items.ELYTRA) elytra++;

		return elytra;
	}
}
