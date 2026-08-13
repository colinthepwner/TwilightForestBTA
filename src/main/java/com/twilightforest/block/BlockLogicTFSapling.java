package com.twilightforest.block;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicSaplingBase;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

import java.util.Random;
import java.util.function.Supplier;

public class BlockLogicTFSapling extends BlockLogicSaplingBase {

	private final Supplier<TFWorldFeature> tree;

	public BlockLogicTFSapling(Block<?> block, Supplier<TFWorldFeature> tree) {
		super(block);
		this.tree = tree;
	}

	@Override
	public void growTree(World world, TilePosc pos, Random random) {
		int x = pos.x();
		int y = pos.y();
		int z = pos.z();

		world.setBlockType(pos, Blocks.AIR);
		if (!this.tree.get().place(world, random, x, y, z)) {
			world.setBlockType(pos, this.block);
		}
	}
}
