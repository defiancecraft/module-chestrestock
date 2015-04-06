package com.defiancecraft.modules.chestrestock.inventory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.modules.chestrestock.ChestRestock;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig.InventoryConfig;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig.InventoryItem;
import com.defiancecraft.modules.chestrestock.tasks.RestockTask;
import com.defiancecraft.modules.chestrestock.util.RestockCache;

public class RestockingInventoryHolder implements InventoryHolder {

	private Inventory inventory;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	
	public RestockingInventoryHolder(Player owner) {
		
		String title = ChestRestock.getMainConfig().inventoryTitle;
		Duration dur = Duration.between(Instant.now(), RestockTask.getNextRestock());
		
		// Format title
		title = ChatColor.translateAlternateColorCodes('&', title);
		title = title.replace("{time}", (dur.toMinutes() < 1 ? dur.getSeconds() + "s" : dur.toMinutes() + "m"));
		title = title.length() > 32 ? title.substring(0, 32) : title;
		
		this.inventory = Bukkit.createInventory(this, getInventorySize(), title);
		this.reloadItems(owner);
		
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public List<ItemStack> getItems() {
		return items;
	}
	
	public int getInventorySize() {
		return ChestRestock.getMainConfig().inventoryRows * 9;
	}
	
	public void reloadItems(Player owner) {
		
		inventory.clear();
		
		InventoriesConfig config = ChestRestock.getInventoriesConfig();
		for (InventoryConfig inv : config.inventories) {
			for (InventoryItem item : inv.items) {

				// If they've already took the item, show air.
				// Otherwise, add it.
				if (RestockCache.isTaken(owner.getUniqueId(), item.id))
					items.add(null);
				else {
					RestockingItemStack is = new RestockingItemStack(item);
					
					if (!owner.hasPermission(inv.permission))
						is.setAllowed(false, inv.upgradeText, inv.upgradeLore);
					
					items.add(is);
				}
				
			}
		}
		
		inventory.setContents(items.toArray(new ItemStack[]{}));
		
	}

}
