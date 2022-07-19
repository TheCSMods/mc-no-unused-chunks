package thecsdev.nounusedchunks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.client.command.NUCClientCommand;
import thecsdev.nounusedchunks.command.NUCCommand;

public final class NoUnusedChunksClient extends NoUnusedChunks implements ClientModInitializer
{
	// ==================================================
	private NUCClientCommand Command;
	// ==================================================
	@Override
	public void onInitializeClient()
	{
		//register the command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
		{
			Command = new NUCClientCommand();
			Command.register(dispatcher);
		});
	}
	// ==================================================
	@Override
	public NUCCommand<?> getCommand() { return Command; }
	// ==================================================
}