package thecsdev.nounusedchunks.client.gui.util;

import static net.minecraft.client.gui.DrawableHelper.drawTextWithShadow;
import static net.minecraft.client.gui.DrawableHelper.fill;
import static thecsdev.nounusedchunks.NoUnusedChunks.lt;

import java.awt.Color;
import java.awt.Dimension;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.nounusedchunks.client.NoUnusedChunksClient;

public class GuiUtils
{
	// --------------------------------------------------
	public static final int COLOR_BLACK = Color.black.getRGB();
	public static final int COLOR_WHITE = Color.white.getRGB();
	public static final int COLOR_TOOLTIP_OUTLINE = new Color(27,0,62).getRGB();
	public static final int COLOR_TOOLTIP_BGROUND = new Color(15,0,15).getRGB();
	// --------------------------------------------------
	public static void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float delta, Text text)
	{
		MinecraftClient MC = NoUnusedChunksClient.MCClient;
		TextRenderer tr = MC.textRenderer;
		
		//get text size so we can know how big the tooltip will be
		Dimension textSize = getTextSize(tr, text);
		textSize = new Dimension(textSize.width + 10, textSize.height + 10);
		
		//offset the X for the tooltip
		mouseX += 5;
		if(MC.currentScreen != null)
		{
			int aX = mouseX + textSize.width;
			if(aX > MC.currentScreen.width)
				mouseX -= (textSize.width + 5);
		}
		
		//fill in a tooltip square background
		fill(matrices, mouseX, mouseY, mouseX + textSize.width, mouseY + textSize.height, COLOR_TOOLTIP_OUTLINE);
		fill(matrices, mouseX + 2, mouseY + 2, mouseX + textSize.width - 2, mouseY + textSize.height - 2, COLOR_TOOLTIP_BGROUND);
		
		//draw text
		String textStr = text.getString();
		String[] lines = textStr.split("(\\r?\\n)|(\\\\n)");
		
		int lineY = 0;
		for (String line : lines)
		{
			drawTextWithShadow(matrices, tr, lt(line), mouseX + 5, mouseY + 5 + lineY, COLOR_WHITE);
			lineY += tr.getWrappedLinesHeight(line, textSize.width);
		}
	}
	// --------------------------------------------------
	public static Dimension getTextSize(TextRenderer tr, Text text)
	{
		String textStr = text.getString();
		String[] lines = textStr.split("(\\r?\\n)|(\\\\n)");
		int maxWidth = 0;
		for (String line : lines)
		{
			int lineWidth = tr.getWidth(line);
			if(lineWidth > maxWidth)
				maxWidth = lineWidth;
		}
		return new Dimension(maxWidth, tr.getWrappedLinesHeight(textStr, maxWidth));
	}
	// --------------------------------------------------
}