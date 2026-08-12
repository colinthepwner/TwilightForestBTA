package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobSheep;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.world.World;

import java.util.Random;

public class MobTFBighorn extends MobSheep {
	public MobTFBighorn(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "bighorn");
		this.setSize(0.9F, 1.3F);
		this.setFleeceColor(randomBighornFleeceColor(this.random));
	}

	public static DyeColor randomBighornFleeceColor(Random random) {
		return DyeColor.colorFromBlockMeta(random.nextInt(2) == 0 ? 12 : random.nextInt(15));
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HUNTER);
	}
}
