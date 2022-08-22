package thecsdev.nounusedchunks.mixin;

import static thecsdev.nounusedchunks.config.NUCConfig.OW_RUC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
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
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import net.minecraft.world.updater.WorldUpdater;
import thecsdev.nounusedchunks.NoUnusedChunks;

@Mixin(WorldUpdater.class)
public abstract class WorldUpdaterMixin
{
	@Shadow public static Logger LOGGER;
	@Shadow public GeneratorOptions generatorOptions;
	@Shadow public boolean eraseCache;
	@Shadow public LevelStorage.Session session;
	@Shadow public Thread updateThread;
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
	
	@SuppressWarnings("deprecation")
	@Inject(method = "updateWorld", at = @At("HEAD"), cancellable = true)
	public void onUpdateWorld(CallbackInfo callback)
	{
		//if no OW_RUC, then ignore this process
		if(!OW_RUC) return;
		callback.cancel(); //else cancel the vanilla behavior, and use chocolate
		
		// ----------------------------- OW & RUC -----------------------------
		LOGGER.info("Using \"" + NoUnusedChunks.ModID + "'s\" WorldUpdater and optimization system.");
		
		//retrieve chunk positions and a total count of chunks
		totalChunkCount = 0;
		ImmutableMap.Builder<RegistryKey<World>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
		ImmutableSet<RegistryKey<World>> immutableSet = this.generatorOptions.getWorlds();
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
		
		//and here we... retrieve all chunks and their positions? idk
		float f = this.totalChunkCount;
		ImmutableMap<RegistryKey<World>, ListIterator<ChunkPos>> immutableMap = builder.build();
		ImmutableMap.Builder<RegistryKey<World>, VersionedChunkStorage> builder2 = ImmutableMap.builder();
		for (UnmodifiableIterator<RegistryKey<World>> unmodifiableIterator2 = immutableSet.iterator(); unmodifiableIterator2.hasNext();)
		{
		    RegistryKey<World>registryKey2 = unmodifiableIterator2.next();
		    Path path = this.session.getWorldDirectory(registryKey2);
		    builder2.put(registryKey2, new VersionedChunkStorage(path.resolve("region"), this.dataFixer, true));
		}
		ImmutableMap<RegistryKey<World>, VersionedChunkStorage> worldRegionFiles = builder2.build();
		
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
		        
		        if (worldChunkIterator.hasNext())
		        {
		            ChunkPos chunkPos = worldChunkIterator.next();
		            boolean willChunkNotSkip = false;
		            try
		            {
		                NbtCompound nbtCompound = null;
		                try { nbtCompound = versionedChunkStorage.getNbt(chunkPos); } catch (IOException ioExc) {}
		                
		                if (nbtCompound != null)
		                {
		                    int chunkDataVersion = VersionedChunkStorage.getDataVersion(nbtCompound);
		                    ChunkGenerator chunkGenerator = ((DimensionOptions) this.generatorOptions.getDimensions().get(GeneratorOptions.toDimensionOptionsKey(worldRegKey))).getChunkGenerator();
		                    NbtCompound nbtCompound2 = versionedChunkStorage.updateChunkNbt(worldRegKey, () -> this.persistentStateManager, nbtCompound, chunkGenerator.getCodecKey());
		                    ChunkPos chunkPos2 = new ChunkPos(nbtCompound2.getInt("xPos"), nbtCompound2.getInt("zPos"));
		                    
		                    if (!chunkPos2.equals(chunkPos))
		                        LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPos2);
		                    
		                    boolean chunkChangesMade = (chunkDataVersion < SharedConstants.getGameVersion().getWorldVersion());
		                    boolean chunkIsUnused = (nbtCompound2.contains("InhabitedTime") && nbtCompound2.getLong("InhabitedTime") == 0);
		                    
		                    //erasing chunk cache (vanilla behavior)
		                    if (this.eraseCache && !chunkIsUnused)
		                    {
		                    	//erase heightmaps, lighting, and isLightOn
		                        chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("Heightmaps"));
		                        chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("isLightOn"));
		                        nbtCompound2.remove("Heightmaps");
		                        nbtCompound2.remove("isLightOn");
		                        
		                        //erase chunk section cache
		                        NbtList nbtList = nbtCompound2.getList("sections", 10);
		                        for (int j = 0; j < nbtList.size(); j++)
		                        {
		                            NbtCompound nbtCompound3 = nbtList.getCompound(j);
		                            chunkChangesMade = (chunkChangesMade || nbtCompound3.contains("BlockLight"));
		                            chunkChangesMade = (chunkChangesMade || nbtCompound3.contains("SkyLight"));
		                            nbtCompound3.remove("BlockLight");
		                            nbtCompound3.remove("SkyLight");
		                        }
		                    }
		                    
		                    //erasing unused chunk data
		                    else if(chunkIsUnused)
		                    {
		                    	nbtCompound2 = null;
		                    	chunkChangesMade = true;
		                    	/*chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("Lights"));
		                    	chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("sections"));
		                    	chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("PostProcessing"));
		                    	chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("block_ticks"));
		                    	chunkChangesMade = (chunkChangesMade || nbtCompound2.contains("fluid_ticks"));
		                    	nbtCompound2.remove("Lights");
		                    	nbtCompound2.remove("sections");
		                    	nbtCompound2.remove("PostProcessing");
		                    	nbtCompound2.remove("block_ticks");
	                    		nbtCompound2.remove("fluid_ticks");
	                    		nbtCompound2.putString("Status", "structure_starts");*/
		                    }
		                    
		                    //save changes
		                    if (chunkChangesMade)
		                    {
		                        versionedChunkStorage.setNbt(chunkPos, nbtCompound2);
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
		for (UnmodifiableIterator<VersionedChunkStorage> unmodifiableIterator = worldRegionFiles.values().iterator(); unmodifiableIterator.hasNext();)
		{
		    VersionedChunkStorage versionedChunkStorage2 = unmodifiableIterator.next();
		    try { versionedChunkStorage2.close(); }
		    catch (IOException iOException) { LOGGER.error("Error upgrading chunk", iOException); }
		}
		this.persistentStateManager.save();
		
		//finish measuring, and log the time. then mark 'done' as true
		l = Util.getMeasuringTimeMs() - l;
		LOGGER.info("World optimizaton finished after {} ms", Long.valueOf(l));
		this.done = true;
	}
}