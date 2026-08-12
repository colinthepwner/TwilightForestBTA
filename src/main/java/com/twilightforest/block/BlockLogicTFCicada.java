package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;

public class BlockLogicTFCicada extends BlockLogicTFCritter {

	public BlockLogicTFCicada(@NotNull Block<?> block) {
		super(block);
	}

	@Override
	protected boolean canAttachTo(@NotNull World world, int x, int y, int z) {
		TilePos pos = new TilePos(x, y, z);
		return world.isBlockOpaqueCube(pos) || world.getBlockMaterial(pos) == Materials.LEAVES;
	}
}
