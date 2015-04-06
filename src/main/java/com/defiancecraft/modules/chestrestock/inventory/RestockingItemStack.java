package com.defiancecraft.modules.chestrestock.inventory;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.defiancecraft.modules.chestrestock.ChestRestock;
import com.defiancecraft.modules.chestrestock.config.MainConfig;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig.InventoryItem;

public class RestockingItemStack extends ItemStack {

	// InventoryItem which this represents
	private InventoryItem item;
	
	// Old metadata when allowed is set to false
	private ItemMeta oldMeta;
	
	// Whether player may take the item (if they have permission for the inventory)
	private boolean allowed = true;
	
	public RestockingItemStack(InventoryItem item) {
		super(item.toItemStack());
		this.item = item;
		
		if (this.item.type.equalsIgnoreCase(ChestRestock.getMainConfig().tokenType)
				&& this.item.damage == ChestRestock.getMainConfig().tokenDamage) {
			this.setupTokenItem();
		}
		
	}
	
	/**
	 * Gets the UUID of this itme
	 * 
	 * @return UUID
	 */
	public UUID getUniqueId() {
		return item.id;
	}
	
	/**
	 * Checks whether the player is allowed to take this item
	 * 
	 * @return Whether the player may take this item
	 */
	public boolean isAllowed() {
		return this.allowed;
	}
	
	/**
	 * Sets whether the player may take this item
	 * 
	 * @param allowed Whether the player may take this item
	 * @param deniedMsg Message shown to player when not allowed to take (can be null if allowed is true) 
	 * @param deniedLore Lore on ItemStack to show when not allowed to take (can be null if allowed is true)
	 */
	public void setAllowed(boolean allowed, String deniedMsg, List<String> deniedLore) {
		
		if (this.isAllowed() && !allowed) {
			
			this.oldMeta = this.getItemMeta().clone();
			this.allowed = false;
			
			// Change lore to show denied message
			ItemMeta meta = this.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', deniedMsg));
			meta.setLore(deniedLore);
			this.setItemMeta(meta);
			
		} else if (!this.isAllowed() && allowed) {
			
			// Restore previous meta
			this.setItemMeta(oldMeta.clone());
			this.allowed = true;
			
		}
		
	}
	
	private void setupTokenItem() {
		
		ItemMeta meta = this.getItemMeta();
		MainConfig config = ChestRestock.getMainConfig();
		
		// Set display name
		if (config.tokenName != null
				&& !config.tokenName.isEmpty())
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.tokenName));
		
		// Set lore
		if (config.tokenLore != null
				&& config.tokenLore.size() > 0)
			meta.setLore(config.tokenLore);
		
		this.setItemMeta(meta);
		
	}
	
}
