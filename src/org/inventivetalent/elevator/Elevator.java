/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Elevator {

	@Nonnull private final World world;

	@Nonnull private MaterialData material;
	@Nonnull @Nonnegative private float speed = 0.2f;

	@Nullable private Vector above;
	@Nonnull private  Vector current;
	@Nullable private Vector below;

	public Elevator(World world, Vector current, MaterialData material, float speed) {
		this.world = world;
		this.current = current;
		this.material = material;
		this.speed = speed;
	}

	public Elevator(World world, Vector current, MaterialData material, float speed, Vector above, Vector below) {
		this(world, current, material, speed);
		this.above = above;
		this.below = below;
	}

	public Elevator(World world, Vector current, MaterialData material, float speed, Vector target) {
		this(world, current, material, speed);
		if (target.getY() > current.getY()) { above = target; }
		if (target.getY() < current.getY()) { below = target; }
	}

	@Nonnull
	public World getWorld() {
		return world;
	}

	@Nonnull
	public MaterialData getMaterial() {
		return material;
	}

	@Nonnull
	public float getSpeed() {
		return speed;
	}

	@Nonnull
	public Vector getCurrent() {
		return current.clone();
	}

	public Location getCurrentLocation() {
		return getCurrent().toLocation(getWorld());
	}

	@Nullable
	public Vector getAbove() {
		return above == null ? null : above.clone();
	}

	public Location getAboveLocation() {
		return getAbove() == null ? null : getAbove().toLocation(getWorld());
	}

	@Nullable
	public Vector getBelow() {
		return below == null ? null : below.clone();
	}

	public Location getBelowLocation() {
		return getBelow() == null ? null : getBelow().toLocation(getWorld());
	}

	public int getX() {
		return getCurrent().getBlockX();
	}

	public int getZ() {
		return getCurrent().getBlockZ();
	}

	public void updateRedstoneTriggers(final Block start, Block end, boolean finished) {
		if (!Elevators.TRIGGER_REDSTONE) { return; }
		BlockFace[] faces = new BlockFace[] {
				BlockFace.NORTH,
				BlockFace.NORTH_EAST,
				BlockFace.EAST,
				BlockFace.SOUTH_EAST,
				BlockFace.SOUTH,
				BlockFace.SOUTH_WEST,
				BlockFace.WEST,
				BlockFace.NORTH_WEST };
		for (BlockFace face : faces) {
			final Block startTarget = start.getRelative(face);
			final Block endTarget = end.getRelative(face);

			if (!finished && startTarget.getType() == Material.LEVER) {
				final Lever lever = new Lever(startTarget.getType());
				lever.setPowered(true);
				startTarget.setBlockData( Bukkit.getUnsafe().fromLegacy(lever.getItemType(), lever.getData()) ,true);

				Bukkit.getScheduler().runTaskLater(Elevators.instance, new Runnable() {
					@Override
					public void run() {
						updateSurroundingBlocks(startTarget, 2);
					}
				}, 1);
			}
			if (finished && endTarget.getType() == Material.LEVER) {
				final Lever lever = new Lever(endTarget.getType());
				lever.setPowered(true);
				endTarget.setBlockData( Bukkit.getUnsafe().fromLegacy(lever.getItemType(), lever.getData()) ,true);

				Bukkit.getScheduler().runTaskLater(Elevators.instance, new Runnable() {
					@Override
					public void run() {
						updateSurroundingBlocks(endTarget, 2);
					}
				}, 1);
			}
		}
	}

	void updateSurroundingBlocks(Block block, int next) {
		BlockFace[] faces = new BlockFace[] {
				BlockFace.UP,
				BlockFace.DOWN,
				BlockFace.NORTH,
				BlockFace.EAST,
				BlockFace.SOUTH,
				BlockFace.WEST };
		for (BlockFace face : faces) {
			Block relative = block.getRelative(face);
			if (relative.getType() != Material.AIR) {
				relative.getState().update();
			}
			if (next > 0) { updateSurroundingBlocks(relative, next - 1); }
		}
	}

}
