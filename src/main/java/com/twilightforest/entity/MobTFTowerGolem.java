package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.BlockParticleHelper;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class MobTFTowerGolem extends MobMonster {

	private static final int MAX_HEALTH = 40;

	private static final int ATTACK_STRENGTH = 9;

	private static final float CHASE_SPEED = 0.76F;
	private static final float WANDER_SPEED = 0.49F;

	private static final int ATTACK_ANIMATION_TICKS = 10;

	private static final int DATA_ATTACK_TIMER = 17;

	private static final double SLAM_LIFT = 0.4;

	private static final int MAX_PER_CHUNK = 16;

	private static final int DUST_ONE_IN = 5;

	private static final double MOVING_THRESHOLD = 2.5000002779052E-7;

	public MobTFTowerGolem(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "towergolem");
		this.setSize(1.6F, 2.8F);
		this.moveSpeed = WANDER_SPEED;
		this.attackStrength = ATTACK_STRENGTH;

		this.mobDrops.add(new WeightedRandomLootObject(Blocks.FLOWER_RED.getDefaultStack(), 0, 2));
		this.mobDrops.add(new WeightedRandomLootObject(Items.INGOT_IRON.getDefaultStack(), 3, 5));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ATTACK_TIMER, (byte) 0, Byte.class);
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return MAX_PER_CHUNK;
	}

	public int getAttackTimer() {
		return this.entityData.getByte(DATA_ATTACK_TIMER);
	}

	private void setAttackTimer(int ticks) {
		this.entityData.set(DATA_ATTACK_TIMER, (byte) ticks);
	}

	@Override
	protected void updateAI() {
		this.moveSpeed = this.getTarget() != null ? CHASE_SPEED : WANDER_SPEED;
		super.updateAI();
	}

	@Override
	protected void attackEntity(@NotNull Entity entity, float distance) {
		int before = this.attackTime;
		super.attackEntity(entity, distance);

		if (this.attackTime > before) {
			this.setAttackTimer(ATTACK_ANIMATION_TICKS);
			entity.yd += SLAM_LIFT;

		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		int timer = this.getAttackTimer();
		if (timer > 0) {
			this.setAttackTimer(timer - 1);
		}

		if (this.xd * this.xd + this.zd * this.zd > MOVING_THRESHOLD
			&& this.random.nextInt(DUST_ONE_IN) == 0) {
			int bx = MathHelper.floor(this.x);
			int by = MathHelper.floor(this.y - 0.2 - this.heightOffset);
			int bz = MathHelper.floor(this.z);
			int blockId = this.world.getBlockId(bx, by, bz);
			if (blockId > 0) {
				this.world.spawnParticle("block",
					this.x + (this.random.nextFloat() - 0.5) * this.bbWidth,
					this.bb.minY + 0.1,
					this.z + (this.random.nextFloat() - 0.5) * this.bbWidth,
					4.0 * (this.random.nextFloat() - 0.5),
					0.5,
					(this.random.nextFloat() - 0.5) * 4.0,
					BlockParticleHelper.encodeBlockData(blockId,
						this.world.getBlockMetadata(bx, by, bz), Side.BOTTOM),
					false);
			}
		}
	}

	@Override
	public String getLivingSound() {
		return "";
	}

	@Override
	protected String getHurtSound() {
		return "tile.crumble";
	}

	@Override
	protected String getDeathSound() {
		return "tile.deepcrumble";
	}
}
