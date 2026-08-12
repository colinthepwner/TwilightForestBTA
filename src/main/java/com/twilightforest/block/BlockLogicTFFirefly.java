package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;

public class BlockLogicTFFirefly extends BlockLogicTFCritter {

	public BlockLogicTFFirefly(@NotNull Block<?> block) {
		super(block);
	}

	@Override
	protected boolean canAttachTo(@NotNull World world, int x, int y, int z) {
		TilePos pos = new TilePos(x, y, z);
		if (world.isBlockOpaqueCube(pos)) {
			return true;
		}
		Material material = world.getBlockMaterial(pos);
		return material == Materials.LEAVES || material == Materials.CACTUS;
	}
}
