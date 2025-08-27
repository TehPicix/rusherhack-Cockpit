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
		return Utils.formatDuration(Cockpit.getFlightTime());
	}
}
