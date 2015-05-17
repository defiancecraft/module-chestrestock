package com.defiancecraft.modules.chestrestock.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.modules.chestrestock.ChestRestock;

public class TokenListener implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		// Return if cancelled, no item, not right-click, or not a token
		if (e.isCancelled()
				|| !ChestRestock.getMainConfig().tokenRedeemClickEnabled
				|| (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
						&& !e.getAction().equals(Action.RIGHT_CLICK_AIR))
				|| e.getItem() == null
				|| !e.getItem().getType().name().equalsIgnoreCase(ChestRestock.getMainConfig().tokenType)
				|| e.getItem().getDurability() != ChestRestock.getMainConfig().tokenDamage
			)
			return;
		
		e.setCancelled(true);
		
		final String name = e.getPlayer().getName();
		
		Economy.deposit(name, 1.0);
		e.setUseInteractedBlock(Result.DENY);
		e.setUseItemInHand(Result.DENY);
		
		// Subtract 1 item
		ItemStack inHand = e.getItem();
		inHand.setAmount(inHand.getAmount() - 1);
		e.getPlayer().setItemInHand(inHand);
		
		e.getPlayer().updateInventory();
		e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', ChestRestock.getMainConfig().redeemMsg));
		
		
	}
	
}
