package io.github.thecsdev.nounusedchunks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Fabric Mod Loader entry-points for this mod.
 */
public final class NoUnusedChunksFML implements ClientModInitializer, DedicatedServerModInitializer
{
	// ==================================================
	public @Override void onInitializeClient() { new io.github.thecsdev.nounusedchunks.client.NoUnusedChunksClient(); }
	public @Override void onInitializeServer() { new io.github.thecsdev.nounusedchunks.server.NoUnusedChunksServer(); }
	// ==================================================
}