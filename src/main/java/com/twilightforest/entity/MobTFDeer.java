package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobCow;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;

public class MobTFDeer extends MobCow {
	public MobTFDeer(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "wilddeer");
		this.setSize(0.7F, 2.3F);
	}

	@Override
	public String getLivingSound() {
		return null;
	}

	@Override
	public boolean interact(Player player) {
		return false;
	}
}
