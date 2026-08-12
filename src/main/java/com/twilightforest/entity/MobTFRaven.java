package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.world.World;

public class MobTFRaven extends MobChicken {

	private static final int MAX_HEALTH = 10;

	public MobTFRaven(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "raven");
		this.setSize(0.3F, 0.7F);
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.raven.caw";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.raven.squawk";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.raven.squawk";
	}
}
