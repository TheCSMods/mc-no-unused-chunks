package thecsdev.nounusedchunks.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.command.NUCCommand;
import thecsdev.nounusedchunks.server.command.NUCServerCommand;

public final class NoUnusedChunksServer extends NoUnusedChunks implements DedicatedServerModInitializer
{
	// ==================================================
	private NUCServerCommand Command;
	// ==================================================
	@Override
	public void onInitializeServer()
	{
		//register the command
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
		{
			Command = new NUCServerCommand();
			Command.register(dispatcher);
		});
	}
	// ==================================================
	@Override
	public NUCCommand<?> getCommand() { return Command; }
	// ==================================================
}