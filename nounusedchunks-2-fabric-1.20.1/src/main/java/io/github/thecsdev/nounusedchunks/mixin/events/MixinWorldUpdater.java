package io.github.thecsdev.nounusedchunks.mixin.events;

import static io.github.thecsdev.nounusedchunks.NoUnusedChunksConfig.TEMP_OWS_RUC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFixer;

import io.github.thecsdev.nounusedchunks.NoUnusedChunks;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import net.minecraft.world.updater.WorldUpdater;

@Mixin(value = WorldUpdater.class, priority = 1987 /*biting unused chunks, and turning them into bits*/)
public abstract class MixinWorldUpdater
{
	@Shadow public static Logger LOGGER;
	@Shadow public Registry<DimensionOptions> dimensionOptionsRegistry;
	@Shadow public Set<RegistryKey<World>> worldKeys;
	@Shadow public boolean eraseCache;
	@Shadow public LevelStorage.Session session;
	@Shadow public DataFixer dataFixer;
	@Shadow public boolean keepUpgradingChunks/* = true*/;
	@Shadow public boolean done;
	@Shadow public float progress;
	@Shadow public int totalChunkCount;
	@Shadow public int upgradedChunkCount;
	@Shadow public int skippedChunkCount;
	@Shadow public Object2FloatMap<RegistryKey<World>> dimensionProgress;
	@Shadow public Text status/* = (Text)Text.translatable("optimizeWorld.stage.counting")*/;
	@Shadow public PersistentStateManager persistentStateManager;

	@Shadow public abstract List<ChunkPos> getChunkPositions(RegistryKey<World> world);
	
	@Inject(method = "updateWorld", at = @At("HEAD"), cancellable = true)
	public void onUpdateWorld(CallbackInfo callback)
	{
		//if no OW_RUC, then ignore this process
		if(!TEMP_OWS_RUC) return;
		//else cancel the vanilla behavior, and use chocolate (aka this)
		callback.cancel();
		
		// ----------------------------- OW & RUC -----------------------------
		LOGGER.info("Using \"" + NoUnusedChunks.getModID() + "'s\" WorldUpdater and optimization system.");
		
		//retrieve chunk positions and a total count of chunks
		totalChunkCount = 0;
		ImmutableMap.Builder<RegistryKey<World>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
		for (RegistryKey<World> registryKey : worldKeys)
		{
		    List<ChunkPos> list = getChunkPositions(registryKey);
		    builder.put(registryKey, list.listIterator());
		    this.totalChunkCount += list.size();
		}
		
		//if there are no chunks, return
		if (this.totalChunkCount == 0)
		{
		    this.done = true;
		    return;
		}
		
		//retrieve chunk regions
		final float totalChunkCountF = this.totalChunkCount;
		ImmutableMap<RegistryKey<World>, ListIterator<ChunkPos>> immutableMap = builder.build();
		ImmutableMap.Builder<RegistryKey<World>, VersionedChunkStorage> worldRegionFilesBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<RegistryKey<World>, VersionedChunkStorage> __worldTempRegionFilesBuilder = ImmutableMap.builder();
		for (RegistryKey<World> registryKey2 : worldKeys)
		{
		    Path path = this.session.getWorldDirectory(registryKey2);
		    worldRegionFilesBuilder.put(registryKey2, new VersionedChunkStorage(path.resolve("region"), this.dataFixer, true));
		    __worldTempRegionFilesBuilder.put(registryKey2, new VersionedChunkStorage(path
		    		.resolve("region").resolve(NoUnusedChunks.getModID() + "_temp"), this.dataFixer, true));
		}
		ImmutableMap<RegistryKey<World>, VersionedChunkStorage> worldRegionFiles = worldRegionFilesBuilder.build();
		ImmutableMap<RegistryKey<World>, VersionedChunkStorage> __worldTempRegionFiles = __worldTempRegionFilesBuilder.build();
		
		//now for the upgrading process
		long measiringTime = Util.getMeasuringTimeMs(); //used to measure the time it took to complete all this
		this.status = Text.translatable("optimizeWorld.stage.upgrading");
		while (this.keepUpgradingChunks) //keep iterating and upgrading chunks
		{
			//flags
		    boolean willKeepUpgradingChunks = false;
		    float localProgress = 0.0F;
		    
		    //iterate RegistryKey<World>-s
		    for (RegistryKey<World> worldRegKey : this.worldKeys)
		    {
		        ListIterator<ChunkPos> worldChunkIterator = (ListIterator<ChunkPos>) immutableMap.get(worldRegKey);
		        VersionedChunkStorage versionedChunkStorage = (VersionedChunkStorage) worldRegionFiles.get(worldRegKey);
		        VersionedChunkStorage __vcsTemp = __worldTempRegionFiles.get(worldRegKey);
		        
		        if (worldChunkIterator.hasNext())
		        {
		            ChunkPos chunkPos = worldChunkIterator.next();
		            boolean willChunkNotSkip = false;
		            try
		            {
		                NbtCompound oldChunkNbt = versionedChunkStorage.getNbt(chunkPos).join().orElse(null);
		                
		                if (oldChunkNbt != null)
		                {
		                    int chunkDataVersion = VersionedChunkStorage.getDataVersion(oldChunkNbt);
		                    ChunkGenerator chunkGenerator = ((DimensionOptions)dimensionOptionsRegistry.getOrThrow(RegistryKeys.toDimensionKey(worldRegKey))).chunkGenerator();
		                    NbtCompound newChunkNbt = versionedChunkStorage.updateChunkNbt(worldRegKey, () -> this.persistentStateManager, oldChunkNbt, chunkGenerator.getCodecKey());
		                    ChunkPos chunkPos2 = new ChunkPos(newChunkNbt.getInt("xPos"), newChunkNbt.getInt("zPos"));
		                    
		                    if (!chunkPos2.equals(chunkPos))
		                        LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPos2);
		                    
		                    boolean chunkChangesMade = (chunkDataVersion < SharedConstants.getGameVersion().getSaveVersion().getId());
		                    boolean chunkIsUnused = NoUnusedChunks.isChunkUnused(newChunkNbt);
		                    
		                    //erasing chunk cache (vanilla behavior)
		                    if (this.eraseCache && !chunkIsUnused)
		                    {
		                    	//erase heightmaps, lighting, and isLightOn
		                        chunkChangesMade = (chunkChangesMade || newChunkNbt.contains("Heightmaps"));
		                        chunkChangesMade = (chunkChangesMade || newChunkNbt.contains("isLightOn"));
		                        newChunkNbt.remove("Heightmaps");
		                        newChunkNbt.remove("isLightOn");
		                        
		                        //erase chunk section cache
		                        NbtList nbtList = newChunkNbt.getList("sections", 10);
		                        for (int j = 0; j < nbtList.size(); j++)
		                        {
		                            NbtCompound nbtCompound3 = nbtList.getCompound(j);
		                            chunkChangesMade = (chunkChangesMade || nbtCompound3.contains("BlockLight"));
		                            chunkChangesMade = (chunkChangesMade || nbtCompound3.contains("SkyLight"));
		                            nbtCompound3.remove("BlockLight");
		                            nbtCompound3.remove("SkyLight");
		                        }
		                    }
		                    //erasing unused chunk data if chunk is unused
		                    else if(chunkIsUnused)
		                    {
		                    	newChunkNbt = null;
		                    	chunkChangesMade = true;
		                    }
		                    
		                    //save changes
		                    if(!chunkIsUnused) __vcsTemp.setNbt(chunkPos, newChunkNbt);
		                    if (chunkChangesMade)
		                    {
		                        //versionedChunkStorage.setNbt(chunkPos, newChunkNbt); -- already stored in __vcsTemp
		                        willChunkNotSkip = true;
		                    }
		                }
		            }
		            catch (CrashException | java.util.concurrent.CompletionException runtimeException)
		            {
		                Throwable throwable = runtimeException.getCause();
		                if (throwable instanceof IOException)
		                	LOGGER.error("Error upgrading chunk {}", chunkPos, throwable);
		                else
		                	throw runtimeException;
		            }
		            
		            if (willChunkNotSkip) this.upgradedChunkCount++;
		            else this.skippedChunkCount++;
		            willKeepUpgradingChunks = true;
		        }
		        
		        float h = worldChunkIterator.nextIndex() / totalChunkCountF;
		        this.dimensionProgress.put(worldRegKey, h);
		        localProgress += h;
		    }
		    
		    this.progress = localProgress;
		    if (!willKeepUpgradingChunks) this.keepUpgradingChunks = false;
		}
		
		//finishing...
		this.status = Text.translatable("optimizeWorld.stage.finished");
		worldRegionFiles.forEach((wrKey, vcsValue) ->
		{
			try
			{
				//close the region files
				vcsValue.close();
				__worldTempRegionFiles.get(wrKey).close();
				
				//move the temp. region files to override the old ones
				Path worldRegDir = this.session.getWorldDirectory(wrKey).resolve("region");
				Path worldRegTempDir = worldRegDir.resolve(NoUnusedChunks.getModID() + "_temp");
				for(File wrtdChild : worldRegTempDir.toFile().listFiles())
				{
					Files.move(
							wrtdChild.toPath(),
							worldRegDir.resolve(wrtdChild.getName()),
							StandardCopyOption.REPLACE_EXISTING);
				}
				worldRegTempDir.toFile().delete();
			}
		    catch (IOException iOException) { LOGGER.error("Error upgrading chunk", iOException); }
			catch (NullPointerException e) {}
		});
		this.persistentStateManager.save();
		
		//finish measuring, and log the time. then mark 'done' as true
		measiringTime = Util.getMeasuringTimeMs() - measiringTime;
		LOGGER.info("World optimizaton finished after {} ms", Long.valueOf(measiringTime));
		this.done = true;
		
		//clear temp. config variables.
		TEMP_OWS_RUC = false;
	}
}