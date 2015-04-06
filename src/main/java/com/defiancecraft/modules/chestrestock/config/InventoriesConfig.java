package com.defiancecraft.modules.chestrestock.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import com.defiancecraft.core.util.JsonConfig;

/**
 * Class representing the primary configuration in which all
 * inventories for restock chests are defined.
 */
public class InventoriesConfig extends JsonConfig {

	public List<InventoryConfig> inventories = new ArrayList<InventoryConfig>(Arrays.asList( // Sample config
			new InventoryConfig(
				"restockchest.example",
				new ArrayList<InventoryItem>(Arrays.asList(
						new InventoryItem("DIAMOND_SWORD", 1, (short)0, (byte)0, Arrays.asList("DAMAGE_ALL:3"), "&aSuper OP Apple!", Arrays.asList("this", "is", "lore"))
				))
			)
	));
	
	public List<BlockConfig> chests = new ArrayList<BlockConfig>();
	
	/**
	 * Checks if a chest is a restocking chest.
	 * @param b Block to check
	 * @return Whether it is a restocking chest.
	 */
	public boolean isRestockChest(Block b) {
		return chests.stream().anyMatch((c) -> BlockConfig.equals(b, c));
	}
	
	/**
	 * Class representing the configuration of a single inventory,
	 * which will be added to the restocking chest.
	 */
	public static class InventoryConfig {
		
		public String permission = "restockchest.undefined";
		public String upgradeText = "&aPlease enter a message for upgrade text!";
		public List<String> upgradeLore = new ArrayList<String>();
		public List<InventoryItem> items;
		
		public InventoryConfig() {}
		
		/**
		 * Internal constructor for initializing the configuration.
		 * 
		 * @param permission Permission node for this inventory.
		 * @param items List of items contained within this inventory.
		 */
		InventoryConfig(String permission, List<InventoryItem> items) {
			this.permission = permission;
			this.items = items;
		}
		
	}
	
	/**
	 * Class representing the configuration of a single item
	 * in an inventory.
	 */
	public static class InventoryItem {
		
		public String type = "AIR";
		public int amount = 1;
		public Short damage;
		public Byte data;
		public List<String> enchantments;
		public String displayName;
		public List<String> lore;
		public UUID id = UUID.randomUUID();
		
		/**
		 * Internal constructor for initializing the configuration.
		 * 
		 * @param type Material of item
		 * @param amount Amount of item
		 * @param damage Damage value
		 * @param data MaterialData value
		 * @param enchantments List of enchantments in format: <name>:<level> (name must be uppercase from org.bukkit.Material)
		 * @param displayName Display name
		 * @param lore Item lore
		 */
		InventoryItem(String type, int amount, short damage, Byte data, List<String> enchantments, String displayName, List<String> lore) {
			this.type = type;
			this.amount = amount;
			this.damage = damage;
			this.data = data;
			this.enchantments = enchantments;
			this.displayName = displayName;
			this.lore = lore;
		}
		
		/**
		 * Constructs an InventoryItem from an ItemStack
		 * @param stack ItemStack to use
		 */
		@SuppressWarnings("deprecation")
		public InventoryItem(ItemStack stack) {
			
			this.type = stack.getType().toString();
			this.amount = stack.getAmount();
			this.damage = stack.getDurability();
			
			// Add data
			if (stack.getData() != null)
				this.data = stack.getData().getData();

			// Add enchantments
			if (stack.getEnchantments().size() > 0) {
				this.enchantments = new ArrayList<String>();
				
				for (Entry<Enchantment, Integer> enchantment : stack.getEnchantments().entrySet())
					this.enchantments.add(enchantment.getKey().getName() + ":" + enchantment.getValue());
			}
			
			// Add display name
			if (stack.hasItemMeta()
					&& stack.getItemMeta().getDisplayName() != null
					&& !stack.getItemMeta().getDisplayName().isEmpty())
				this.displayName = stack.getItemMeta().getDisplayName();
			
			// Add lore
			if (stack.hasItemMeta()
					&& stack.getItemMeta().getLore() != null
					&& stack.getItemMeta().getLore().size() > 0)
				this.lore = stack.getItemMeta().getLore();
			
		}
		
		/**
		 * Gets the material that this InventoryItem is made up of.
		 * @return Material
		 */
		public Material getType() {
			return Material.getMaterial(this.type);
		}
		
		/**
		 * Gets a Map<Enchantment, Integer> (can be used in {@link ItemStack#addEnchantments(Map)}),
		 * skipping any invalid enchantments (see format in constructor)
		 * 
		 * @return Map of enchantments and levels
		 */
		public Map<Enchantment, Integer> getEnchantments() {
			
			Map<Enchantment, Integer> map = new HashMap<Enchantment, Integer>();
			
			for (String enchantment : (enchantments == null ? new ArrayList<String>() : enchantments)) { // Prevent NPEs
				if (enchantment.matches("^[a-zA-Z_]+:\\d+$")) {
					
					// Attempt to get Enchantment, skipping if not found
					Enchantment ench = Enchantment.getByName(enchantment.toUpperCase().split(":")[0]);
					if (ench == null) continue;
					
					// Put it in the map
					try {
						map.put(ench, Integer.parseInt(enchantment.split(":")[1]));
					} catch (NumberFormatException e) {}
					
				}
			}
			
			return map;
			
		}
		
		/**
		 * Converts this InventoryItem to an ItemStack
		 * 
		 * @return ItemStack of this InventoryItem
		 */
		@SuppressWarnings("deprecation")
		public ItemStack toItemStack() {
			
			ItemStack stack = new ItemStack(getType(), amount);
			
			if (damage != null) stack.setDurability(damage);
			if (data != null) stack.setData(new MaterialData(getType(), data));
			if (enchantments != null && enchantments.size() > 0) stack.addUnsafeEnchantments(getEnchantments());
			
			ItemMeta meta = stack.getItemMeta();
			
			if (displayName != null && !displayName.isEmpty()) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
			if (lore != null && lore.size() > 0) meta.setLore(lore);
			
			stack.setItemMeta(meta);
			
			return stack;
			
		}
		
	}
	
	public static class BlockConfig {
		
		public int x, y, z;
		public String world;
		
		/**
		 * Constructs a new BlockConfig with given block
		 * @param b Block to use
		 */
		public BlockConfig(Block b) {
			this.x = b.getX();
			this.y = b.getY();
			this.z = b.getZ();
			this.world = b.getWorld().getName();
		}
		
		public static boolean equals(Block a, BlockConfig b) {
			return a.getX() == b.x
					&& a.getY() == b.y
					&& a.getZ() == b.z
					&& a.getWorld().getName().equalsIgnoreCase(b.world);
		}
		
	}
	
}
