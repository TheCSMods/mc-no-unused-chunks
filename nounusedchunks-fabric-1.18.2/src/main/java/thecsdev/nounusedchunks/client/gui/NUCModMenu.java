package thecsdev.nounusedchunks.client.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import thecsdev.nounusedchunks.NoUnusedChunks;

public final class NUCModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> NoUnusedChunks.getConfigScreen(parent);
	}
}