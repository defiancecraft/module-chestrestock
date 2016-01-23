package com.defiancecraft.modules.chestrestock.util;

/**
 * An exception representing when a restock chest is instantiated at
 * an inadequate time, i.e. the time until next restock is negative.
 * This is to prevent timing errors such that players do NOT access
 * chests while they are being restocked/before the restock task has
 * executed. 
 *
 */
public class RestockInProgressException extends Exception {

	private static final long serialVersionUID = 51479081380293729L;

	public String getMessage() {
		return "Chest restock is currently in progress/has not executed at expected time.";
	}
	
}
