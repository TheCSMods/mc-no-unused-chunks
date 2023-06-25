package io.github.thecsdev.nounusedchunks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class NoUnusedChunks extends Object
{
	// ==================================================
	public static final Logger LOGGER = LoggerFactory.getLogger(getModID());
	// --------------------------------------------------
	private static final String ModID = "nounusedchunks";
	private static NoUnusedChunks Instance;
	// --------------------------------------------------
	protected final ModContainer modInfo;
	// ==================================================
	public NoUnusedChunks()
	{
		//validate instance first
		if(isModInitialized())
			throw new IllegalStateException(getModID() + " has already been initialized.");
		else if(!isInstanceValid(this))
			throw new UnsupportedOperationException("Invalid " + getModID() + " type: " + this.getClass().getName());
		
		//assign instance
		Instance = this;
		modInfo = FabricLoader.getInstance().getModContainer(getModID()).get();
		
		//log stuff
		LOGGER.info("Initializing '" + getModName() + "' " + modInfo.getMetadata().getVersion() +
				" as '" + getClass().getSimpleName() + "'.");
		//LOGGER.info("Initializing '" + getModID() + "' as '" + getClass().getSimpleName() + "'.");
		
		//init stuff
		//TODO - Initialize common stuff here (client/dedicated-server/internal-server)
	}
	// ==================================================
	public static NoUnusedChunks getInstance() { return Instance; }
	public ModContainer getModInfo() { return modInfo; }
	// --------------------------------------------------
	public static String getModName() { return getInstance().getModInfo().getMetadata().getName(); }
	public static String getModID() { return ModID; }
	// --------------------------------------------------
	public static boolean isModInitialized() { return isInstanceValid(Instance); }
	private static boolean isInstanceValid(NoUnusedChunks instance) { return isServer(instance) || isClient(instance); }
	// --------------------------------------------------
	public static boolean isServer() { return isServer(Instance); }
	public static boolean isClient() { return isClient(Instance); }
	
	private static boolean isServer(NoUnusedChunks arg0) { return arg0 instanceof io.github.thecsdev.nounusedchunks.server.NoUnusedChunksServer; }
	private static boolean isClient(NoUnusedChunks arg0) { return arg0 instanceof io.github.thecsdev.nounusedchunks.client.NoUnusedChunksClient; }
	// ==================================================
	/**
	 * Returns true if a chunk is unused.
	 * <p>
	 * Checks are done based off the Chunk's NBT data.
	 * 
	 * @param chunkNbt The Chunk's {@link NbtCompound} data.
	 */
	public static boolean isChunkUnused(NbtCompound chunkNbt)
	{
		return chunkNbt.contains("InhabitedTime", NbtElement.LONG_TYPE) &&
				chunkNbt.getLong("InhabitedTime") == 0;
	}
	// ==================================================
}