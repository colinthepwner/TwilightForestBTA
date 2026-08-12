package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.world.World;

public class MobTFRedcapSapper extends MobTFRedcap {

	public MobTFRedcapSapper(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "redcapsapper");
	}

	@Override
	public int getMaxHealth() {
		return 30;
	}
}
