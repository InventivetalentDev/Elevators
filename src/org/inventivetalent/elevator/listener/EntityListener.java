/*
 *
 */

package org.inventivetalent.elevator.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.inventivetalent.elevator.Elevators;

public class EntityListener implements Listener {

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			FallingBlock fallingBlock = (FallingBlock) event.getEntity();
			if (fallingBlock.hasMetadata("Elevator")) {
				event.setCancelled(true);
				fallingBlock.remove();
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!Elevators.DISBLE_DAMAGE) { return; }
		if (event.getEntityType() == EntityType.PLAYER) {
			if (event.getEntity().hasMetadata("InElevator")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().hasMetadata("Elevator")) {
			event.getEntity().setHealth(event.getEntity().getHealth());
		}
	}

}
