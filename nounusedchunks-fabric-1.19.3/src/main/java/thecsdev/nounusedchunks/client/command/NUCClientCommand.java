package thecsdev.nounusedchunks.client.command;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.command.NUCCommand;

public final class NUCClientCommand extends NUCCommand<FabricClientCommandSource>
{
	@Override
	public String getCommandName() { return NoUnusedChunks.ModID; }

	@Override
	public boolean canConfig(FabricClientCommandSource commandSource) { return true; }
}
