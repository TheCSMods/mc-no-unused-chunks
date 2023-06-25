package io.github.thecsdev.nounusedchunks.client.gui.widgets;

import java.util.function.Consumer;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

public class ActionCheckboxWidget extends CheckboxWidget
{
	// --------------------------------------------------
	public final Consumer<ActionCheckboxWidget> callback;
	// --------------------------------------------------
	public ActionCheckboxWidget(
			int x, int y, int width, int height,
			Text message, boolean checked, boolean showMessage,
			Consumer<ActionCheckboxWidget> callback)
	{
		super(x, y, width, height, message, checked, showMessage);
		this.callback = callback;
	}
	// --------------------------------------------------
	public @Override void onPress()
	{
		super.onPress();
		if(this.callback != null)
			this.callback.accept(this);
	}
	// --------------------------------------------------
}