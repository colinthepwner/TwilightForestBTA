package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobPig;
import net.minecraft.core.world.World;

public class MobTFBoar extends MobPig {
	public MobTFBoar(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "wildboar");
		this.setSize(0.9F, 0.9F);
	}
}
