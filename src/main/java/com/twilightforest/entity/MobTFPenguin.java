package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.world.World;

public class MobTFPenguin extends MobChicken {

	public MobTFPenguin(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "penguin");
	}

	@Override
	public String getLivingSound() {
		return null;
	}
}
