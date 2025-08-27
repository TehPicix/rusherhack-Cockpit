package me.tehpicix.rusherhack.cockpit;

import org.rusherhack.client.api.feature.hud.TextHudElement;

public class FlightTimeHUD extends TextHudElement {

	public FlightTimeHUD() {
		super("FlightTime");
	}

	@Override
	public String getLabel() {
		return "Flight Time";
	}

	@Override
	public String getText() {
		int elytra = Utils.getTotalElytra();
		if (elytra == 0) return "N/A";
		if (elytra == 1) return Utils.formatDuration(Cockpit.getFlightTime());
		return Utils.formatDuration(Cockpit.getFlightTime()) + " (" + elytra + " Elytra)";
	}
}
