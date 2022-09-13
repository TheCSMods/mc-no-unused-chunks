package thecsdev.nounusedchunks.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import thecsdev.nounusedchunks.client.gui.util.GuiUtils;

public class ActionCheckboxWidget extends Checkbox
{
	// --------------------------------------------------
	public interface ACCallback { public void onPress(ActionCheckboxWidget checkbox); }
	public final ACCallback callback;
	public final Font tr;
	public Component tooltip;
	// --------------------------------------------------
	@SuppressWarnings("resource")
	public ActionCheckboxWidget(int x, int y, int width, int height, Component message, boolean checked, boolean showMessage, ACCallback callback)
	{
		super(x, y, width, height, message, checked, showMessage);
		this.callback = callback;
		this.tr = Minecraft.getInstance().font;
	}
	
	public void setTooltip(Component text) { this.tooltip = text; }
	// --------------------------------------------------
	@Override
	public void onPress()
	{
		super.onPress();
		if(callback != null)
			callback.onPress(this);
	}
	
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta)
	{
		super.render(matrices, mouseX, mouseY, delta);
		if(this.isHovered)
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, tooltip);
	}
	// --------------------------------------------------
}
