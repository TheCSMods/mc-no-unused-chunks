package thecsdev.nounusedchunks.mixin;

import static thecsdev.nounusedchunks.config.NUCConfig.OW_RUC;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DataFixer;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import thecsdev.nounusedchunks.NoUnusedChunks;

@Mixin(value = WorldUpgrader.class, priority = 1100)
public abstract class WorldUpgraderMixin
{
	@Shadow public static Logger LOGGER;
	@Shadow public static ThreadFactory THREAD_FACTORY;
	@Shadow public WorldGenSettings worldGenSettings;
	@Shadow public boolean eraseCache;
	@Shadow public LevelStorageSource.LevelStorageAccess levelStorage;
	@Shadow public Thread thread;
	@Shadow public DataFixer dataFixer;
	@Shadow public volatile boolean running;
	@Shadow public volatile boolean finished;
	@Shadow public volatile float progress;
	@Shadow public volatile int totalChunks;
	@Shadow public volatile int converted;
	@Shadow public volatile int skipped;
	@Shadow public Object2FloatMap<ResourceKey<Level>> progressMap;
	@Shadow public volatile Component status;
	@Shadow public static Pattern REGEX;
	@Shadow public DimensionDataStorage overworldDataStorage;
	
	@Shadow public abstract List<ChunkPos> getAllChunkPos(ResourceKey<Level> levelKey);
	
	@Inject(method = "work", at = @At("HEAD"), cancellable = true)
	public void onWork(CallbackInfo callback)
	{
		//if no OW_RUC, then ignore this process
		if(!OW_RUC) return;
		callback.cancel();
		
		// ----------------------------- OW & RUC -----------------------------
		LOGGER.info("Using \"" + NoUnusedChunks.ModID + "'s\" WorldUpgrader and optimization system.");
		
		//retrieve chunk positions and a total count of chunks
		this.totalChunks = 0;
		Builder<ResourceKey<Level>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
		ImmutableSet<ResourceKey<Level>> worldKeySet = this.worldGenSettings.levels();
		for(ResourceKey<Level> worldKey : worldKeySet)
		{
			List<ChunkPos> list = this.getAllChunkPos(worldKey);
			builder.put(worldKey, list.listIterator());
			this.totalChunks += list.size();
		}
		
		//if there are no chunks, return
		if (this.totalChunks == 0)
		{
			this.finished = true;
			return;
		}
		
		//retrieve chunk regions
		float f1 = (float)this.totalChunks;
		ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> immutablemap = builder.build();
		Builder<ResourceKey<Level>, ChunkStorage> worldRegionFilesBuilder = ImmutableMap.builder();
		Builder<ResourceKey<Level>, ChunkStorage> __worldTempRegionFilesBuilder = ImmutableMap.builder();
		for(ResourceKey<Level> worldKey : worldKeySet)
		{
			//get region directory path of the world (dimension)
			Path path = this.levelStorage.getDimensionPath(worldKey);
			
			//create region file builders
			worldRegionFilesBuilder.put(worldKey, new ChunkStorage(path.resolve("region"), this.dataFixer, false)); //dsync off
			__worldTempRegionFilesBuilder.put(worldKey, new ChunkStorage(path.resolve("region")
					.resolve(NoUnusedChunks.ModID + "_temp"), this.dataFixer, true));
		}
		ImmutableMap<ResourceKey<Level>, ChunkStorage> worldRegionFiles = worldRegionFilesBuilder.build();
		ImmutableMap<ResourceKey<Level>, ChunkStorage> __worldTempRegionFiles = __worldTempRegionFilesBuilder.build();
		
		//now for the upgrading process
		long i = Util.getMillis();
		this.status = NoUnusedChunks.tt("optimizeWorld.stage.upgrading");
		while(this.running)
		{
			//flags
			boolean willKeepUpgradingChunks = false;
			float progressTrack = 0.0F;
			
			//iterate ResourceKey<Level>-s
			for(ResourceKey<Level> worldKey : worldKeySet)
			{
				ListIterator<ChunkPos> worldChunkIterator = immutablemap.get(worldKey);
				ChunkStorage versionedChunkStorage = worldRegionFiles.get(worldKey);
				ChunkStorage __vcsTemp = __worldTempRegionFiles.get(worldKey);
				
				if (worldChunkIterator.hasNext())
				{
					ChunkPos chunkPos = worldChunkIterator.next();
					boolean willChunkNotSkip = false;

					try
					{
						CompoundTag chunkNbt = versionedChunkStorage.read(chunkPos).join().orElse(null);
						if (chunkNbt != null)
						{
							int chunkNbtVersion = ChunkStorage.getVersion(chunkNbt);
							ChunkGenerator chunkgenerator = this.worldGenSettings.dimensions().get(WorldGenSettings.levelToLevelStem(worldKey)).generator();
							CompoundTag chunkNbtUpdated = versionedChunkStorage.upgradeChunkTag(worldKey, () -> this.overworldDataStorage, chunkNbt, chunkgenerator.getTypeNameForDataFixer());
							ChunkPos chunkPosUpdated = new ChunkPos(chunkNbtUpdated.getInt("xPos"), chunkNbtUpdated.getInt("zPos"));
							
							if (!chunkPosUpdated.equals(chunkPos))
								LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPosUpdated);
							
							@SuppressWarnings("deprecation")
							boolean chunkChangesMade = chunkNbtVersion < SharedConstants.getCurrentVersion().getWorldVersion();
							boolean chunkIsUnused = OW_RUC && NoUnusedChunks.isChunkUnused(worldKey, chunkPos, chunkNbtUpdated);
							
							if (this.eraseCache && !chunkIsUnused)
							{
								chunkChangesMade = chunkChangesMade || chunkNbtUpdated.contains("Heightmaps");
								chunkChangesMade = chunkChangesMade || chunkNbtUpdated.contains("isLightOn");
								chunkNbtUpdated.remove("Heightmaps");
								chunkNbtUpdated.remove("isLightOn");
								
								// ----- Forge-1.19.x - This Forge version erases lighting cache as well.
								//erase chunk section cache (block and sky light)
		                        ListTag nbtList = chunkNbtUpdated.getList("sections", 10);
		                        for (int j = 0; j < nbtList.size(); j++)
		                        {
		                            CompoundTag nbtCompound3 = nbtList.getCompound(j);
		                            chunkChangesMade = chunkChangesMade || nbtCompound3.contains("BlockLight");
		                            chunkChangesMade = chunkChangesMade || nbtCompound3.contains("SkyLight");
		                            nbtCompound3.remove("BlockLight");
		                            nbtCompound3.remove("SkyLight");
		                        }
							}
							else if(chunkIsUnused)
							{
								chunkNbtUpdated = null;
								chunkChangesMade = true;
							}
							
							if(!chunkIsUnused) __vcsTemp.write(chunkPos, chunkNbtUpdated);
							if (chunkChangesMade)
							{
								//versionedChunkStorage.write(chunkPos, chunkNbtUpdated); -- already stored in __vcsTemp
								willChunkNotSkip = true;
							}
						}
					}
					catch (ReportedException crashException)
					{
						Throwable crashCause = crashException.getCause();
						if (!(crashCause instanceof IOException)) { throw crashException; }
						LOGGER.error("Error upgrading chunk {}", chunkPos, crashCause);
					}
					catch (Exception crashIoExc)
					{
						if(crashIoExc instanceof IOException)
							LOGGER.error("Error upgrading chunk {}", chunkPos, crashIoExc);
						else throw crashIoExc;
					}

					if (willChunkNotSkip) { ++this.converted; }
					else { ++this.skipped; }

					willKeepUpgradingChunks = true;
				}
				
				float f2 = (float)worldChunkIterator.nextIndex() / f1;
				this.progressMap.put(worldKey, f2);
				progressTrack += f2;
			}

			this.progress = progressTrack;
			if (!willKeepUpgradingChunks) { this.running = false; }
		}

		//finishing...
		this.status = NoUnusedChunks.tt("optimizeWorld.stage.finished");
		worldRegionFiles.forEach((wrKey, vcsValue) ->
		{
			try
			{
				//close the chunk storages
				vcsValue.close();
				
				ChunkStorage __vcsTempValue = __worldTempRegionFiles.get(wrKey);
				if(__vcsTempValue != null) __vcsTempValue.close();
				
				//move the temp. region files to override the old ones
				Path worldRegDir = this.levelStorage.getDimensionPath(wrKey).resolve("region");
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
			catch (IOException ioexception) { LOGGER.error("Error upgrading chunk", (Throwable)ioexception); }
			catch (NullPointerException e) {} //just in case Forge has this problem as well
		});
		
		this.overworldDataStorage.save();
		
		//finish measuring, and log the time. then mark 'done' as true
		i = Util.getMillis() - i;
		LOGGER.info("World optimizaton finished after {} ms", (long)i);
		this.finished = true;
		
		//clear temp. config variables.
		OW_RUC = false;
	}
}