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
		if (!mc.player.isFallFlying()) return "Not flying";
		double speed = Cockpit.getSpeed();
		double distance = Cockpit.getSpeed() * Cockpit.getFlightTime();
		distance = distance / 1000D;
		return String.format("%.1f km (%.1f km/h)", distance, speed * 3.6);
	}
}