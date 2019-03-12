/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.inventivetalent.elevator.util.EntityUtil;
import org.inventivetalent.elevator.util.Reflection;

import java.util.*;

public class ElevatorRunnable extends BukkitRunnable {

	static Map<UUID, PlayerData> playerDataMap = new HashMap<>();

	private final Elevator  elevator;
	private final Direction direction;

	private final Player passenger;

	private Floor currentFloor;
	private Floor targetFloor;

	private List<Floor> allFloors = new ArrayList<>();

	private List<BlockState> changedWallBlocks = new ArrayList<>();

	public ElevatorRunnable(Elevator elevator, Direction direction, Player passenger, Floor currentFloor, Floor targetFloor, List<Floor> floors) {
		this.elevator = elevator;
		this.direction = direction;
		this.passenger = passenger;
		this.currentFloor = currentFloor;
		this.targetFloor = targetFloor;
		this.allFloors.addAll(floors);

		ElevatorValidator.validateLocation(elevator);
		ElevatorValidator.validateShaft(elevator);
	}

	public void start() {
		playerDataMap.put(passenger.getUniqueId(), new PlayerData(passenger));

		int start = elevator.getBelow() != null ? elevator.getBelow().getBlockY() : elevator.getCurrent().getBlockY();
		int end = elevator.getAbove() != null ? elevator.getAbove().getBlockY() : elevator.getCurrent().getBlockY();

		for (int i = start; i < end; i++) {
			Location loc = new Location(elevator.getWorld(), elevator.getX(), i, elevator.getZ());

			BlockFace[] faces = new BlockFace[] {
					BlockFace.NORTH,
					BlockFace.EAST,
					BlockFace.SOUTH,
					BlockFace.WEST };
			for (BlockFace face : faces) {
				Block relative = loc.getBlock().getRelative(face);
				if (relative.getType() == Material.AIR) {
					changedWallBlocks.add(relative.getState());
					relative.setType(Material.BARRIER);
				}
			}
		}

		for (Floor floor : allFloors) {
			floor.sign.setDisabled(true);
		}

		currentFloor.block.setType(Material.AIR);
		playFoorRemoveSound(currentFloor.block.getLocation());

		targetFloor.block.setType(Material.AIR);
		playFoorRemoveSound(targetFloor.block.getLocation());

		if (direction == Direction.DOWN) {
			for (Floor floor : allFloors) {
				if (floor.block.getY() <= targetFloor.block.getY()) { continue; }
				if (floor.block.getY() < currentFloor.block.getY()) {
					changedWallBlocks.add(floor.block.getState());
					floor.block.setType(Material.AIR);

					playFoorRemoveSound(floor.block.getLocation());
				}
			}
		}
		if (direction == Direction.UP) {
			for (Floor floor : allFloors) {
				if (floor.block.getY() >= targetFloor.block.getY()) { continue; }
				if (floor.block.getY() > currentFloor.block.getY()) {
					changedWallBlocks.add(floor.block.getState());
					floor.block.setType(Material.AIR);

					playFoorRemoveSound(floor.block.getLocation());
				}
			}
		}

		//Center the player, so the falling block appears in the center as well
		passenger.teleport(new Location(passenger.getWorld(), passenger.getLocation().getBlockX() + .5, passenger.getLocation().getBlockY() + .5, passenger.getLocation().getBlockZ() + .5, passenger.getLocation().getYaw(), passenger.getLocation().getPitch()));

		passenger.setMetadata("InElevator", new FixedMetadataValue(Elevators.instance, elevator));
		passenger.setAllowFlight(true);

		if (passenger.getWorld().getDifficulty() != Difficulty.PEACEFUL) { spawnSlimes(); }

		elevator.updateRedstoneTriggers(currentFloor.block, targetFloor.block, false);
		runTaskTimer(Elevators.instance, 1, 1);
	}

	void spawnSlimes() {
		List<Slime> slimes = new ArrayList<>();

		Location loc = new Location(passenger.getWorld(), passenger.getLocation().getBlockX() + .5, passenger.getLocation().getBlockY() + .5, passenger.getLocation().getBlockZ() + .5);

		FallingBlock fallingBlock = passenger.getWorld().spawnFallingBlock(loc, Bukkit.getUnsafe().fromLegacy(elevator.getMaterial().getItemType(), elevator.getMaterial().getData()));
		fallingBlock.setDropItem(false);
		fallingBlock.setMetadata("Elevator", new FixedMetadataValue(Elevators.instance, elevator));

		EntityUtil.setEntitySize(Reflection.getHandle(fallingBlock), 0.001f, 0.001f);

		for (int i = 0; i < 6; i++) {
			Slime slime = passenger.getWorld().spawn(passenger.getLocation(), Slime.class);
			slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 255));
			slime.setSize(-1);
			slime.setMaxHealth(2048D);
			slime.setHealth(2048D);
			slime.setMetadata("Elevator", new FixedMetadataValue(Elevators.instance, elevator));
			if (!slimes.isEmpty()) {
				slimes.get(slimes.size() - 1).setPassenger(slime);
			}
			slimes.add(slime);
		}
		slimes.get(slimes.size() - 1).setPassenger(fallingBlock);
		passenger.setPassenger(slimes.get(0));
	}

	void playFloorResetSound(Location location) {
		location.getWorld().playSound(location, Sound.BLOCK_PISTON_EXTEND, 0.5f, 1f);
	}

	void playFoorRemoveSound(Location location) {
		location.getWorld().playSound(location, Sound.BLOCK_PISTON_CONTRACT, 0.5f, 1f);
	}

	public void done() {
		cancel();

		for (BlockState state : changedWallBlocks) {
			state.update(true);
			if (state.getType() == elevator.getMaterial().getItemType()) {
				playFloorResetSound(state.getLocation());
			}
		}
		changedWallBlocks.clear();

		for (Floor floor : allFloors) {
			floor.sign.setDisabled(false);
		}

		if (passenger.getPassenger() != null) {
			List<Entity> list = new ArrayList<>();
			Entity passenger1 = passenger;
			while ((passenger1 = passenger1.getPassenger()) != null) {
				list.add(passenger1);
			}
			passenger.setPassenger(null);
			for (Entity ent : list) {
				ent.removeMetadata("Elevator", Elevators.instance);
				ent.setPassenger(null);
				ent.eject();
				ent.teleport(new Location(ent.getWorld(), 0, 0, 0));
				if (ent instanceof LivingEntity) {
					((LivingEntity) ent).setHealth(0);
				}
				ent.remove();
			}
		}

		passenger.removeMetadata("InElevator", Elevators.instance);
		passenger.teleport(new Location(passenger.getWorld(), passenger.getLocation().getBlockX() + .5, passenger.getLocation().getY(), passenger.getLocation().getBlockZ() + .5, passenger.getLocation().getYaw(), passenger.getLocation().getPitch()));

		playerDataMap.get(passenger.getUniqueId()).reset(passenger);
		playerDataMap.remove(passenger);
	}

	int soundTimer = 20;

	@Override
	public void run() {
		if (!passenger.isOnline() || passenger.isDead()) {
			done();
			return;
		}

		if ((direction == Direction.UP && elevator.getAbove() != null && passenger.getLocation().getY() < elevator.getAbove().getBlockY() + 1) || (direction == Direction.DOWN && elevator.getBelow() != null && passenger.getLocation().getY() > elevator.getBelow().getBlockY() + 1.25)) {
			passenger.setVelocity(new Vector(0, 0, 0));
			passenger.setVelocity(new Vector(0, elevator.getSpeed() * direction.getValue(), 0));

			soundTimer++;
			if (soundTimer >= 20) { // The minecart sound file is 1 second long
				soundTimer = 0;
				if (Math.abs(passenger.getLocation().getY() - targetFloor.block.getY()) > 5) {
					passenger.getWorld().playSound(passenger.getLocation().add(0, -1, 0), Sound.ENTITY_MINECART_RIDING, 0.1f, 0.1f);
				}
			}
		} else {

			targetFloor.block.setBlockData( Bukkit.getUnsafe().fromLegacy(elevator.getMaterial().getItemType(), elevator.getMaterial().getData()), false);
			currentFloor.block.setBlockData( Bukkit.getUnsafe().fromLegacy(elevator.getMaterial().getItemType(), elevator.getMaterial().getData()), false);

			//Redstone activation
			elevator.updateRedstoneTriggers(currentFloor.block, targetFloor.block, true);

			playFloorResetSound(targetFloor.block.getLocation());
			playFloorResetSound(currentFloor.block.getLocation());

			passenger.setVelocity(new Vector(0, 0, 0));

			done();
		}
		passenger.setFallDistance(0);//Reset the fall-distance to prevent fall damage
	}
}
