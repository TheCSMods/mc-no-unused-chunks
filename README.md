## No unused chunks
This Minecraft mod reduces world size by discarding unused chunks.
In other words, any newly generated chunks that the player or an entity or a block hasn't interacted with will not be saved, and that will therefore reduce the world size. 
 
<ins><b>Important</b></ins>: Please back-up your world if you are planning on using this mod on an already existing world.
This mod shouldn't cause any issues, but it is still a good idea to back-up worlds, just in case.

## How does the mod work?
Every chunk in a world has a special flag called "needs saving". The game uses this flag to keep track of whether any changes were made to a chunk. When this flag is set to "true", the game will then save the chunk the moment it gets unloaded. The game does this in order to save performance by avoiding saving chunks that had no changes made to them. 
 
As soon as the game generates a new chunk, that chunk is then immediately marked as "needs saving" by the game, causing the game to save those chunks the moment they get unloaded. This is very likely done to save performance by avoiding generating the same chunks every time a player comes in their range. 
 
This mod overrides the rule above, and unflags newly generated chunks as "needs saving" anyways, causing them to not be saved once they get unloaded, unless a change is made to those chunks. This ends up shrinking the world size at the cost of performance, because the next time a player comes near those chunks, they will have to be generated again.

## How well does the mod do?
I performed a few tests in Minecraft 1.18.2 by generating an 18x18 chunk area with 18 render distance in a few worlds with and without the mod. I then measured the world sizes after each chunk generation to see what the world size was after generating was done.
```cs
------------------------------------------------
# Default (18x18) (100% unflag) (Seed: -4665624743998437880)
- Without mod: ~23.9 MB
- With mod:    ~14.4 MB
- Saved:       ~9.5  MB (-39.75%)
------------------------------------------------
# Default (18x18) (100% unflag) (Seed: 2419730229291416458)
- Without mod: ~24   MB
- With mod:    ~14.4 MB
- Saved:       ~9.6  MB (-40%)
------------------------------------------------
# Superflat (18x18) (no features) (no entities) (100% unflag) (Seed: idk)
- Without mod: ~15.5 MB
- With mod:    ~6.25 MB
- Saved:       ~9.25 MB (-59.7%)
------------------------------------------------
# Amplified (18x18) (100% unflag) (Seed: -2757405298360622893)
- Without mod: ~25.5 MB
- With mod:    ~15.8 MB
- Saved:       ~9.7  MB (-38.04%)
------------------------------------------------
```

## The trade-off
Doing what the mod does has it's own trade-offs, the main one being performance. By not saving newly generated chunks, they will have to be re-generated every time a player comes near them, or whenever the world is loaded once again. Generating chunks of course costs performance and takes time for the game to do, and the higher the render distance, the more performance it will cost. 
 
It is because of this trade-off that the `UNFLAG_CHANCE` config property was introduced into the mod. This property defines the percentage of chunks that will be affected by the mod. By default, 100% of newly generated chunks are unflagged. Decrease this value to increase the world generation performance, but keep in mind that that will increase the world size as well.

## Can the mod remove unused chunks generated before the mod was installed or while the mod was disabled?
**Edit:** As of v1.2, it finally can, but it is very important that you read the important notice.

### Important notice:
- The game keeps track of which chunks players have interacted with using a special variable called "InhabitedTime". Whenever a player enters a chunk or does something to a chunk, the value of "InhabitedTime" increases. The way this feature works is by going through every chunk in every region file of a given world, and removing all chunks whose "InhabitedTime" value is set to "0". This means that any and all chunks whose "InhabitedTime" is "0" will get removed. It is important to keep in mind that if you used a creative tool/mod/program to modify chunks without ever entering them and making changes to them manually, or if the game fails to keep track of "InhabitedTime", there is a high chance those chunks will get removed anyways. Always back up your worlds before doing this, and immediately make sure no chunks you needed somehow got removed.
- If there are any issues/bugs or if you do spot this mod's feature removing chunks it isn't suppoed to remove, please let me know so I can see if something can be done to resolve it.

(For <v1.2) Unfortunately it can not. Any previously saved chunks will not be affected by the mod. Doing so would likely require the mod to mess with the world files by detecting and removing unused chunks from there, which I do not know how do to.

## Configuring the mod
There are several ways to configure the mod. The main way of configuring it is by going into the `config` directory and creating a `nounusedchunks.properties` file.
Below is a list of properties you can use to configure the mod:
```properties
# ========== Properties for v1.0 and above ==========
# Whether the mod is enabled or not.
# When enabled, newly generated chunks that
# have been untouched will not be
# marked as 'needs saving'.
ENABLED=true

# (Advanced) (Range: 1 to 100 (percentage))
# Defines the chance a newly generated chunk will be unmarked
# as 'needs saving'. Lower chance means more newly generated
# chunks will immediately get saved like they would normally
# while saving performance at the cost of drive storage, while
# higher values will make the mod unmark more newly generated
# chunks as 'needs saving' and thus saving more storage at the
# cost of performance.
UNFLAG_CHANCE=100
```

### Mod Menu and Cloth Config API
The easier way of configuring the mod is to install the [Mod Menu](https://www.curseforge.com/minecraft/mc-mods/modmenu) and the [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) mods, and then use those mods to configure this mod in a settings menu while the game is running.

##
<p align=center>
  <a href="https://www.curseforge.com/minecraft/mc-mods/no-unused-chunks"><img alt="CurseForge" src="https://cf.way2muchnoise.eu/645755.svg"/></a>
  <a href="https://modrinth.com/mod/U8avpWmO"><img alt="Modrinth" src="https://img.shields.io/modrinth/dt/U8avpWmO?label=Modrinth"></a>
</p>
