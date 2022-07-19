package thecsdev.nounusedchunks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.api.EnvType;
import net.minecraft.command.CommandSource;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.config.NUCConfig;

public abstract class NUCCommand<CS extends CommandSource>
{
	// ==================================================
	public void register(CommandDispatcher<CS> dispatcher)
	{
		dispatcher.register(literal(getCommandName())
				.then(literal("config").requires(cs -> canConfig(cs))
						.then(literal("ENABLED")
								.then(argument("value", BoolArgumentType.bool())
										.executes(context -> exec_config_ENABLED(context))))
						.then(literal("UNFLAG_CHANCE")
								.then(argument("value", IntegerArgumentType.integer(1, 100))
										.executes(context -> exec_config_UNFLAGCHANCE(context))))));
	}
	// --------------------------------------------------
	private int exec_config_ENABLED(CommandContext<CS> context)
	{
		NUCConfig.ENABLED = context.getArgument("value", Boolean.class);
		NUCConfig.saveProperties();
		return 1;
	}
	
	private int exec_config_UNFLAGCHANCE(CommandContext<CS> context)
	{
		NUCConfig.UNFLAG_CHANCE = context.getArgument("value", Integer.class);
		NUCConfig.saveProperties();
		return 1;
	}
	// ==================================================
	/**
	 * The name of the command. <b>Must be constant!</b>
	 */
	public abstract String getCommandName();
	// --------------------------------------------------
	/**
	 * Returns true if a given command source can use
	 * the config commands.
	 */
	public abstract boolean canConfig(CS commandSource);
	// --------------------------------------------------
	/**
	 * Used to make a command syntax require a
	 * specific {@link EnvType}.
	 * @param env The required {@link EnvType}.
	 */
	protected final boolean requireEnv(EnvType env)
	{
		try { return NoUnusedChunks.getEnviroment() == env; }
		catch (Exception e) { return false; }
	}
	// ==================================================
	/**
	 * Creates a literal argument builder.
	 *
	 * @param name the literal name
	 * @return the created argument builder
	 */
	public LiteralArgumentBuilder<CS> literal(String name) { return LiteralArgumentBuilder.literal(name); }
	// --------------------------------------------------
	/**
	 * Creates a required argument builder.
	 *
	 * @param name the name of the argument
	 * @param type the type of the argument
	 * @param <CS>  the type of the parsed argument value
	 * @return the created argument builder
	 */
	public <ARG> RequiredArgumentBuilder<CS, ARG> argument(String name, ArgumentType<ARG> type)
	{
		return RequiredArgumentBuilder.argument(name, type);
	}
	// ==================================================
}