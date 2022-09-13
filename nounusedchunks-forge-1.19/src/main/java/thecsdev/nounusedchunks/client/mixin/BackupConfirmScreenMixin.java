package thecsdev.nounusedchunks.client.mixin;

import static thecsdev.nounusedchunks.NoUnusedChunks.tt;
import static thecsdev.nounusedchunks.config.NUCConfig.OW_RUC;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import thecsdev.nounusedchunks.client.gui.widgets.ActionCheckboxWidget;

@Mixin(BackupConfirmScreen.class)
public abstract class BackupConfirmScreenMixin extends Screen
{
	// ==================================================
	public BackupConfirmScreenMixin(Component title) { super(title); }
	// ==================================================
	public ActionCheckboxWidget nuc_removeUnusedChunksCheckbox;
	//public ActionCheckboxWidget nuc_removeLightCacheCheckbox; -- Obsolete as of forge-1.19.x
	// --------------------------------------------------
	@Accessor("promptForCacheErase")
	public abstract boolean getPromptForCacheErase();
	
	@Accessor("eraseCache")
	public abstract Checkbox getEraseCache();
	
	@Accessor("listener")
	public abstract BackupConfirmScreen.Listener getListener();
	// ==================================================
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo callback)
	{
		//define the check-box
		int i = getEraseCache().y, j = getEraseCache().getHeight() + 5;
		
		/*nuc_removeLightCacheCheckbox = new ActionCheckboxWidget(
				this.width / 2 - 155 + 80, i + j,
				150, 20,
				tt("nounusedchunks.backupprompt.removelightcache"), false, true,
				checkbox -> {});
		nuc_removeLightCacheCheckbox.setTooltip(tt("nounusedchunks.backupprompt.removelightcache.tooltip"));
		nuc_removeLightCacheCheckbox.active = false;*/
		
		nuc_removeUnusedChunksCheckbox = new ActionCheckboxWidget(
				this.width / 2 - 155 + 80, i + j,
				150, 20,
				tt("nounusedchunks.backupprompt.removeuninhabitedchunks"), OW_RUC, true,
				checkbox -> OW_RUC = checkbox.selected());
		nuc_removeUnusedChunksCheckbox.setTooltip(tt("nounusedchunks.backupprompt.removeuninhabitedchunks.tooltip"));
		
		//if the game is showing the check-boxes, add the check-box below the
		//vanilla one, and move the buttons down
		if(getPromptForCacheErase())
		{
			//add the check-box
			addRenderableWidget(nuc_removeUnusedChunksCheckbox);
			//addRenderableWidget(nuc_removeLightCacheCheckbox);
			
			//move the stuff such as buttons and other UI elements below it a little bit down
			this.children().forEach(drawable ->
			{
				//ignore non-clickables,
				//ignore the removeUnusedChunks checkbox
				if(!(drawable instanceof AbstractButton) ||
						drawable == nuc_removeUnusedChunksCheckbox /*|| drawable == nuc_removeLightCacheCheckbox*/)
					return;
				AbstractButton cw = (AbstractButton)drawable;
				
				//ignore above ones
				if(cw.y < nuc_removeUnusedChunksCheckbox.y - 5)
					return;
				
				//move a bit down
				cw.y += j + 10 /*+ nuc_removeUnusedChunksCheckbox.getHeight() + 5*/;
			});
		}
		
		//now position the other checkbox (edit: not anymore)
		//nuc_removeUnusedChunksCheckbox.y = nuc_removeLightCacheCheckbox.y + nuc_removeLightCacheCheckbox.getHeight() + 5;
	}
	// ==================================================
}