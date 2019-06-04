/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.inventivetalent.elevator.exception.InconsistentLocationException;
import org.inventivetalent.elevator.exception.MissingFloorException;
import org.inventivetalent.elevator.exception.ObstructedShaftException;

public class ElevatorValidator {

	public static void validateLocation(Elevator elevator) {
		int x = elevator.getX();
		int z = elevator.getZ();

		if (elevator.getAbove() != null) {
			if (elevator.getAbove().getBlockX() != x || elevator.getAbove().getBlockZ() != z) {
				throw new InconsistentLocationException();
			}
		}
		if (elevator.getBelow() != null) {
			if (elevator.getBelow().getBlockX() != x || elevator.getBelow().getBlockZ() != z) {
				throw new InconsistentLocationException();
			}
		}
	}

	public static void validateShaft(Elevator elevator) {
		int start = elevator.getBelow() != null ? elevator.getBelow().getBlockY() : elevator.getCurrent().getBlockY();
		int end = elevator.getAbove() != null ? elevator.getAbove().getBlockY() : elevator.getCurrent().getBlockY();

		for (int i = start; i < end; i++) {
			Location loc = new Location(elevator.getWorld(), elevator.getX(), i, elevator.getZ());
			if (loc.getBlock().getType() != Material.AIR) {
				if (loc.getBlock().getBlockData() instanceof org.bukkit.block.data.type.WallSign) { continue; }
				if ((loc.getBlock().getType() == elevator.getMaterial().getItemType() && loc.getBlock().getData() == elevator.getMaterial().getData())) {
					continue;
				}
				throw new ObstructedShaftException();
			}
		}
	}

	public static void validateFloor(ParsedSign sign, Material material) {
		Block floor = sign.getSign().getLocation().add(0, -2, 0).getBlock();
		if (floor.getType() == Material.AIR || floor.getType() != material) { throw new MissingFloorException(); }
	}

	public static void validateFloor(Floor floor, Material material) {
		Block floorBlock = floor.sign.getSign().getLocation().add(0, -2, 0).getBlock();
		if (floorBlock.getType() == Material.AIR || floorBlock.getType() != material) { throw new MissingFloorException(); }
	}

}
