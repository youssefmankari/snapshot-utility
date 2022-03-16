package org.fajr.snapshot_utility;

import java.io.File;

public class Preferences {
	
	public static String applicationSettingsFolder = 
			System.getProperty("user.home") + 
			File.separator + 
			"Fajr App"+ 
			File.separator + 
			"Settings" ;
	
	public static String settingsFile = "settings.json";
	public static String cursorFileName = "telescop.png";;
	
	

}
