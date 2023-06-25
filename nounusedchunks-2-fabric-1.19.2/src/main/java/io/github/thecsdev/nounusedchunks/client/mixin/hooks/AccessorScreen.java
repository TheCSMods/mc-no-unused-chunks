package io.github.thecsdev.nounusedchunks.client.mixin.hooks;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public interface AccessorScreen
{
	@Accessor("drawables")
	public abstract List<Drawable> getDrawables();
}