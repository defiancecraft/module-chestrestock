package com.defiancecraft.modules.chestrestock.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.defiancecraft.core.util.FileUtils;
import com.defiancecraft.modules.chestrestock.tasks.RestockTask;

public class RestockCache {

	private static Map<UUID, Map<UUID, Instant>> playerMap;
	
	/**
	 * Checks whether a player took an item after the last restock
	 * and before the next restock.
	 * 
	 * @param player Player to check
	 * @param id Unique ID of item
	 * @return Whether they took the item
	 */
	public static boolean isTaken(UUID player, UUID id) {

		if (!playerMap.containsKey(player))
			return false;
		
		Map<UUID, Instant> takenItems = playerMap.get(player);
		for (Entry<UUID, Instant> takenItem : takenItems.entrySet()) {
			if (takenItem.getKey().equals(id)
					&& takenItem.getValue().isAfter(RestockTask.getLastRestock())
					&& takenItem.getValue().isBefore(RestockTask.getNextRestock()))
				return true;
		}
		
		return false;
		
	}
	
	/**
	 * Sets the time an item was taken to now.
	 * 
	 * @param player Player that took the item
	 * @param id ID of item
	 */
	public static void setTaken(UUID player, UUID id) {
		
		if (!playerMap.containsKey(player))
			playerMap.put(player, new HashMap<UUID, Instant>());
		
		playerMap.get(player).put(id, Instant.now());
		
	}
	
	/**
	 * Reloads the restock cache, loading in all players that
	 * have taken items from the restocking chest and the last 
	 * time that the chest was restocked.
	 */
	public static void reload() {
		
		try {
			
			File file = FileUtils.getSharedConfig("restockitems.cache");
			if (!file.exists()) {
				
				playerMap = new HashMap<UUID, Map<UUID, Instant>>();
				if (RestockTask.getLastRestock() == null)
					RestockTask.setLastRestock(Instant.now());
			
			} else {
				
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
				RestockCacheObject o = (RestockCacheObject)in.readObject();
				
				RestockCache.playerMap = o.playerMap == null ? new HashMap<UUID, Map<UUID, Instant>>() : o.playerMap;
				if (RestockTask.getLastRestock() == null)
					RestockTask.setLastRestock(o.lastRestock == null ? Instant.now() : o.lastRestock);
				
				in.close();
				
			}
			
		} catch (Throwable t) {
			
			Bukkit.getLogger().warning("Failed to load restock cache; stack trace below.");
			t.printStackTrace();
			
			RestockCache.playerMap = new HashMap<UUID, Map<UUID, Instant>>();
			if (RestockTask.getLastRestock() == null)
				RestockTask.setLastRestock(Instant.now());
			
		}
			
	}
	
	/**
	 * Saves the cache to disk
	 */
	public static void save() {
		
		try {
			
			File file = FileUtils.getSharedConfig("restockitems.cache");
			if (!file.exists())
				file.createNewFile();
			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(new RestockCacheObject(RestockTask.getLastRestock(), playerMap));
			out.flush();
			out.close();
			
		} catch (Throwable t) {
			
			Bukkit.getLogger().warning("Failed to save restock cache; stack trace below.");
			t.printStackTrace();
			
		}
		
	}
	
	/**
	 * Clears the cache.
	 */
	public static void clear() {
		
		RestockCache.playerMap.clear();
		
	}
	
	public static class RestockCacheObject implements Serializable {
		
		private static final long serialVersionUID = 2502667968432441952L;
		
		public Instant lastRestock;
		public Map<UUID, Map<UUID, Instant>> playerMap;
		
		public RestockCacheObject(Instant lastRestock, Map<UUID, Map<UUID, Instant>> playerMap) {
			this.lastRestock = lastRestock;
			this.playerMap = playerMap;
		}
		
	}
	
}
