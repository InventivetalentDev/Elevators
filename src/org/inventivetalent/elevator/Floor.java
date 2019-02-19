/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.block.Block;

public class Floor implements Comparable<Floor> {

	public Block      block;
	public ParsedSign sign;

	public Floor(Block block, ParsedSign sign) {
		this.block = block;
		this.sign = sign;
	}

	@Override
	public int compareTo(Floor o) {
		if (this.block.getLocation().getBlockY() < o.block.getLocation().getBlockY()) { return -1; }
		return 1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		Floor floor = (Floor) o;

		if (block != null ? !block.equals(floor.block) : floor.block != null) { return false; }
		return !(sign != null ? !sign.equals(floor.sign) : floor.sign != null);

	}

	@Override
	public int hashCode() {
		int result = block != null ? block.hashCode() : 0;
		result = 31 * result + (sign != null ? sign.hashCode() : 0);
		return result;
	}
}
