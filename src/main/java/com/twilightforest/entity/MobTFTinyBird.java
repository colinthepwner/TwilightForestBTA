package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.world.World;

public class MobTFTinyBird extends MobChicken {

	private static final int MAX_HEALTH = 1;

	public MobTFTinyBird(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "tinybird");
		this.setSize(0.3F, 0.7F);
		this.setSkinVariant(this.random.nextInt(4));
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.tinybird.chirp";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.tinybird.hurt";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.tinybird.hurt";
	}
}
