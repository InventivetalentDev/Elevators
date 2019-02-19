/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.inventivetalent.elevator.exception.InvalidSignException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * [Elevator]
 * > selector <
 * - direction -
 * name
 */
public class ParsedSign {

	private static Map<Location, ParsedSign> signMap = new HashMap<>();

	public Sign   sign;
	public String name;

	public String options;

	String selectedFloorName  = "";
	int    selectedFloorIndex = -1;

	public List<Floor> floors = new ArrayList<>();

	boolean finderTimeout = false;

	public boolean disabled = false;

	private ParsedSign(Sign sign) {
		this.sign = sign;
		if (!Elevators.SIGN_TITLE.equals(sign.getLine(0))) {
			throw new InvalidSignException("wrong sign title");
		}
		if (sign.getLine(3) == null) {
			throw new InvalidSignException("missing name");
		}
		this.name = sign.getLine(3);
		if (name.contains("§7:")) {
			String[] split = name.split("§7:");
			name = split[0];
			options = split[1];
		}

	}

	public void cycleFloor() {
		if (!finderTimeout) {
			finderTimeout = true;
			this.floors = new FloorFinder(this).getFloors();
			Bukkit.getScheduler().runTaskLater(Elevators.instance, new Runnable() {
				@Override
				public void run() {
					finderTimeout = false;
				}
			}, 40);
		}
		if (floors.isEmpty()) { return; }

		selectedFloorIndex++;
		if (selectedFloorIndex >= floors.size()) {
			selectedFloorIndex = 0;
		}

		Floor selectedFloor = floors.get(selectedFloorIndex);

		selectedFloorName = selectedFloor.sign.name;

		Sign sign = getSign();
		sign.setLine(1, "§8> §r" + selectedFloorName.substring(0, Math.min(selectedFloorName.length(), 10)) + " §8<");

		String directionString = " --- ";
		if (selectedFloor.block.getY() < getElevatorFloor().getY()) { directionString = " vvv "; }
		if (selectedFloor.block.getY() > getElevatorFloor().getY()) { directionString = " ^^^ "; }
		sign.setLine(2, "§e" + directionString);

		sign.update();
	}

	void setDisabled(boolean b) {
		Sign sign = getSign();

		this.disabled = b;
		String line = sign.getLine(2);
		if (line != null) {
			line = line.substring(Math.min(line.length(), 2));
		} else {
			line = " --- ";
		}
		line = (disabled ? "§c" : "§e") + line;
		sign.setLine(2, line);
		sign.update();
	}

	public Sign getSign() {
		return (Sign) sign.getLocation().getBlock().getState();
	}

	public Location getElevatorFloor() {
		return sign.getLocation().clone().add(0, -2, 0);
	}

	public Floor getSelectedFloor() {
		if (selectedFloorIndex == -1) { return null; }
		return floors.get(selectedFloorIndex);
	}

	public Floor getCurrentFloor() {
		for (Floor floor : floors) {
			if (floor.sign.equals(this) || floor.sign.name.equals(this.name)) {
				return floor;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public String getOptions() {
		return options;
	}

	public boolean isPrivate() {
		return getOptions() != null && getOptions().contains("p:");
	}

	public String getOwner() {
		String[] s1 = getOptions().split("p:");
		if (s1[1].contains(":")) {
			s1[1] = s1[1].split(":")[0];
		}
		return s1[1];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		ParsedSign that = (ParsedSign) o;

		if (selectedFloorIndex != that.selectedFloorIndex) { return false; }
		if (finderTimeout != that.finderTimeout) { return false; }
		if (sign != null ? !sign.equals(that.sign) : that.sign != null) { return false; }
		if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
		if (selectedFloorName != null ? !selectedFloorName.equals(that.selectedFloorName) : that.selectedFloorName != null) { return false; }
		return !(floors != null ? !floors.equals(that.floors) : that.floors != null);

	}

	@Override
	public int hashCode() {
		int result = sign != null ? sign.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (selectedFloorName != null ? selectedFloorName.hashCode() : 0);
		result = 31 * result + selectedFloorIndex;
		result = 31 * result + (floors != null ? floors.hashCode() : 0);
		result = 31 * result + (finderTimeout ? 1 : 0);
		return result;
	}

	public static ParsedSign get(Sign sign) {
		if (!signMap.containsKey(sign.getLocation())) {
			signMap.put(sign.getLocation(), new ParsedSign(sign));
		}
		return signMap.get(sign.getLocation());
	}

	public static void reset(Location location) {
		signMap.remove(location);
	}

}
