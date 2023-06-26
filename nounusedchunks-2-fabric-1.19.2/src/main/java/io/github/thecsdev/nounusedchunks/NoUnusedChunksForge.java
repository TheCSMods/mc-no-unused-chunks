package io.github.thecsdev.nounusedchunks;

import io.github.thecsdev.nounusedchunks.client.NoUnusedChunksClient;
import io.github.thecsdev.nounusedchunks.server.NoUnusedChunksServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge Mod Loader entry-points for this mod.
 */
@Mod(NoUnusedChunks.ModID)
public final class NoUnusedChunksForge
{
	// ==================================================
	public NoUnusedChunksForge()
	{
		//*literal black magic...*
		//(it's amazing this actually works)
		if(FMLEnvironment.dist.isClient())
			new NoUnusedChunksClient();
		else if(FMLEnvironment.dist.isDedicatedServer())
			new NoUnusedChunksServer();
	}
	// ==================================================
}