package com.twilightforest.world.chunk;

import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.perlin.DensityGenerator;
import net.minecraft.core.world.generate.chunk.perlin.TerrainGeneratorLerp;
import org.jetbrains.annotations.NotNull;

public class TerrainGeneratorTwilightForest extends TerrainGeneratorLerp {

	private final DensityGenerator densityGenerator;

	public TerrainGeneratorTwilightForest(@NotNull World world) {
		super(world);
		this.densityGenerator = new DensityGeneratorTwilightForest(world);
	}

	@NotNull
	@Override
	public DensityGenerator getDensityGenerator() {
		return this.densityGenerator;
	}

	@Override
	protected int getBlockAt(@NotNull Chunk chunk, int x, int y, int z, double density) {
		return density > 0.0 ? this.world.getWorldType().getFillerBlockId() : 0;
	}
}
