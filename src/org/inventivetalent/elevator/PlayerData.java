/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerData {

	boolean allowFlight = false;
	boolean flying      = false;
	float   walkSpeed   = 0;
	float   flySpeed    = 0;

	Entity passenger;
	Entity vehicle;

	public PlayerData(Player player) {
		this.allowFlight = player.getAllowFlight();
		this.flying = player.isFlying();
		this.walkSpeed = player.getWalkSpeed();
		this.flySpeed = player.getFlySpeed();

		this.passenger = player.getPassenger();
		this.vehicle = player.getVehicle();
	}

	public void reset(Player player) {
		player.setAllowFlight(allowFlight);
		player.setFlying(flying);
		player.setWalkSpeed(walkSpeed);
		player.setFlySpeed(flySpeed);

		player.setPassenger(passenger);
		if (vehicle != null) { vehicle.setPassenger(player); }
	}

}
