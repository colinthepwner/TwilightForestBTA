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

	private final boolean damaging;

	public BlockLogicTFHedge(Block<?> block) {
		this(block, true);
	}

	public static BlockLogicTFHedge harmless(Block<?> block) {
		return new BlockLogicTFHedge(block, false);
	}

	private BlockLogicTFHedge(Block<?> block, boolean damaging) {
		super(block, Materials.CLAY);
		this.damaging = damaging;
	}

	private boolean shouldDamage(Entity entity) {
		return this.damaging && !(entity instanceof MobSpider);
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
