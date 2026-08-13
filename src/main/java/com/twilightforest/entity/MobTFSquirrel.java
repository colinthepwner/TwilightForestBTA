package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.animal.MobDeer;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

public class MobTFSquirrel extends MobDeer {

	private static final int MAX_HEALTH = 1;

	public MobTFSquirrel(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "squirrel");
		this.setSize(0.3F, 0.7F);
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected float getBlockPathWeight(@NotNull TilePosc pos) {
		return TFAmbientPathing.weigh(this.world, pos);
	}

	@Override
	public String getLivingSound() {
		return null;
	}

	@Override
	protected String getHurtSound() {
		return null;
	}

	@Override
	protected String getDeathSound() {
		return null;
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HUNTER);
	}
}
