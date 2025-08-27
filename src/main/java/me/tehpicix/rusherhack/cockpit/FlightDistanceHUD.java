package me.tehpicix.rusherhack.cockpit;

import org.rusherhack.client.api.feature.hud.TextHudElement;

public class FlightDistanceHUD extends TextHudElement {

	public FlightDistanceHUD() {
		super("FlightDistance");
	}

	@Override
	public String getLabel() {
		return "Flight Distance";
	}

	@Override
	public String getText() {

		if (!mc.player.isFallFlying()) {
			return "N/A";
		}

		// estimated distance in meters
		double speed = Cockpit.getSpeed(); // in m/s
		double distance = Cockpit.getSpeed() * Cockpit.getFlightTime();
		distance = distance / 1000D; // convert to km

		return String.format("%.1f km (%.1f km/h)", distance, speed * 3.6);
	}
}