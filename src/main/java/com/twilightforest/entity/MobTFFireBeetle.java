package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.world.World;

public class MobTFFireBeetle extends MobMonster {

	public MobTFFireBeetle(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "firebeetle");
		this.setSize(1.1F, 0.75F);
		this.moveSpeed = 0.23F;
		this.attackStrength = 4;
	}

	@Override
	public int getMaxHealth() {
		return 25;
	}
}
