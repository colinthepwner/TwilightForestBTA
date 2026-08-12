package com.twilightforest.world.chunk;

import com.twilightforest.world.feature.TFRavineLargeFeature;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.CavesLargeFeature;
import net.minecraft.core.world.generate.LargeFeature;
import net.minecraft.core.world.generate.chunk.perlin.ChunkGeneratorPerlin;
import org.jetbrains.annotations.NotNull;

public class ChunkGeneratorTF extends ChunkGeneratorPerlin {

	public ChunkGeneratorTF(@NotNull World world) {
		super(
			world,
			new ChunkDecoratorTF(world),
			new TerrainGeneratorTF(world),
			new SurfaceGeneratorTF(world),
			new LargeFeature[]{new CavesLargeFeature(), new TFRavineLargeFeature()}
		);
	}
}
