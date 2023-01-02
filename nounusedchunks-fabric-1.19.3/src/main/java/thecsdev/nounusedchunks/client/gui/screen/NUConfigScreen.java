package thecsdev.nounusedchunks.client.gui.screen;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.panel.TPanelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.util.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TDynamicSliderWidget;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TSelectEnumWidget;
import io.github.thecsdev.tcdcommons.api.util.ITextProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.MathHelper;
import thecsdev.nounusedchunks.config.NUCConfig;

public class NUConfigScreen extends TScreen
{
	// ==================================================
	public final Screen parent;
	// ==================================================
	public NUConfigScreen(Screen parent)
	{
		super(translatable("nounusedchunks.clothconfig.title"));
		this.parent = parent;
	}
	public enum EnumEnabled implements ITextProvider
	{
		ENABLED("addServer.resourcePack.enabled"),
		DISABLED("addServer.resourcePack.disabled");
		
		private final MutableText text;
		EnumEnabled(String translationKey) { this.text = translatable(translationKey); }
		public @Override MutableText getIText() { return text; }
		
		public boolean toBoolean() { return this == ENABLED; }
		public static EnumEnabled fromBoolean(boolean arg0) { return arg0 ? ENABLED : DISABLED; }
	}
	protected @Override void onClosed()
	{
		getClient().setScreen(parent);
		NUCConfig.saveProperties();
		super.onClosed();
	}
	// ==================================================
	protected @Override void init()
	{
		//define content pane dimensions
		int pW = (int) (getTpeWidth() / 2), pH = 20*2 + 5 + 10*2;
		int pX = (getTpeWidth() / 2) - (pW / 2), pY = (getTpeHeight() / 2) - (pH / 2);
		
		//create and add content pane
		TPanelElement contentPane = new TPanelElement(pX, pY, pW, pH);
		contentPane.setScrollPadding(10);
		addTChild(contentPane, false);
		
		//header
		{
			var panel_header = new TPanelElement(pX, pY - 25, pW, 20);
			panel_header.setScrollPadding(0);
			addTChild(panel_header, false);
			
			var lbl_header = new TLabelElement(0, 0, pW, 20);
			lbl_header.setText(getTitle());
			lbl_header.setHorizontalAlignment(HorizontalAlignment.CENTER);
			panel_header.addTChild(lbl_header, true);
		}
		
		//enabled
		{
			var lbl_enabled = new TLabelElement(10, 10, (pW / 2) - 20, 20);
			lbl_enabled.setText(translatable("addServer.resourcePack.enabled"));
			lbl_enabled.setTooltip(translatable("nounusedchunks.clothconfig.general.enabled.tooltip"));
			contentPane.addTChild(lbl_enabled, true);
			
			var sel_enabled = new TSelectEnumWidget<EnumEnabled>(
					lbl_enabled.getTpeEndX() + 20, lbl_enabled.getTpeY(),
					lbl_enabled.getTpeWidth(), lbl_enabled.getTpeHeight(),
					EnumEnabled.class, EnumEnabled.fromBoolean(NUCConfig.ENABLED));
			sel_enabled.setTooltip(translatable("nounusedchunks.clothconfig.general.enabled.tooltip"));
			sel_enabled.setEnumValueToLabel(value -> ((EnumEnabled)value).getIText());
			sel_enabled.setOnSelectionChange(newValue -> NUCConfig.ENABLED = ((EnumEnabled)newValue).toBoolean());
			contentPane.addTChild(sel_enabled, false);
		}
		
		//un-flag chance
		{
			var lbl_ufc = new TLabelElement(10, 35, (pW / 2) - 20, 20);
			lbl_ufc.setText(translatable("nounusedchunks.clothconfig.general.unflag_chance"));
			lbl_ufc.setTooltip(translatable("nounusedchunks.clothconfig.general.unflag_chance.tooltip"));
			contentPane.addTChild(lbl_ufc, true);
			
			var slide_ufc = new TDynamicSliderWidget(
					lbl_ufc.getTpeEndX() + 20, lbl_ufc.getTpeY(),
					lbl_ufc.getTpeWidth(), lbl_ufc.getTpeHeight(), ((double)NUCConfig.UNFLAG_CHANCE) / 100,
					__slider -> NUCConfig.UNFLAG_CHANCE = MathHelper.clamp((int)(__slider.getValue() * 100), 0, 100));
			slide_ufc.setTooltip(translatable("nounusedchunks.clothconfig.general.unflag_chance.tooltip"));
			contentPane.addTChild(slide_ufc, false);
		}
	}
	// ==================================================
}