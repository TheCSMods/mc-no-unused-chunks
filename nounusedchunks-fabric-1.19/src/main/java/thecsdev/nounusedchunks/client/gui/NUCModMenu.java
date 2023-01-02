package thecsdev.nounusedchunks.client.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import thecsdev.nounusedchunks.client.gui.screen.NUConfigScreen;

public final class NUCModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent ->
		{
			//if cloth config is loaded, create a screen and return it
			/*if(FabricLoader.getInstance().isModLoaded("cloth-config"))
				return NUCClothConfig.createConfigScreen(parent);
			//else return the default config screen
			else */return new NUConfigScreen(parent);
		};
	}
}