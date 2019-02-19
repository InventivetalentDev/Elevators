/*
 *
 */

package org.inventivetalent.elevator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.elevator.listener.EntityListener;
import org.inventivetalent.elevator.listener.SignListener;
import org.mcstats.MetricsLite;

public class Elevators extends JavaPlugin {

	public static Elevators instance;

	public static final String  RAW_SIGN_TITLE    = "[Elevator]";
	public static       String  SIGN_TITLE        = "§6[§5Elevator§6]";
	public static       boolean PRIVATE_ELEVATORS = true;
	public static       boolean TRIGGER_REDSTONE  = true;
	public static       boolean DISBLE_DAMAGE     = true;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		SIGN_TITLE = org.bukkit.ChatColor.translateAlternateColorCodes('&', getConfig().getString("signTitle"));
		PRIVATE_ELEVATORS = getConfig().getBoolean("privateElevators");
		TRIGGER_REDSTONE = getConfig().getBoolean("triggerRedstone", false);
		DISBLE_DAMAGE = getConfig().getBoolean("disableDamage", false);

		Bukkit.getPluginManager().registerEvents(new SignListener(), this);
		Bukkit.getPluginManager().registerEvents(new EntityListener(), this);

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}
		} catch (Exception e) {
		}
	}
}
