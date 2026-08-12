package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.world.World;

public class MobTFSlimeBeetle extends MobMonster {

	public MobTFSlimeBeetle(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "slimebeetle");
		this.setSize(0.9F, 1.75F);
		this.moveSpeed = 0.23F;
		this.attackStrength = 4;
	}

	@Override
	public int getMaxHealth() {
		return 25;
	}
}
