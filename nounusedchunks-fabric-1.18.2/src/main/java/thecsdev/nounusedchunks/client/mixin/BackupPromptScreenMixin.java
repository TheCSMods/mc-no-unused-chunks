package thecsdev.nounusedchunks.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(BackupPromptScreen.class)
public abstract class BackupPromptScreenMixin extends Screen
{
	// ==================================================
	protected BackupPromptScreenMixin(Text title) { super(title); }
	// ==================================================
	public CheckboxWidget removeUnusedChunksCheckbox;
	// --------------------------------------------------
	@Accessor("showEraseCacheCheckbox")
	public abstract boolean getShowEraseCacheCheckbox();
	
	@Accessor("eraseCacheCheckbox")
	public abstract CheckboxWidget getEraseCacheCheckbox();
	// ==================================================
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo callback)
	{
		//define the check-box
		int i = getEraseCacheCheckbox().y, j = getEraseCacheCheckbox().getHeight() + 5;
		removeUnusedChunksCheckbox = new CheckboxWidget(
				this.width / 2 - 155 + 80, i + j,
				150, 20,
				new TranslatableText("nounusedchunks.backupprompt.removeunusedchunks"), false);
		
		//if showing the check-boxes, add the check-box below the
		//vanilla one, and move the buttons down
		if(getShowEraseCacheCheckbox())
		{
			//add the check-box
			addDrawableChild(removeUnusedChunksCheckbox);
			
			//move the stuff below it a bit down
			((ScreenMixin)(Object)this).getDrawables().forEach(drawable ->
			{
				//ignore non-clickables
				//ignore the removeUnusedChunks checkbox
				if(!(drawable instanceof ClickableWidget) || drawable == removeUnusedChunksCheckbox)
					return;
				ClickableWidget cw = (ClickableWidget)drawable;
				
				//ignore above ones
				if(cw.y < removeUnusedChunksCheckbox.y - 5)
					return;
				
				//move a bit down
				cw.y += j + 10;
			});
		}
	}
	// ==================================================
}
