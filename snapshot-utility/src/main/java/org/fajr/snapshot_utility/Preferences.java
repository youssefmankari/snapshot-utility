package org.fajr.snapshot_utility;

import java.io.File;

public class Preferences {
	
	public static String APPLICATION_SETTINGS_FOLDER = 
			System.getProperty("user.home") + 
			File.separator + 
			"Fajr App"+ 
			File.separator + 
			"Settings" ;
	
	public static String SETTINGS_FILE = "settings.json";
	public static String CURSOR_FILE = "telescop.png";;
	
	

}
