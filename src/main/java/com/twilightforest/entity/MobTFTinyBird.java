package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.ai.TFBrain;
import com.twilightforest.entity.ai.TFBrainHost;
import com.twilightforest.entity.ai.TFTaskAvoidEntity;
import com.twilightforest.entity.ai.TFTaskPanic;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobTFTinyBird extends MobChicken implements TFBrainHost {

	private static final int MAX_HEALTH = 1;

	private static final double TRIGGER_RANGE = 2.0;

	private static final float NORMAL_SPEED = 0.45F;
	private static final float FLEE_SPEED_FAR = 0.41F;
	private static final float FLEE_SPEED_NEAR = 0.72F;
	private static final float PANIC_SPEED = 0.68F;

	private final TFBrain brain = new TFBrain(this, NORMAL_SPEED);

	private final TFTaskPanic panic = new TFTaskPanic(this, this.brain, PANIC_SPEED);

	public MobTFTinyBird(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "tinybird");
		this.setSize(0.3F, 0.7F);
		this.setSkinVariant(this.random.nextInt(4));
		this.moveSpeed = NORMAL_SPEED;

		this.brain.add(1, this.panic);
		this.brain.add(3, new TFTaskAvoidEntity(this, this.brain, Player.class,
			TRIGGER_RANGE, FLEE_SPEED_FAR, FLEE_SPEED_NEAR));
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
	protected void updateAI() {
		this.brain.tick(super::updateAI);
	}

	@Override
	public boolean hurt(@Nullable Entity attacker, int damage, DamageType type) {
		if (attacker != this) {
			this.panic.alarm(attacker);
		}
		return super.hurt(attacker, damage, type);
	}

	@Override
	public MobPathfinder asMob() {
		return this;
	}

	@Override
	public float tfBlockPathWeight(TilePosc pos) {
		return this.getBlockPathWeight(pos);
	}

	@Override
	public void tfSetSpeed(float speed) {
		this.moveSpeed = speed;
	}

	@Override
	public void tfSetRandomWalk(boolean enabled) {
		this.doRandomWalk = enabled;
	}

	@Override
	public void tfDrive(float yRot, float moveForward, boolean jumping) {
		this.yRot = yRot;
		this.moveForward = moveForward;
		this.isJumping = jumping;
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

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HUNTER);
	}
}
