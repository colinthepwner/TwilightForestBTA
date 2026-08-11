package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobSpider;
import net.minecraft.core.world.World;

public class MobTFHedgeSpider extends MobSpider {

	public MobTFHedgeSpider(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "hedgespider");
	}

	@Override
	public boolean canSpawnHere() {
		return this.world.checkIfAABBIsClear(this.bb)
			&& this.world.getEntitiesWithinAABBExcludingEntity(this, this.bb).isEmpty()
			&& !this.world.isAABBInMaterial(this.bb, net.minecraft.core.block.material.Materials.WATER);
	}
}
