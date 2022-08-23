package thecsdev.nounusedchunks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.client.command.NUCClientCommand;
import thecsdev.nounusedchunks.command.NUCCommand;

public final class NoUnusedChunksClient extends NoUnusedChunks implements ClientModInitializer
{
	// ==================================================
	public static MinecraftClient MCClient;
	private NUCClientCommand Command;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//define the MCClient
		MCClient = MinecraftClient.getInstance();
		
		//register command
		Command = new NUCClientCommand();
		Command.register(ClientCommandManager.DISPATCHER);
	}
	// ==================================================
	@Override
	public NUCCommand<?> getCommand() { return Command; }
	// ==================================================
}