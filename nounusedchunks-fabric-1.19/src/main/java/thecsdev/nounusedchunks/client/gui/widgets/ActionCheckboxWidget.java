package thecsdev.nounusedchunks.client.gui.widgets;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.nounusedchunks.client.gui.util.GuiUtils;

public class ActionCheckboxWidget extends CheckboxWidget
{
	// --------------------------------------------------
	public interface ACCallback { public void onPress(ActionCheckboxWidget checkbox); }
	public final ACCallback callback;
	public Text tooltip;
	// --------------------------------------------------
	public ActionCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked, boolean showMessage, ACCallback callback)
	{
		super(x, y, width, height, message, checked, showMessage);
		this.callback = callback;
	}

	public void setTooltip(Text text) { this.tooltip = text; }
	// --------------------------------------------------
	@Override
	public void onPress()
	{
		super.onPress();
		if(callback != null)
			callback.onPress(this);
	}
	

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		super.render(matrices, mouseX, mouseY, delta);
		if(isHovered())
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, delta, tooltip);
	}
	// --------------------------------------------------
}
