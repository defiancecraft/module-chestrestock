package com.defiancecraft.modules.chestrestock.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.core.command.ArgumentParser;
import com.defiancecraft.core.command.ArgumentParser.Argument;
import com.defiancecraft.modules.chestrestock.ChestRestock;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig.BlockConfig;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig.InventoryConfig;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig.InventoryItem;
import com.defiancecraft.modules.chestrestock.inventory.RestockingInventoryHolder;
import com.defiancecraft.modules.chestrestock.tasks.RestockTask;

public class RestockCommands {

	public static boolean help(CommandSender sender, String[] args) {
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&9&lRestock Help\n"
				+ "&b/restockchest\n"
				+ "&b/restock create <permission> <upgrade text>\n"
				+ "&b/restock setup\n"
				+ "&b/restock status\n"
		));
		return true;
		
	}
	
	public static boolean restockChest(CommandSender sender, String[] args) {
	
		if (!(sender instanceof Player))
			return false;
		
		Player p = (Player)sender;
		
		// Send message if they don't have permission
		if (!p.hasPermission("defiancecraft.restockchest")) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChestRestock.getMainConfig().upgradeMsg));
		} else {
			
			// Check if not in whitelisted world
			if (!ChestRestock.getMainConfig().whitelistWorlds.stream().anyMatch((w) -> 
				w.equalsIgnoreCase(p.getWorld().getName())
			)) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChestRestock.getMainConfig().whitelistMsg));
				return true;
			}
			
			RestockingInventoryHolder holder = new RestockingInventoryHolder(p);
			p.openInventory(holder.getInventory());
			
		}
		
		return true;
		
	}
	
	public static boolean status(CommandSender sender, String[] args) {
		
		sender.sendMessage(ChatColor.AQUA + "Next restock is at " + ChatColor.WHITE + ChatColor.ITALIC + RestockTask.getNextRestock().toString());
		return true;
		
	}
	
	public static boolean create(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.STRING);
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /restock create <permission> <upgrade text>");
			return true;
		}
		
		Player p = (Player)sender;
		@SuppressWarnings("deprecation")
		Block target = p.getTargetBlock((HashSet<Byte>)null, 20);
		ItemStack[] contents;
		
		// Get the inventory contents (either chest or player)
		if (!target.getType().equals(Material.CHEST)
				|| !(target.getState() instanceof Chest)) {
			
			p.sendMessage(ChatColor.GRAY + "Not looking at chest. Using inventory instead.");
			contents = p.getInventory().getContents();
			
		} else {
			
			contents = ((Chest)target.getState()).getBlockInventory().getContents();
			
		}
		
		// Create config for inventory
		InventoryConfig config = new InventoryConfig();
		config.items = new ArrayList<InventoryItem>();
		config.permission = parser.getString(1);
		config.upgradeText = parser.getString(2);
		
		for (ItemStack item : contents)
			if (item != null)
				config.items.add(new InventoryItem(item));
		
		// Add config to the actual config, and save
		ChestRestock.getInventoriesConfig().inventories.add(config);
		ChestRestock.saveInventoriesConfig();
		
		p.sendMessage(ChatColor.GREEN + "Created inventory with permission '" + parser.getString(1) + "'!");
		return true;
		
	}
	
	public static boolean setup(CommandSender sender, String[] args) {
		
		Player p = (Player)sender;
		Block target = p.getTargetBlock(new HashSet<Material>(Arrays.asList(Material.AIR)), 20);
		
		if (!target.getType().equals(Material.CHEST)) {
			p.sendMessage(ChatColor.RED + "You are not looking at a chest.");
			return true;
		}
		
		BlockConfig config = new BlockConfig(target);
		ChestRestock.getInventoriesConfig().chests.add(config);
		ChestRestock.saveInventoriesConfig();
		
		p.sendMessage(ChatColor.GREEN + "Created restock chest!");
		
		return true;
		
	}
	
}
