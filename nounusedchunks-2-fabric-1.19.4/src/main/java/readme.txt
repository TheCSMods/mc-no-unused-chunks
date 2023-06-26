In `gradle.properties`, there's a property that looks like this:
maven_group = io.github.thecsdev

In `build.gradle`, the project has been configured to discard all `.class`
files outside of the `maven_group`. This means that packages such as
`net.minecraftforge` are only there during "compile-time", and not "run-time".
This allows the mod to be written in a way that allows it to interact with
Minecraft Forge, without Forge actually being present.

# Important node:
Because of this, all `.class` files created outside of the `maven_group` will
be discarded once the jar file is compiled aka built!