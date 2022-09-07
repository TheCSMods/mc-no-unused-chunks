package thecsdev.nounusedchunks;

import java.util.Random;
import java.util.logging.Logger;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import thecsdev.nounusedchunks.client.NoUnusedChunksClient;
import thecsdev.nounusedchunks.command.NUCCommand;
import thecsdev.nounusedchunks.config.NUCConfig;
import thecsdev.nounusedchunks.server.NoUnusedChunksServer;

public abstract class NoUnusedChunks 
{
	// ==================================================
	private static NoUnusedChunks Instance;
	// --------------------------------------------------
	public static final Logger LOGGER = Logger.getLogger(getModID());
	// --------------------------------------------------
	public static final String ModName = "No Unused Chunks";
	public static final String ModID   = "nounusedchunks";
	// ==================================================
	public NoUnusedChunks()
	{
		//validate and assign instance
		if(validateInstance())
		{
			String crashMsg = "Attempting to initialize " + ModID;
			RuntimeException exc = new RuntimeException(ModID + " has already been initialized.");
			throw new CrashException(new CrashReport(crashMsg, exc));
		}
		Instance = this;
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' as '" + getClass().getSimpleName() + "'.");
		
		//load properties
		NUCConfig.loadProperties();
	}
	// --------------------------------------------------
	public static String getModName() { return ModName; }
	public static String getModID() { return ModID; }
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
	
	/**
	 * Returns the currently running {@link EnvType}.
	 * @throws RuntimeException If the mod hasn't been initialized yet.
	 */
	public static EnvType getEnviroment()
	{
		//validate
		if(!validateInstance())
			throw new RuntimeException(ModID + " is unitialized.");
		//return based on instance type
		if(Instance instanceof NoUnusedChunksClient) return EnvType.CLIENT;
		else return EnvType.SERVER;
	}
	
	public static boolean isClient() { return getEnviroment() == EnvType.CLIENT; }
	public static boolean isServer() { return getEnviroment() == EnvType.SERVER; }
	// --------------------------------------------------
	/**
	 * Returns the registered {@link NUCCommand}.
	 * Will return null if the mod hasn't been initialized yet.
	 */
	public abstract NUCCommand<?> getCommand();
	// ==================================================
	public static Screen getConfigScreen(Screen parent)
	{
		//if cloth config is loaded, create a screen and return it
		if(FabricLoader.getInstance().isModLoaded("cloth-config2"))
			return thecsdev.nounusedchunks.client.gui.NUCClothConfig.createConfigScreen(parent);
		//else return null
		else return null;
	}
	// --------------------------------------------------
	public static Text tt(String translationKey) { return new TranslatableText(translationKey); }
	public static Text lt(String text) { return new LiteralText(text); }
	// --------------------------------------------------
	/**
	 * Returns true if a chunk is unused.
	 * (well, it's supposed to. idk)
	 */
	public static boolean isChunkUnused(RegistryKey<World> worldRegKey, ChunkPos chunkPos, NbtCompound chunkNbt)
	{
		return chunkNbt.contains("InhabitedTime") &&
				chunkNbt.getLong("InhabitedTime") == 0;
	}
	// ==================================================
	public static int nextInt(Random random, int min, int max) { return random.nextInt((max - min) + 1) + min; }
	// ==================================================
}