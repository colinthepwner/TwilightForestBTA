package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.world.World;

public class MobTFKobold extends MobMonster {

	public MobTFKobold(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "kobold");
		this.setSize(0.8F, 1.1F);
		this.moveSpeed = 0.28F;

		this.attackStrength = 3;
	}

	@Override
	public int getMaxHealth() {
		return 13;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.kobold.kobold";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.kobold.hurt";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.kobold.die";
	}
}
