package thecsdev.nounusedchunks.mixin;

import java.util.List;
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
import thecsdev.nounusedchunks.NoUnusedChunks;

@Mixin(targets = "net/minecraft/world/level/chunk/ChunkStatus$SimpleGenerationTask", remap = true)
public interface SimpleGenerationTaskMixin
{
	@Inject(method = "doWork", at = @At("RETURN"))
	default void doWork(
			ChunkStatus cStatus, Executor executor, ServerLevel world, ChunkGenerator cGen,
			StructureManager strManager, ThreadedLevelLightEngine lightEngine, Function<ChunkAccess,
			CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cAccessFunc,
			List<ChunkAccess> chunkAccesses, ChunkAccess chunkAccess, boolean bl,
			CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> callback)
	{
		NoUnusedChunks.LOGGER.info("Generating a chunk?");
	}
}