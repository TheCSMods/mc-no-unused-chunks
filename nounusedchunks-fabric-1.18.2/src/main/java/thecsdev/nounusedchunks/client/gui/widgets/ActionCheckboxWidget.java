package thecsdev.nounusedchunks.client.gui.widgets;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

public class ActionCheckboxWidget extends CheckboxWidget
{
	// --------------------------------------------------
	public interface ACCallback { public void onPress(ActionCheckboxWidget checkbox); }
	public final ACCallback callback;
	// --------------------------------------------------
	public ActionCheckboxWidget(int x, int y, int width, int height, Text message, boolean checked, boolean showMessage, ACCallback callback)
	{
		super(x, y, width, height, message, checked, showMessage);
		this.callback = callback;
	}
	// --------------------------------------------------
	@Override
	public void onPress()
	{
		super.onPress();
		if(callback != null)
			callback.onPress(this);
	}
	// --------------------------------------------------
}
