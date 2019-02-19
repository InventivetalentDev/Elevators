/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public enum Direction {

	UP(1),
	DOWN(-1);

	int value;

	Direction(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public Vector getTarget(Elevator elevator) {
		if (this == UP) {
			return elevator.getAbove();
		}
		if (this == DOWN) {
			return elevator.getBelow();
		}
		return elevator.getCurrent();
	}

	public Location getTargetLocation(Elevator elevator) {
		return getTarget(elevator).toLocation(elevator.getWorld());
	}

}
