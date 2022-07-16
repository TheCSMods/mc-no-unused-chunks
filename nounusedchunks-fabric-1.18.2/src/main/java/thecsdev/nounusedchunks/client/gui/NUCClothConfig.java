package thecsdev.nounusedchunks.client.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import thecsdev.nounusedchunks.config.NUCConfig;

public final class NUCClothConfig
{
	public static Screen createConfigScreen(Screen parent)
	{
		//create the config builder and the entry builder
		ConfigBuilder builder = ConfigBuilder.create()
		        .setParentScreen(parent)
		        .setTitle(tt("nounusedchunks.clothconfig.title"))
		        .setSavingRunnable(() -> NUCConfig.saveProperties());
		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		
		//general settings
		ConfigCategory general = builder.getOrCreateCategory(tt("nounusedchunks.clothconfig.general"));
		general.addEntry(entryBuilder.startBooleanToggle(tt("nounusedchunks.clothconfig.general.enabled"), NUCConfig.ENABLED)
				.setDefaultValue(true)
				.setSaveConsumer(newValue -> NUCConfig.ENABLED = newValue)
				.setTooltip(tt("nounusedchunks.clothconfig.general.enabled.tooltip"))
				.build());
		general.addEntry(entryBuilder.startIntSlider(tt("nounusedchunks.clothconfig.general.unflag_chance"), NUCConfig.UNFLAG_CHANCE, 1, 100)
				.setDefaultValue(100)
				.setSaveConsumer(newValue -> NUCConfig.UNFLAG_CHANCE = newValue)
				.setTooltip(tt("nounusedchunks.clothconfig.general.unflag_chance.tooltip"))
				.build());
		
		//finally return
		return builder.build();
	}
	
	public static TranslatableText tt(String key) { return new TranslatableText(key); }
}