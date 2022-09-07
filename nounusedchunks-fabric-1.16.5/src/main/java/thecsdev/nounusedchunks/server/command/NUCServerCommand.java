package thecsdev.nounusedchunks.server.command;

import net.minecraft.server.command.ServerCommandSource;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.command.NUCCommand;

public class NUCServerCommand extends NUCCommand<ServerCommandSource>
{
	@Override
	public String getCommandName() { return NoUnusedChunks.ModID + "srv"; }

	@Override
	public boolean canConfig(ServerCommandSource commandSource) { return commandSource.hasPermissionLevel(4); }
}
