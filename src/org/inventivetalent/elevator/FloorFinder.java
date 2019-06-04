/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloorFinder {

	Location startLocation;

	private List<Floor> floors = new ArrayList<>();

	int count = 0;

	public FloorFinder(Location start) {
		this.startLocation = start;

		for (int i = startLocation.getBlockY(); i > 0; i--) {
			Location location = new Location(start.getWorld(), start.getX(), i, start.getZ());
			checkBlock(location.getBlock());
			if (count > 2) { break; }
		}
		count = 0;
		for (int i = startLocation.getBlockY(); i < 255; i++) {
			Location location = new Location(start.getWorld(), start.getX(), i, start.getZ());
			checkBlock(location.getBlock());
			if (count > 2) { break; }
		}

		Collections.sort(floors);
	}

	public List<Floor> getFloors() {
		return floors;
	}

	void checkBlock(Block block) {
		if (block.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
			Sign currentSign = (Sign) block.getState();
			Block currentBlock = block.getLocation().add(0, -2, 0).getBlock();

			ParsedSign parsed = ParsedSign.get(currentSign);

			Floor floor = new Floor(currentBlock, parsed);
			if (!floors.contains(floor)) { floors.add(floor); }
		} else if (block.getType() != Material.AIR) {
			count++;
		} else { count = 0; }
	}

	public FloorFinder(ParsedSign sign) {
		this(sign.sign.getLocation());
	}

	public FloorFinder(Elevator elevator) {
		this(elevator.getCurrentLocation());
	}

}
