package com.defiancecraft.modules.chestrestock.config;

import java.util.Arrays;
import java.util.List;

public class MainConfig {

	public String inventoryTitle = "Restock: {time}";
	public int inventoryRows = 5;
	
	public int restockInterval = 10;
	public String upgradeMsg = "&a&lGive me your money to use this command.";
	
	public String tokenType = "DOUBLE_PLANT";
	public short tokenDamage = 0;
	public String tokenName = "Right click to get tokens!";
	public List<String> tokenLore = Arrays.asList("I am worth", "1 token!");
	public String redeemMsg = "&aYou redeemed: 1 token!";
	
	public List<String> whitelistWorlds = Arrays.asList("world");
	public String whitelistMsg = "Feck offfff";
	
}
