/*
 *
 */

package org.inventivetalent.elevator.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.inventivetalent.elevator.*;
import org.inventivetalent.elevator.exception.ElevatorException;
import org.inventivetalent.elevator.exception.ExceptionHandler;

public class SignListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final Block block = event.getClickedBlock();
		if (block == null) { return; }
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
		if (block.getType() == Material.WALL_SIGN) {
			try {
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();

				if (Elevators.SIGN_TITLE.equals(lines[0])) {
					ParsedSign parsedSign = ParsedSign.get(sign);
					if (parsedSign.disabled) {
						player.sendMessage("§cThis elevator is currently being used.");
						return;
					}

					if (!player.hasPermission("elevator.use")) {
						player.sendMessage("§cYou don't have permission to use elevators");
						return;
					}

					if (Elevators.PRIVATE_ELEVATORS) {
						if (!player.hasMetadata("elevator.use.private")) {
							if (parsedSign.isPrivate()) {
								String name = parsedSign.getOwner();
								if (!player.getName().equals(name)) {
									player.sendMessage("§cYou are not permitted to use this private elevator");
									return;
								}
							}
						}
					}

					if (!player.isSneaking() && player.getLocation().add(0, -1, 0).getBlock().equals(parsedSign.getElevatorFloor().getBlock())) {
						Floor floor = parsedSign.getSelectedFloor();
						if (floor == null) {
							parsedSign.sign.setLine(1, "§8> §7... §8<");
							parsedSign.sign.update();
							player.sendMessage("§cNo floor selected");
							return;
						}

						ElevatorValidator.validateFloor(parsedSign, parsedSign.getElevatorFloor().getBlock().getType());
						ElevatorValidator.validateFloor(floor, parsedSign.getElevatorFloor().getBlock().getType());

						MaterialData elevatorMaterial = new MaterialData(parsedSign.getElevatorFloor().getBlock().getType(), parsedSign.getElevatorFloor().getBlock().getData());
						Elevator elevator = new Elevator(sign.getWorld(), parsedSign.getElevatorFloor().toVector(), elevatorMaterial, 0.2f, floor.block.getLocation().toVector());
						ElevatorRunnable runnable = new ElevatorRunnable(elevator, floor.block.getY() > player.getLocation().getY() ? Direction.UP : Direction.DOWN, player, parsedSign.getCurrentFloor(), floor, parsedSign.floors);
						runnable.start();

					} else { parsedSign.cycleFloor(); }
				}
			} catch (ElevatorException e) {
				ExceptionHandler.handle(player, e);
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (Elevators.RAW_SIGN_TITLE.equals(event.getLine(0))) {
			if (!event.getPlayer().hasPermission("elevator.create")) {
				event.getPlayer().sendMessage("§cNo permission");
				event.setCancelled(true);
				return;
			}
			if (event.getBlock().getLocation().add(0, -2, 0).getBlock().getType() == Material.AIR) {
				event.getPlayer().sendMessage("§cCould not find a valid elevator floor");
				event.setCancelled(true);
				return;
			}
			boolean private_ = false;
			if (event.getLine(2) != null) {
				if (event.getLine(2).equalsIgnoreCase("(private)")) {
					if (!event.getPlayer().hasPermission("elevator.create.private")) {
						event.getPlayer().sendMessage("§cYou don't have permission to create private elevators.");
						event.setCancelled(true);
						return;
					} else {
						private_ = true;
						//Seems like adding text in another color actually creates a new tellraw component which is hidden if it is too long
						event.setLine(3, (event.getLine(3) != null ? event.getLine(3) : "") + "§7:p:" + event.getPlayer().getName());
					}
				}
			}
			event.setLine(0, Elevators.SIGN_TITLE);
			event.getPlayer().sendMessage("§a" + (private_ ? "Private e" : " E") + "levator created");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.WALL_SIGN) {
			ParsedSign.reset(event.getBlock().getLocation());
		}
	}

}
