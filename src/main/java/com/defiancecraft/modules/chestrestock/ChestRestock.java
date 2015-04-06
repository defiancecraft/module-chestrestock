package com.defiancecraft.modules.chestrestock;

import java.time.Duration;
import java.time.Instant;

import org.bukkit.plugin.java.JavaPlugin;

import com.defiancecraft.core.command.CommandRegistry;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.modules.Module;
import com.defiancecraft.core.util.FileUtils;
import com.defiancecraft.core.util.JsonConfig;
import com.defiancecraft.modules.chestrestock.commands.RestockCommands;
import com.defiancecraft.modules.chestrestock.config.InventoriesConfig;
import com.defiancecraft.modules.chestrestock.config.MainConfig;
import com.defiancecraft.modules.chestrestock.listeners.InventoryListener;
import com.defiancecraft.modules.chestrestock.listeners.TokenListener;
import com.defiancecraft.modules.chestrestock.tasks.RestockTask;
import com.defiancecraft.modules.chestrestock.util.RestockCache;

public class ChestRestock extends JavaPlugin implements Module {

	private static InventoriesConfig inventoriesConfig;
	private static MainConfig mainConfig;
	
	private RestockTask task;
	
    public void onEnable() {

        // Save inventoriesConfig, so each new item gets a unique ID
    	ChestRestock.inventoriesConfig = JsonConfig.load(FileUtils.getSharedConfig("inventories.json"), InventoriesConfig.class);
    	ChestRestock.inventoriesConfig.save(FileUtils.getSharedConfig("inventories.json"));
    	
    	// Load main config
    	ChestRestock.mainConfig = getConfig(MainConfig.class);

    	// Register commands
    	CommandRegistry.registerPlayerCommand(this, "restockchest", RestockCommands::restockChest);
    	CommandRegistry.registerPlayerCommand(this, "restock", "defiancecraft.restock.help", RestockCommands::help);
    	CommandRegistry.registerPlayerSubCommand("restock", "setup", "defiancecraft.restock.setup", RestockCommands::setup);
    	CommandRegistry.registerPlayerSubCommand("restock", "status", "defiancecraft.restock.status", RestockCommands::status);
    	CommandRegistry.registerPlayerSubCommand("restock", "create", "defiancecraft.restock.create", RestockCommands::create);
    	
    	// Load the restock cache
    	RestockCache.reload();
    	
    	// Run restock task
    	this.task = new RestockTask();
    	this.task.runTaskTimer(this, RestockTask.getNextRestock().isBefore(Instant.now()) ? 0 :
    						    Duration.between(Instant.now(), RestockTask.getNextRestock()).getSeconds() * 20,
    						    mainConfig.restockInterval * 1200); // Period is in minutes, change to ticks
    	
    	// Register listeners
    	getServer().getPluginManager().registerEvents(new InventoryListener(), this);
    	getServer().getPluginManager().registerEvents(new TokenListener(), this);
    	
    }

    public void onDisable() {
    	
    	// Save restock cache
    	RestockCache.save();
    	
    	// Cancel task
    	this.task.cancel();
    	
    }
    
    public static InventoriesConfig getInventoriesConfig() {
    	return ChestRestock.inventoriesConfig;
    }
    
    public static void saveInventoriesConfig() {
    	ChestRestock.inventoriesConfig.save(FileUtils.getSharedConfig("inventories.json"));
    }
    
    public static MainConfig getMainConfig() {
    	return ChestRestock.mainConfig;
    }
    
    @Override
    public String getCanonicalName() {
        return "ChestRestock";
    }

    @Override
    public Collection[] getCollections() {
        return new Collection[] {};
    }

}
