package thecsdev.nounusedchunks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import thecsdev.nounusedchunks.client.NoUnusedChunksClient;
import thecsdev.nounusedchunks.config.NUCConfig;
import thecsdev.nounusedchunks.server.NoUnusedChunksServer;

@Mod(NoUnusedChunks.ModID)
public class NoUnusedChunks
{
	// ==================================================
	private static NoUnusedChunks Instance;
	// --------------------------------------------------
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	public static final String ModName = "No Unused Chunks";
	public static final String ModID   = "nounusedchunks";
	// --------------------------------------------------
	public final ModContainer ModInfo;
	// ==================================================
	public NoUnusedChunks()
	{
		//assign final stuff
		ModInfo = ModList.get().getModContainerById(ModID).get();
		
		//validate instance
		if(validateInstance())
			crash("Attempting to initialize " + ModID, new RuntimeException(ModID + " has already been initialized."));
		
		//on initialize
		if(getClass().equals(NoUnusedChunks.class)) //check if not a subclass
		{
			//depending on the side, initialize NoUnusedChunks
			if(FMLEnvironment.dist.isClient())
				new NoUnusedChunksClient();
			else if(FMLEnvironment.dist.isDedicatedServer())
				new NoUnusedChunksServer();
			else
				crash("Attempting to initialize " + ModID, new RuntimeException("Invalid FMLEnvironment.dist()"));
			
			//do not proceed, return
			return;
		}
		
		//assign instance
		Instance = this;
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//load config
		NUCConfig.loadProperties();
		
		//register this class as event listener
		MinecraftForge.EVENT_BUS.register(this);
	}
	// --------------------------------------------------
	/**
	 * Returns true if the {@link #Instance} is valid. This
	 * should always return true. If it doesn't, the mod
	 * probably hasn't been initialized yet.
	 */
	public static boolean validateInstance()
	{
		if(Instance != null && (Instance instanceof NoUnusedChunksClient || Instance instanceof NoUnusedChunksServer))
			return true;
		else return false;
	}
	// --------------------------------------------------
	/**
	 * Throws a {@link ReportedException} using a
	 * {@link CrashReport}.
	 */
	public static void crash(String crashMessage, Throwable exception)
	{
		CrashReport crashReport = CrashReport.forThrowable(exception, crashMessage);
		throw new ReportedException(crashReport);
	}
	// ==================================================
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
	public static NoUnusedChunks getInstance() { return Instance; }
	// ==================================================
	public static Component tt(String key) { return new TranslatableComponent(key); }
	public static Component lt(String text) { return new TextComponent(text); }
	// ==================================================
}