package thecsdev.nounusedchunks.mixin;

import static thecsdev.nounusedchunks.config.NUCConfig.OW_RLC;
import static thecsdev.nounusedchunks.config.NUCConfig.OW_RUC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.DataFixer;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import net.minecraft.world.updater.WorldUpdater;
import thecsdev.nounusedchunks.NoUnusedChunks;

@Mixin(value = WorldUpdater.class, priority = 1001)
public abstract class WorldUpdaterMixin
{
	@Shadow public static Logger LOGGER;
	@Shadow public ImmutableSet<RegistryKey<World>> worlds;
	@Shadow public boolean eraseCache;
	@Shadow public LevelStorage.Session session;
	@Shadow public DataFixer dataFixer;
	@Shadow public volatile boolean keepUpgradingChunks;
	@Shadow public volatile boolean done;
	@Shadow public volatile float progress;
	@Shadow public volatile int totalChunkCount;
	@Shadow public volatile int upgradedChunkCount;
	@Shadow public volatile int skippedChunkCount;
	@Shadow public volatile Text status;
	@Shadow public PersistentStateManager persistentStateManager;
	@Shadow public Object2FloatMap<RegistryKey<World>> dimensionProgress;
	
	@Shadow public abstract List<ChunkPos> getChunkPositions(RegistryKey<World> world);
	
	@Inject(method = "updateWorld", at = @At("HEAD"), cancellable = true)
	public void onUpdateWorld(CallbackInfo callback)
	{
		//if no OW_RUC, then ignore this process
		if(!OW_RUC && !OW_RLC) return;
		callback.cancel(); //else cancel the vanilla behavior, and use chocolate
		
		// ----------------------------- OW & RUC -----------------------------
		LOGGER.log(Level.INFO, "Using \"" + NoUnusedChunks.ModID + "'s\" WorldUpdater and optimization system.");
		
		//retrieve chunk positions and a total count of chunks
		this.totalChunkCount = 0;
		ImmutableMap.Builder<RegistryKey<World>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
		ImmutableSet<RegistryKey<World>> immutableSet = this.worlds;
		for (UnmodifiableIterator<RegistryKey<World>> unmodifiableIterator1 = immutableSet.iterator(); unmodifiableIterator1.hasNext();)
		{
		    RegistryKey<World> registryKey = unmodifiableIterator1.next();
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
		float f = this.totalChunkCount;
		ImmutableMap<RegistryKey<World>, ListIterator<ChunkPos>> immutableMap = builder.build();
		ImmutableMap.Builder<RegistryKey<World>, VersionedChunkStorage> worldRegionFilesBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<RegistryKey<World>, VersionedChunkStorage> __worldTempRegionFilesBuilder = ImmutableMap.builder();
		for (UnmodifiableIterator<RegistryKey<World>> unmodifiableIterator2 = immutableSet.iterator(); unmodifiableIterator2.hasNext();)
		{
		    RegistryKey<World>registryKey2 = unmodifiableIterator2.next();
		    Path path = this.session.getWorldDirectory(registryKey2).toPath();
		    worldRegionFilesBuilder.put(registryKey2, new VersionedChunkStorage(path.resolve("region").toFile(), this.dataFixer, false));
		    
		    Path worldRegTempDir = path.resolve("region").resolve(NoUnusedChunks.ModID + "_temp");
		    try
		    {
		    	FileUtils.deleteDirectory(worldRegTempDir.toFile());
		    	__worldTempRegionFilesBuilder.put(registryKey2, new VersionedChunkStorage(worldRegTempDir.toFile(), this.dataFixer, true));
		    }
		    catch(IOException exc) {}
		}
		ImmutableMap<RegistryKey<World>, VersionedChunkStorage> worldRegionFiles = worldRegionFilesBuilder.build();
		ImmutableMap<RegistryKey<World>, VersionedChunkStorage> __worldTempRegionFiles = __worldTempRegionFilesBuilder.build();
		
		//now for the upgrading process
		long l = Util.getMeasuringTimeMs(); //used to measure the time it took to complete all this
		this.status = (Text) new TranslatableText("optimizeWorld.stage.upgrading");
		while (this.keepUpgradingChunks) //keep iterating and upgrading chunks
		{
			//flags
		    boolean willKeepUpgradingChunks = false;
		    float g = 0.0F;
		    
		    //iterate RegistryKey<World>-s
		    for (UnmodifiableIterator<RegistryKey<World>> worldIterator = immutableSet.iterator(); worldIterator.hasNext();)
		    {
		        RegistryKey<World> worldRegKey = worldIterator.next();
		        ListIterator<ChunkPos> worldChunkIterator = (ListIterator<ChunkPos>) immutableMap.get(worldRegKey);
		        VersionedChunkStorage versionedChunkStorage = (VersionedChunkStorage) worldRegionFiles.get(worldRegKey);
		        VersionedChunkStorage __vcsTemp = __worldTempRegionFiles.get(worldRegKey);
		        
		        if (worldChunkIterator.hasNext())
		        {
		            ChunkPos chunkPos = worldChunkIterator.next();
		            boolean willChunkNotSkip = false;
		            try
		            {
		                NbtCompound oldChunkNbt = null;
		                try { oldChunkNbt = versionedChunkStorage.getNbt(chunkPos); } catch (IOException ioExc) {}
		                
		                if (oldChunkNbt != null)
		                {
		                    int chunkDataVersion = VersionedChunkStorage.getDataVersion(oldChunkNbt);
		                    NbtCompound nbtCompound2 = versionedChunkStorage.updateChunkNbt(worldRegKey, () -> this.persistentStateManager, oldChunkNbt);
		                    NbtCompound nbtCompound3 = nbtCompound2.getCompound("Level");
		                    ChunkPos chunkPos2 = new ChunkPos(nbtCompound3.getInt("xPos"), nbtCompound3.getInt("zPos"));
		                    
		                    if (!chunkPos2.equals(chunkPos))
		                        LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPos2);
		                    
		                    boolean chunkChangesMade = (chunkDataVersion < SharedConstants.getGameVersion().getWorldVersion());
		                    boolean chunkIsUnused = OW_RUC && NoUnusedChunks.isChunkUnused(worldRegKey, chunkPos, nbtCompound3);
		                    
		                    //erasing chunk cache (vanilla behavior)
		                    if ((this.eraseCache || OW_RLC) && !chunkIsUnused)
		                    {
		                    	//erase heightmaps, lighting, and isLightOn
		                    	if(this.eraseCache)
		                    	{
			                        chunkChangesMade = (chunkChangesMade || nbtCompound3.contains("Heightmaps"));
			                        chunkChangesMade = (chunkChangesMade || nbtCompound3.contains("isLightOn"));
			                        nbtCompound3.remove("Heightmaps");
			                        nbtCompound3.remove("isLightOn");
		                    	}
		                        
		                        //erase chunk section cache
		                        if(OW_RLC)
		                        {
			                        NbtList nbtList = nbtCompound3.getList("Sections", 10);
			                        for (int j = 0; j < nbtList.size(); j++)
			                        {
			                            NbtCompound nbtCompound4 = nbtList.getCompound(j);
			                            chunkChangesMade = (chunkChangesMade || nbtCompound4.contains("BlockLight"));
			                            chunkChangesMade = (chunkChangesMade || nbtCompound4.contains("SkyLight"));
			                            nbtCompound4.remove("BlockLight");
			                            nbtCompound4.remove("SkyLight");
			                        }
		                        }
		                    }
		                    
		                    //erasing unused chunk data
		                    else if(chunkIsUnused)
		                    {
		                    	nbtCompound3 = null;
		                    	chunkChangesMade = true;
		                    }
		                    
		                    //save changes
		                    if(!chunkIsUnused) __vcsTemp.setNbt(chunkPos, nbtCompound2);
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
		        
		        float h = worldChunkIterator.nextIndex() / f;
		        this.dimensionProgress.put(worldRegKey, h);
		        g += h;
		    }
		    
		    this.progress = g;
		    if (!willKeepUpgradingChunks) this.keepUpgradingChunks = false;
		}
		
		//finishing...
		this.status = new TranslatableText("optimizeWorld.stage.finished");
		worldRegionFiles.forEach((wrKey, vcsValue) ->
		{
			try
			{
				//close the region files
				vcsValue.close();
				
				VersionedChunkStorage vcsTemp = __worldTempRegionFiles.get(wrKey);
				if(vcsTemp != null) vcsTemp.close();
				
				//move the temp. region files to override the old ones
				Path worldRegDir = this.session.getWorldDirectory(wrKey).toPath().resolve("region");
				Path worldRegTempDir = worldRegDir.resolve(NoUnusedChunks.ModID + "_temp");
				for(File wrtdChild : worldRegTempDir.toFile().listFiles())
				{
					Files.move(
							wrtdChild.toPath(),
							worldRegDir.resolve(wrtdChild.getName()),
							StandardCopyOption.REPLACE_EXISTING);
				}
				worldRegTempDir.toFile().deleteOnExit();
				FileUtils.deleteDirectory(worldRegTempDir.toFile());
			}
		    catch (IOException iOException) { LOGGER.error("Error upgrading chunk", iOException); }
			catch (NullPointerException e) {}
			catch (ConcurrentModificationException e) { /*Minecraft's issue...*/ }
		});
		this.persistentStateManager.save();
		
		//finish measuring, and log the time. then mark 'done' as true
		l = Util.getMeasuringTimeMs() - l;
		LOGGER.info("World optimizaton finished after {} ms", Long.valueOf(l));
		this.done = true;
		
		//clear temp. config variables.
		OW_RUC = false;
		OW_RLC = false;
	}
}