package com.twilightforest.block;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobSpider;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class BlockLogicTFHedge extends BlockLogic {

	public static final int DAMAGE = 3;

	public BlockLogicTFHedge(Block<?> block) {
		super(block, Materials.LEAVES);
	}

	private static boolean shouldDamage(Entity entity) {
		return !(entity instanceof MobSpider);
	}

	@Override
	public void onEntityCollision(@NotNull World world, @NotNull TilePosc tilePos,
	                              @NotNull Entity entity) {
		if (shouldDamage(entity)) {
			entity.hurt(null, DAMAGE, DamageType.COMBAT);
		}
	}

	@Override
	public void onEntityWalkedOn(@NotNull World world, @NotNull TilePosc tilePos,
	                             @NotNull Entity walker) {
		if (shouldDamage(walker)) {
			walker.hurt(null, DAMAGE, DamageType.COMBAT);
		}
	}
}
