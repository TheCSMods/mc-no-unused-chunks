package thecsdev.nounusedchunks.mixin;

import static thecsdev.nounusedchunks.config.NUCConfig.ENABLED;
import static thecsdev.nounusedchunks.config.NUCConfig.UNFLAG_CHANCE;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

@Mixin(value = ChunkStatus.class, remap = true)
public abstract class ChunkStatusMixin
{
	@Inject(method = "generate", at = @At("RETURN"))
	public void nounusedchunks_onGenerate(
			Executor executor, ServerLevel world, ChunkGenerator chunkGenerator,
			StructureManager structureManager, ThreadedLevelLightEngine lightingProvider,
			Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> fullChunkConverter,
			List<ChunkAccess> chunkAccesses, boolean bl,
			CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> callback)
	{
		//handle properties
		if(ENABLED) return;
		if(UNFLAG_CHANCE != 100)
		{
			Random rnd = new Random();
			if(!(UNFLAG_CHANCE >= rnd.nextInt(0, 101))) return;
		}
		
		//turn the action into a Runnable task
		Runnable task = () ->
		{
			try
			{
				//get and null check the return value
				CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> rValue = callback.getReturnValue();
				if(rValue == null || !rValue.isDone()) return; //do not remove the isDone check, weird deadlocks occur when you do
				
				//get and null check the chunk
				ChunkAccess chunk = rValue.get().left().orElse(null);
				if(chunk == null) return;
				
				//unmark the chunk
				chunk.setUnsaved(false);
			}
			catch(Exception exc) {}
		};
		
		//let the server execute the task once it is able to
		world.getServer().execute(task);
	}
}