package me.tehpicix.rusherhack.cockpit;

public class Utils {

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
}
