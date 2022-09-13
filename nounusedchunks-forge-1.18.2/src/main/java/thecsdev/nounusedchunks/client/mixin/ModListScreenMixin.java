package thecsdev.nounusedchunks.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.widget.ModListWidget;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.client.gui.screen.NUCConfigScreen;

@Mixin(value = ModListScreen.class, remap = false)
public abstract class ModListScreenMixin extends Screen
{
	protected ModListScreenMixin(Component p_96550_) { super(p_96550_); }

	@Shadow public ModListWidget.ModEntry selected;
	@Shadow public Button configButton;
	
	@Inject(method = "displayModConfig", at = @At("HEAD"), cancellable = true, remap = false)
	public void onDisplayModConfig(CallbackInfo callback)
	{
		//check if selection is this mod
		if (selected == null ||
			!selected.getInfo().getModId().equals(NoUnusedChunks.ModID))
			return;
		
		//open screen
		Minecraft.getInstance().setScreen(new NUCConfigScreen((ModListScreen)(Object)this));
		callback.cancel();
	}
	
	@Inject(method = "updateCache", at = @At("RETURN"), remap = false)
	public void onUpdateCache(CallbackInfo callback)
	{
		if(this.selected != null && this.selected.getInfo().getModId().equals(NoUnusedChunks.ModID))
			this.configButton.active = true;
	}
}