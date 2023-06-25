package io.github.thecsdev.nounusedchunks.client.gui.widgets;

import java.util.function.Consumer;

import io.github.thecsdev.nounusedchunks.client.gui.util.GuiUtils;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ActionCheckboxWidget extends CheckboxWidget
{
	// --------------------------------------------------
	public final Consumer<ActionCheckboxWidget> callback;
	public Text tooltip;
	// --------------------------------------------------
	public ActionCheckboxWidget(
			int x, int y, int width, int height,
			Text message, boolean checked, boolean showMessage,
			Consumer<ActionCheckboxWidget> callback)
	{
		super(x, y, width, height, message, checked, showMessage);
		this.callback = callback;
	}
	public void setTooltip(Text text) { this.tooltip = text; }
	// --------------------------------------------------
	public @Override void onPress()
	{
		super.onPress();
		if(this.callback != null)
			this.callback.accept(this);
	}
	
	public @Override void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		super.render(matrices, mouseX, mouseY, delta);
		if(isHovered())
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, delta, tooltip);
	}
	// --------------------------------------------------
}