package thecsdev.nounusedchunks.client.mixin;

import static thecsdev.nounusedchunks.NoUnusedChunks.tt;
import static thecsdev.nounusedchunks.config.NUCConfig.OW_RLC;
import static thecsdev.nounusedchunks.config.NUCConfig.OW_RUC;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.BackupPromptScreen.Callback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import thecsdev.nounusedchunks.client.gui.widgets.ActionCheckboxWidget;

@Mixin(BackupPromptScreen.class)
public abstract class BackupPromptScreenMixin extends Screen
{
	// ==================================================
	protected BackupPromptScreenMixin(Text title) { super(title); }
	// ==================================================
	public ActionCheckboxWidget nuc_removeUnusedChunksCheckbox;
	public ActionCheckboxWidget nuc_removeLightCacheCheckbox;
	// --------------------------------------------------
	@Accessor("showEraseCacheCheckbox")
	public abstract boolean getShowEraseCacheCheckbox();
	
	@Accessor("eraseCacheCheckbox")
	public abstract CheckboxWidget getEraseCacheCheckbox();
	
	@Accessor("callback")
	public abstract Callback getCallback();
	// ==================================================
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo callback)
	{
		//define the check-box-es
		int i = getEraseCacheCheckbox().y, j = getEraseCacheCheckbox().getHeight() + 5;
		
		nuc_removeLightCacheCheckbox = new ActionCheckboxWidget(
				this.width / 2 - 155 + 80, i + j,
				150, 20,
				tt("nounusedchunks.backupprompt.removelightcache"), OW_RLC, true,
				checkbox -> OW_RLC = checkbox.isChecked());
		nuc_removeLightCacheCheckbox.setTooltip(tt("nounusedchunks.backupprompt.removelightcache.tooltip"));
		
		nuc_removeUnusedChunksCheckbox = new ActionCheckboxWidget(
				this.width / 2 - 155 + 80, i + j,
				150, 20,
				tt("nounusedchunks.backupprompt.removeuninhabitedchunks"), OW_RUC, true,
				checkbox -> OW_RUC = checkbox.isChecked());
		nuc_removeUnusedChunksCheckbox.setTooltip(tt("nounusedchunks.backupprompt.removeuninhabitedchunks.tooltip"));
		
		//if the game is showing the check-boxes, add the check-box below the
		//vanilla one, and move the buttons down
		if(getShowEraseCacheCheckbox())
		{
			//add the check-box
			addButton(nuc_removeUnusedChunksCheckbox);
			addButton(nuc_removeLightCacheCheckbox);
			
			//move the stuff such as buttons and other UI elements below it a little bit down
			this.buttons.forEach(cw ->
			{
				//ignore the removeUnusedChunksCheckbox button
				if(cw == nuc_removeUnusedChunksCheckbox || cw == nuc_removeLightCacheCheckbox)
					return;
				
				//ignore above ones
				if(cw.y < nuc_removeUnusedChunksCheckbox.y - 5)
					return;
				
				//move a bit down
				cw.y += j + 10 + nuc_removeUnusedChunksCheckbox.getHeight() + 5;
			});
		}
		
		//now position the other checkbox
		nuc_removeUnusedChunksCheckbox.y = nuc_removeLightCacheCheckbox.y + nuc_removeLightCacheCheckbox.getHeight() + 5;
	}
	// ==================================================
}
