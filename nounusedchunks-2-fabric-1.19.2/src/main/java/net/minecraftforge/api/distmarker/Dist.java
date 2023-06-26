package net.minecraftforge.api.distmarker;

public class Dist
{
	public boolean isClient() { return isDedicatedServer(); }
	public boolean isDedicatedServer() { throw new AssertionError("Nope."); }
}