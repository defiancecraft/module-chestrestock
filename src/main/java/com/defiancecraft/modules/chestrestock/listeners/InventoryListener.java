package com.defiancecraft.modules.chestrestock.listeners;

import java.time.Duration;
import java.time.Instant;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.chestrestock.ChestRestock;
import com.defiancecraft.modules.chestrestock.inventory.RestockingInventoryHolder;
import com.defiancecraft.modules.chestrestock.inventory.RestockingItemStack;
import com.defiancecraft.modules.chestrestock.tasks.RestockTask;
import com.defiancecraft.modules.chestrestock.util.RestockCache;
import com.defiancecraft.modules.chestrestock.util.RestockInProgressException;

public class InventoryListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		if (e.isCancelled()
				|| !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				|| !e.getClickedBlock().getType().equals(Material.CHEST)
				|| !ChestRestock.getInventoriesConfig().isRestockChest(e.getClickedBlock()))
			return;

		e.setCancelled(true);
		
		// Open restock inventory
		try {
			RestockingInventoryHolder holder = new RestockingInventoryHolder(e.getPlayer());
			e.getPlayer().openInventory(holder.getInventory());
		} catch (RestockInProgressException ex) {
			e.getPlayer().sendMessage(ChatColor.RED + "Chest restock in progress");
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryDrag(InventoryDragEvent e) {
		
		// Return if none of the affected slots are ours, or it's not
		// our InventoryHolder, or it's cancelled.
		if (e.isCancelled()
				|| !(e.getInventory().getHolder() instanceof RestockingInventoryHolder)
				|| !e.getRawSlots().stream().anyMatch((s) ->
					s < ((RestockingInventoryHolder)e.getInventory().getHolder()).getInventorySize()
				))
			return;
		
		e.setCancelled(true);
		e.setResult(Result.DENY);
		
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent e) {
		
		// Return if not restocking chest or is cancelled
		if (e.isCancelled() || !(e.getInventory().getHolder() instanceof RestockingInventoryHolder))
			return;
		
		RestockingInventoryHolder holder = (RestockingInventoryHolder) e.getInventory().getHolder();
		
		// If the chest restocked during this time or should have restocked,
		// stop them from taking any items - timing bug.
		if (!holder.getLastRestock().equals(RestockTask.getLastRestock())
				|| Duration.between(Instant.now(), RestockTask.getNextRestock()).isNegative()) {
			
			e.setCancelled(true);
			e.getWhoClicked().sendMessage(ChatColor.RED + "Chest restock is in progress");
			
			// Close their inventory (although it should have been closed by RestockTask)
			new BukkitRunnable() {
				
				@Override
				public void run() {
					e.getWhoClicked().closeInventory();
				}
				
			}.runTask(JavaPlugin.getPlugin(ChestRestock.class));
			
			return;
		}
		
		// If they are swapping an item, placing one in, or
		// shift-clicking one in, deny it!
		if (
				(!e.isShiftClick()
					&& !e.getSlotType().equals(SlotType.OUTSIDE)
					&& e.getRawSlot() < holder.getInventorySize()
					&& e.getCursor() != null
					&& !e.getCursor().getType().equals(Material.AIR))
				||
				(e.isShiftClick()
					&& e.getRawSlot() >= holder.getInventorySize())
			) {
			e.setCancelled(true);
			((Player)e.getWhoClicked()).sendMessage(ChatColor.RED + "You cannot place items into this chest.");
			return;
		}
		
		// If it's a right-click, or if it's a shift-click and player has no room
		// or if it's a double click (will stack items automatically, must be prevented)
		if (e.isRightClick()
				|| (e.isShiftClick()
						&& !playerHasRoom((Player)e.getWhoClicked(), e.getCurrentItem())
				|| e.getClick().equals(ClickType.DOUBLE_CLICK))
			) {
			e.setCancelled(true);
			return;
		}
		
		// Handle if it's a restocking item
		if (e.getRawSlot() >= 0 && e.getRawSlot() < holder.getInventorySize()) {
			
			ItemStack i = holder.getItems().size() <= e.getRawSlot() ? null : holder.getItems().get(e.getRawSlot());
			if (i instanceof RestockingItemStack) {
				
				RestockingItemStack ri = (RestockingItemStack)i;
				
				// Deny if they don't have the permission for it
				if (!ri.isAllowed()) {
					
					e.setCancelled(true);
					e.setResult(Result.DENY);
					
				// Set to taken in cache if they do
				} else {
					RestockCache.setTaken(e.getWhoClicked().getUniqueId(), ri.getUniqueId());
				}
				
			// If it's not our item, but it's in our inventory, fucking deny it!
			} else {
				e.setCancelled(true);
			}
			
		}
		
	}
	
	public boolean playerHasRoom(Player p, ItemStack a) {
		
		int required = a.getAmount();
		
		for (ItemStack b : p.getInventory().getContents()) {
			
			// Free slot
			if (b == null) return true;
			
			// Will stack
			if (a.isSimilar(b)
					&& b.getAmount() < b.getMaxStackSize()) {
				
				required -= b.getMaxStackSize() - b.getAmount();
				if (required <= 0)
					return true;
				
			}
		}
		
		return false;
		
	}
	
}
