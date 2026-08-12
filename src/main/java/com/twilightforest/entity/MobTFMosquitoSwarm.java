package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.world.World;

public class MobTFMosquitoSwarm extends MobMonster {

	public MobTFMosquitoSwarm(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "mosquitoswarm");
		this.setSize(0.7F, 1.9F);
		this.moveSpeed = 0.23F;
		this.attackStrength = 3;
	}

	@Override
	public int getMaxHealth() {
		return 12;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.mosquito.mosquito";
	}
}
