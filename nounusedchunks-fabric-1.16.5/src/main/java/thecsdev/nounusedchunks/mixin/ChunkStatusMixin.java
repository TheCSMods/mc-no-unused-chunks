package thecsdev.nounusedchunks.mixin;

import static thecsdev.nounusedchunks.config.NUCConfig.UNFLAG_CHANCE;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import thecsdev.nounusedchunks.NoUnusedChunks;
import thecsdev.nounusedchunks.config.NUCConfig;

@Mixin(ChunkStatus.class)
public abstract class ChunkStatusMixin
{
	private final Random noUnusedChunks_rng = new Random();
	
	@Inject(method = "runGenerationTask", at = @At("RETURN"))
	public void nounusedchunks_onRunGenerationTask(
			ServerWorld world,
			ChunkGenerator generator,
			StructureManager structureManager,
			ServerLightingProvider lightingProvider,
			Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> fullChunkConverter,
			List<Chunk> chunks,
			CallbackInfoReturnable<Either<Chunk, ChunkHolder.Unloaded>> callback)
	{
		//handle properties
		if(!NUCConfig.ENABLED) return;
		if(UNFLAG_CHANCE != 100)
		{
			if(!(UNFLAG_CHANCE >= NoUnusedChunks.nextInt(noUnusedChunks_rng, 0, 101))) return;
		}
		
		//turn the action into a Runnable task
		Runnable task = () ->
		{
			try
			{
				//get and null check the return value
				@SuppressWarnings("unchecked") //ik what i'm doing
				CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> rValue = (CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>)(Object)callback.getReturnValue();
				if(rValue == null || !rValue.isDone()) return; //do not remove the isDone check, weird deadlocks occur when you do
				
				//get and null check the chunk
				Chunk chunk = rValue.get().left().orElse(null);
				if(chunk == null) return;
				
				//unmark the chunk
				chunk.setShouldSave(false); //TODO - apparently this does nothing on 1.16.5
			}
			catch(Exception exc) {}
		};
		
		//let the server execute the task once it is able to
		world.getServer().execute(task);
	}
}