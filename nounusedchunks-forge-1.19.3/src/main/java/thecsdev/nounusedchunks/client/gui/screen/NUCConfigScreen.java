package thecsdev.nounusedchunks.client.gui.screen;

import static thecsdev.nounusedchunks.NoUnusedChunks.lt;
import static thecsdev.nounusedchunks.NoUnusedChunks.tt;

import java.awt.Color;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.client.gui.widgets.StringWidget;
import thecsdev.nounusedchunks.client.gui.widgets.UnflagChanceSliderWidget;
import thecsdev.nounusedchunks.config.NUCConfig;

public class NUCConfigScreen extends Screen
{
	// ==================================================
	public static final int BG_COLOR = new Color(0, 0, 0, 120).getRGB();
	public static final int OL_COLOR = new Color(255, 255, 255, 50).getRGB();
	// --------------------------------------------------
	public final ModListScreen parent;
	private Button btn_enabled;
	private UnflagChanceSliderWidget slider_ufc;
	private Button btn_done;
	// --------------------------------------------------
	private int x1, y1, x2, y2, y3;
	// ==================================================
	public NUCConfigScreen(ModListScreen parent)
	{
		super(NoUnusedChunks.tt("nounusedchunks.config.title"));
		this.parent = parent;
	}
	// --------------------------------------------------
	@Nullable public Button getBtnEnabled() { return btn_enabled; }
	@Nullable public Button getBtnDone() { return btn_done; }
	@Nullable public UnflagChanceSliderWidget getSliderUfc() { return slider_ufc; }
	// --------------------------------------------------
	@Override
	public void onClose()
	{
		this.minecraft.setScreen(parent);
		NUCConfig.saveProperties();
	}
	// ==================================================
	@Override
	protected void init()
	{
		//super
		super.init();
		
		//calculate bg coordinates
		this.x1 = 10;
		this.y1 = 10;
		this.x2 = this.width - 10;
		this.y2 = this.height - 45;
		this.y3 = this.height - 40;
		
		//category
		this.addRenderableWidget(new StringWidget(this.width / 2, y1 + 15, tt("nounusedchunks.config.general"), font)
				.setCentered());
		
		//enabled
		this.addRenderableWidget(new StringWidget(x1 + 10, y1 + 30, tt("nounusedchunks.config.general.enabled"), font)
				.setTooltip(tt("nounusedchunks.config.general.enabled.tooltip")));
		this.addRenderableWidget(btn_enabled = Button.builder(lt(Boolean.toString(NUCConfig.ENABLED)), arg0 ->
			{
				NUCConfig.ENABLED = !NUCConfig.ENABLED;
				arg0.setMessage(lt(Boolean.toString(NUCConfig.ENABLED)));
			})
			.bounds(x2 - 160, y1 + 25, 150, 20)
			.build()
		);
		
		//un-flag chance
		this.addRenderableWidget(new StringWidget(x1 + 10, y1 + 55, tt("nounusedchunks.config.general.unflag_chance"), font)
				.setTooltip(tt("nounusedchunks.config.general.unflag_chance.tooltip")));
		this.addRenderableWidget(slider_ufc = new UnflagChanceSliderWidget(btn_enabled.getX(), btn_enabled.getY() + 25, btn_enabled.getWidth(), 20));
		
		//done
		this.addRenderableWidget(btn_done = Button.builder(tt("gui.done"), arg0 -> onClose()).bounds((this.width / 2) - 75, y3 + 5, 150, 20).build());
	}
	// --------------------------------------------------
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta)
	{
		//background rendering
		renderBackground(matrices);
		fill(matrices, x1 - 1, y1 - 1, x2 + 1, y2 + 1, OL_COLOR);
		fill(matrices, x1, y1, x2, y2, BG_COLOR);
		//fill(matrices, x1, y3, x2, y4, BG_COLOR);
			
		//widget rendering
		super.render(matrices, mouseX, mouseY, delta);
		
		//draw tooltips
		this.children().forEach(child ->
		{
			if(child instanceof StringWidget && child.isMouseOver(mouseX, mouseY))
				((StringWidget)child).renderToolTip(matrices, mouseX, mouseY);
		});
	}
	// ==================================================
}