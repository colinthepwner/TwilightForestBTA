package com.twilightforest.world.chunk;

import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.LargeFeature;
import net.minecraft.core.world.generate.chunk.perlin.ChunkGeneratorPerlin;
import org.jetbrains.annotations.NotNull;

public class ChunkGeneratorTwilightForest extends ChunkGeneratorPerlin {

	public ChunkGeneratorTwilightForest(@NotNull World world) {
		super(
			world,
			new ChunkDecoratorTwilightForest(world),
			new TerrainGeneratorTwilightForest(world),
			new SurfaceGeneratorTwilightForest(world),
			new LargeFeature[]{}
		);
	}
}
