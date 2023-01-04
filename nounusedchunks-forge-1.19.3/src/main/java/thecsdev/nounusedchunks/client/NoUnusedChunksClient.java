package thecsdev.nounusedchunks.client;

import net.minecraft.client.Minecraft;
import thecsdev.nounusedchunks.NoUnusedChunks;

public class NoUnusedChunksClient extends NoUnusedChunks
{
	public static Minecraft MCClient;
	public NoUnusedChunksClient() { MCClient = Minecraft.getInstance(); }
}