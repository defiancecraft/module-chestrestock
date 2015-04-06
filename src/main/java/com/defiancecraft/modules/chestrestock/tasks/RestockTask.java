package com.defiancecraft.modules.chestrestock.tasks;

import java.time.Duration;
import java.time.Instant;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.chestrestock.ChestRestock;
import com.defiancecraft.modules.chestrestock.inventory.RestockingInventoryHolder;
import com.defiancecraft.modules.chestrestock.util.RestockCache;

public class RestockTask extends BukkitRunnable {

	private static Instant lastRestock;
	
	public void run() {
		
		// Close open inventories.
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getOpenInventory() != null
				&& p.getOpenInventory().getTopInventory().getHolder() instanceof RestockingInventoryHolder)
				p.closeInventory();
		
		// Clear cache
		RestockCache.clear();
		RestockTask.setLastRestock(Instant.now());
		
	}

	public static Instant getLastRestock() {
		return RestockTask.lastRestock;
	}
	
	public static void setLastRestock(Instant lastRestock) {
		RestockTask.lastRestock = lastRestock;
	}
	
	public static Instant getNextRestock() {
		return getLastRestock().plus(Duration.ofMinutes(ChestRestock.getMainConfig().restockInterval));
	}
	
}
