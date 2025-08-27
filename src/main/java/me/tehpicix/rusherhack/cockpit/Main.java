package me.tehpicix.rusherhack.cockpit;

import java.util.List;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class Main extends Plugin {

	@Override
	public void onLoad() {
		Cockpit.begin();
		List
		    .of(
		        new FlightTimeHUD(),
		        new FlightDistanceHUD())
		    .forEach(RusherHackAPI.getHudManager()::registerFeature);
	}

	@Override
	public void onUnload() {
		Cockpit.end();
	}
}