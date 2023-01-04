package thecsdev.nounusedchunks.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import thecsdev.nounusedchunks.client.gui.util.GuiUtils;

public class StringWidget extends AbstractWidget
{
	// ==================================================
	public final Font font;
	public boolean isCentered = false;
	public int color = -1;
	public Component tooltip;
	// --------------------------------------------------
	public StringWidget(int x, int y, Component text, Font font)
	{
		super(x, y - (font.lineHeight / 2), font.width(text.getString()), font.lineHeight, text);
		this.font = font;
	}
	public StringWidget setCentered() { isCentered = true; return this; }
	public StringWidget setColored(int color) { this.color = color; return this; }
	public StringWidget setTooltip(Component tooltip) { this.tooltip = tooltip; return this; }
	// ==================================================
	protected @Override void updateWidgetNarration(NarrationElementOutput neo) { neo.add(NarratedElementType.TITLE, getMessage()); }
	protected @Override boolean clicked(double p_93681_, double p_93682_) { return false; }
	// --------------------------------------------------
	public void renderToolTip(PoseStack matrices, int mouseX, int mouseY)
	{
		if(isHovered && tooltip != null)
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, tooltip);
	}
	// --------------------------------------------------
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta)
	{
		int x = getX(), y = getY();
		this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		
		if(!isCentered) drawString(matrices, font, getMessage(), x, y, color);
		else drawCenteredString(matrices, font, getMessage(), x, y, color);
	}
	// ==================================================
}