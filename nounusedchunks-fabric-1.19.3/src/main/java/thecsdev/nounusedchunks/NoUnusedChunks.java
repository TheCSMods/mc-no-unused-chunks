package thecsdev.nounusedchunks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
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
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
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
	public static Text tt(String translationKey) { return Text.translatable(translationKey); }
	public static Text lt(String text) { return Text.literal(text); }
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
}