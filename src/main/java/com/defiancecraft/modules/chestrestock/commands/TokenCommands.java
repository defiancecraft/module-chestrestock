package com.defiancecraft.modules.chestrestock.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.modules.chestrestock.ChestRestock;

public class TokenCommands {

	@SuppressWarnings("deprecation")
	public static boolean redeem(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		Player p = (Player) sender;
		ItemStack[] items = p.getInventory().getContents();
		int amount = 0;
		
		for (int i = 0; i < items.length; i++) {
			
			// Ignore air
			if (items[i] == null)
				continue;
			
			// Count tokens			
			if (items[i].getType().equals(Material.DOUBLE_PLANT) && items[i].getDurability() == 0) {
				amount += items[i].getAmount();
				items[i] = null;
			}
				
		}
		
		try {
			Economy.deposit(p.getName(), amount);
		} catch (Exception e) {
			p.sendMessage(ChatColor.RED + "Failed to redeem tokens.");
			return true;
		}
		
		p.getInventory().setContents(items);
		p.updateInventory();
		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChestRestock.getMainConfig().redeemMultipleMsg));
		
		return true;
				
	}
	
}
