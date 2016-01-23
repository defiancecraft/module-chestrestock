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
import com.defiancecraft.modules.chestrestock.listeners.InventoryListener;
import com.defiancecraft.modules.chestrestock.tasks.RestockTask;
import com.defiancecraft.modules.chestrestock.util.RestockCache;
import com.defiancecraft.modules.chestrestock.util.RestockInProgressException;

public class RestockingInventoryHolder implements InventoryHolder {

	/**
	 * This is used as a hash of sorts to compare against the current state.
	 * If the last restock according to this inventory and the last restock
	 * according to the RestockTask differ, the user's inventory should be
	 * closed. In the case that it was not, this is used to prevent them taking
	 * items anyway.
	 * 
	 * @see InventoryListener#onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent)
	 */
	private final Instant lastRestock;
	
	private Inventory inventory;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	
	public RestockingInventoryHolder(Player owner) throws RestockInProgressException {
		
		String title = ChestRestock.getMainConfig().inventoryTitle;
		Duration dur = Duration.between(Instant.now(), RestockTask.getNextRestock());
		
		if (dur.isNegative())
			throw new RestockInProgressException();
		
		// Format title
		title = ChatColor.translateAlternateColorCodes('&', title);
		title = title.replace("{time}", (dur.toMinutes() < 1 ? dur.getSeconds() + "s" : dur.toMinutes() + "m"));
		title = title.length() > 32 ? title.substring(0, 32) : title;
		
		this.lastRestock = RestockTask.getLastRestock();
		this.inventory = Bukkit.createInventory(this, getInventorySize(), title);
		this.reloadItems(owner);
		
	}
	
	public Instant getLastRestock() {
		return lastRestock;
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
