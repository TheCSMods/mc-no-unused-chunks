## No unused chunks
This Minecraft mod reduces world size by discarding unused chunks.
 
<ins><b>Important</b></ins>: Please back-up your world if you are planning on using this mod on an already existing world.
This mod shouldn't cause any issues, but it is still a good idea to back-up worlds, just in case.

### How to use this mod:
1. Go to the world selection screen
2. Select a world of your choice
3. Click on the "Edit" button
4. Click on the "Optimize World" button
5. There should be a checkbox called "Remove uninhabited chunks". Check it it.
6. Click on "Create backup and Load". This will back-up your world just in case something goes wrong.
7. That's it. It's best to also verify everything went to plan and that no data loss took place.

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

### Important notice:
- The game keeps track of which chunks players have interacted with using a special variable called "InhabitedTime". Whenever a player enters a chunk or does something to a chunk, the value of "InhabitedTime" increases. The way this feature works is by going through every chunk in every region file of a given world, and removing all chunks whose "InhabitedTime" value is set to "0". This means that any and all chunks whose "InhabitedTime" is "0" will get removed. It is important to keep in mind that if you used a creative tool/mod/program to modify chunks without ever entering them as an in-game player and making changes to them manually, or if the game fails to keep track of "InhabitedTime", there is a high chance those chunks will get removed anyways. Always back up your worlds before doing this, and immediately make sure no chunks you needed somehow got removed.
- If there are any issues/bugs or if you do spot this mod's feature removing chunks it isn't suppoed to remove, please let me know so I can see if something can be done to resolve it.

##
<p align=center>
  <a href="https://www.curseforge.com/minecraft/mc-mods/no-unused-chunks"><img alt="CurseForge" src="https://cf.way2muchnoise.eu/645755.svg"/></a>
  <a href="https://modrinth.com/mod/U8avpWmO"><img alt="Modrinth" src="https://img.shields.io/modrinth/dt/U8avpWmO?label=Modrinth"></a>
</p>
