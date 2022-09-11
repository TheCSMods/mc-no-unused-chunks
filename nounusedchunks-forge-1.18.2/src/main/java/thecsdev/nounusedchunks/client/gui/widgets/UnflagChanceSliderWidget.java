package thecsdev.nounusedchunks.client.gui.widgets;

import static thecsdev.nounusedchunks.NoUnusedChunks.lt;

import net.minecraft.client.gui.components.AbstractSliderButton;
import thecsdev.nounusedchunks.config.NUCConfig;

public class UnflagChanceSliderWidget extends AbstractSliderButton
{
	public UnflagChanceSliderWidget(int x, int y, int w, int h)
	{
		super(x, y, w, h, lt("N/A"), (float)NUCConfig.UNFLAG_CHANCE / 100);
		updateMessage();
	}

	@Override protected void updateMessage() { setMessage(lt(valInt() + "%")); }
	@Override protected void applyValue() { NUCConfig.UNFLAG_CHANCE = valInt(); }
	
	private int valInt() { return clamp((int) (this.value * 100), 0, 100); }
	private static int clamp(int i, int min, int max) { return Math.max(Math.min(i, max), 0); }
}