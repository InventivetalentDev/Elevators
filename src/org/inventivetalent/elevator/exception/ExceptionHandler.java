/*
 *
 */

package org.inventivetalent.elevator.exception;

import org.bukkit.entity.Player;
import org.inventivetalent.elevator.Elevators;

public class ExceptionHandler {

	static boolean DEBUG = false;

	public static void handle(Player player, ElevatorException exception) {
		if (DEBUG) {
			exception.printStackTrace();
		}
		if (exception instanceof ObstructedShaftException) {
			player.sendMessage("§cThe elevator shaft is obstructed by a block, or one of the floors has another elevator material");
			return;
		}
		if (exception instanceof InvalidSignException) {
			player.sendMessage("§cThis sign is invalid: " + exception.getMessage());
			return;
		}
		if (exception instanceof InconsistentLocationException) { //This *should* never happen
			player.sendMessage("§cThe elevator floors are somehow not correctly aligned");
			return;
		}
		if (exception instanceof MissingFloorException) {
			player.sendMessage("§cOne of the floors does not have a valid floor block");
			return;
		}

		Elevators.instance.getLogger().warning("Unhandled elevator exception");
		exception.printStackTrace();
	}

}
