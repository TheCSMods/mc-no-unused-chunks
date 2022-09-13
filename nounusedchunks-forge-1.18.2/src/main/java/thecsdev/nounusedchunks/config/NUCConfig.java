package thecsdev.nounusedchunks.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import thecsdev.nounusedchunks.NoUnusedChunks;

public final class NUCConfig
{
	// ==================================================
	public static boolean ENABLED = true;
	public static int UNFLAG_CHANCE = 100;
	// -------------------------------------------------- TEMP. VALUES:
	public static boolean OW_RUC = false; //remove unused chunks
	public static boolean OW_RLC = false; //remove lighting cache
	// ==================================================
	public static void saveProperties()
	{
		try
		{
			//get and make sure the properties file exists
			File fProp = getPropertiesFile();
			if(!fProp.exists())
			{
				fProp.getParentFile().mkdirs();
				fProp.createNewFile();
			}
			
			//create a Properties instance and store the properties
			Properties prop = new Properties();
			prop.setProperty("ENABLED", Boolean.toString(ENABLED));
			prop.setProperty("UNFLAG_CHANCE", Integer.toString(UNFLAG_CHANCE));
			
			//save the properties
			FileOutputStream fos = new FileOutputStream(fProp);
			prop.store(fos, NoUnusedChunks.ModID + " properties");
			fos.close();

			//log
			NoUnusedChunks.LOGGER.info("Saved '" + NoUnusedChunks.ModID + "' config.");
		}
		catch(IOException ioExc)
		{
			//if saving is unsuccessful, throw a CrashException
			NoUnusedChunks.crash("Unable to save the '" + NoUnusedChunks.ModID + "' mod config.", ioExc);
		}
	}
	// --------------------------------------------------
	public static void loadProperties()
	{
		try
		{
			//get and make sure the properties file exists
			File fProp = getPropertiesFile();
			if(!fProp.exists())
			{
				NoUnusedChunks.LOGGER.info("Could not load '" + NoUnusedChunks.ModID + "' config. File not found.");
				return;
			}
			
			//create a Properties instance and load the properties
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream(fProp);
			prop.load(fis);
			fis.close();
			
			//read the properties
			ENABLED = smartBool(prop.getProperty("ENABLED"), true);
			UNFLAG_CHANCE = smartInt(prop.getProperty("UNFLAG_CHANCE"), 1, 100, 100);
			
			//read OW_RUC just so dedicated servers can use it.
			//useless on the client
			OW_RUC = smartBool(prop.getProperty("OW_RUC"), false);
			
			//log
			NoUnusedChunks.LOGGER.info("Loaded '" + NoUnusedChunks.ModID + "' config.");
		}
		catch(IOException ioExc)
		{
			//if loading is unsuccessful, throw a CrashException
			NoUnusedChunks.crash("Unable to load the '" + NoUnusedChunks.ModID + "' mod config.", ioExc);
		}
	}
	// ==================================================
	/**
	 * Returns the {@link File} where the
	 * {@link #PROPERTIES} should be stored.
	 */
	public static File getPropertiesFile()
	{
		return new File(System.getProperty("user.dir") + "/config/" + NoUnusedChunks.ModID + ".properties");
	}
	// ==================================================
	//private static boolean smartBool(String arg0) { return smartBool(arg0, true); }
	private static boolean smartBool(String arg0, boolean def)
	{
		if(arg0 == null) return def;
		String a = arg0.split(" ")[0].toLowerCase();
		return (a.startsWith("true") || a.startsWith("ye") ||
				a.startsWith("ok") || a.startsWith("sur")) && a.length() <= 5;
	}
	
	private static int smartInt(String arg0, int min, int max, int def)
	{
		try { return clamp(Integer.parseInt(arg0), min, max); }
		catch(Exception e) { return clamp(def, min, max); }
	}
	
	private static int clamp(int in, int min, int max) { return Math.max(min, Math.min(max, in)); }
	// ==================================================
}