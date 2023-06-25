package io.github.thecsdev.nounusedchunks.client.mixin.events;

import static io.github.thecsdev.nounusedchunks.NoUnusedChunksConfig.TEMP_OWS_RUC;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.thecsdev.nounusedchunks.client.gui.widgets.ActionCheckboxWidget;
import io.github.thecsdev.nounusedchunks.client.mixin.addons.AddonBackupPromptScreen;
import io.github.thecsdev.nounusedchunks.client.mixin.hooks.AccessorScreen;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

@Mixin(BackupPromptScreen.class)
public abstract class MixinBackupPromptScreen extends Screen implements AddonBackupPromptScreen
{
	// ==================================================
	protected MixinBackupPromptScreen(Text title) { super(title); }
	// ==================================================
	private @Unique ActionCheckboxWidget nounusedchunks_removeUnusedChunksCheckbox;
	// --------------------------------------------------
	@Accessor("showEraseCacheCheckbox")
	public abstract boolean getShowEraseCacheCheckbox();
	
	@Accessor("eraseCacheCheckbox")
	public abstract CheckboxWidget getEraseCacheCheckbox();
	// ==================================================
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo callback)
	{
		//define fields
		TEMP_OWS_RUC = false; //HIGHLY IMPORTANT TO RESET THE FLAG - PREVENTS POTENTIAL CORRUPTIONS AND ERRORS
		final int i =  getEraseCacheCheckbox().y, j = getEraseCacheCheckbox().getHeight() + 5;
		
		//define the check-box
		nounusedchunks_removeUnusedChunksCheckbox = new ActionCheckboxWidget(
				this.width / 2 - 155 + 80, i + j, //x and y
				250, 20, //width and height
				Text.translatable("nounusedchunks.backupprompt.removeuninhabitedchunks"), //text
				TEMP_OWS_RUC, //current value
				true, //show message
				checkbox -> TEMP_OWS_RUC = checkbox.isChecked()); //on-click
		
		nounusedchunks_removeUnusedChunksCheckbox.setTooltip(
				Text.translatable("nounusedchunks.backupprompt.removeuninhabitedchunks.tooltip"));
		
		//if showing the check-boxes, add the check-box below the
		//vanilla one, and move the buttons down
		if(getShowEraseCacheCheckbox())
		{
			//add the check-box
			addDrawableChild(nounusedchunks_removeUnusedChunksCheckbox);
			
			//move the stuff below it a bit down
			((AccessorScreen)(Object)this).getDrawables().forEach(drawable ->
			{
				//ignore non-clickables
				//ignore the removeUnusedChunks checkbox
				if(!(drawable instanceof ClickableWidget) || drawable == nounusedchunks_removeUnusedChunksCheckbox)
					return;
				ClickableWidget cw = (ClickableWidget)drawable;
				
				//ignore above ones
				if(cw.y < nounusedchunks_removeUnusedChunksCheckbox.y - 5)
					return;
				
				//move a bit down
				cw.y = cw.y + j + 10;
			});
		}
	}
	// ==================================================
}