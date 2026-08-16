package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.achievement.TFAchievements;
import com.twilightforest.entity.ai.TFBrain;
import com.twilightforest.entity.ai.TFTaskChargeAttack;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.IItemHolding;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

import java.util.ArrayList;
import java.util.List;

public class MobTFMinotaur extends MobMonster implements IItemHolding, TFTaskChargeAttack.Charger {

	private static final int MAX_HEALTH = 30;

	private static final int ATTACK_STRENGTH = 7;

	private static final float NORMAL_SPEED = 0.76F;
	private static final float CHARGE_SPEED = 1.67F;

	private static final double GORE_LIFT = 0.4;

	private static final int DATA_CHARGING = 17;

	private static final float CHARGE_LEG_CHURN = 0.6F;

	private static final ItemStack HELD_AXE = new ItemStack(Items.TOOL_AXE_GOLD, 1);

	private final TFBrain brain = new TFBrain(this, NORMAL_SPEED);

	private final List<WeightedRandomLootObject> burntDrops = new ArrayList<>();

	public MobTFMinotaur(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "minotaur");
		this.moveSpeed = NORMAL_SPEED;
		this.attackStrength = ATTACK_STRENGTH;

		this.mobDrops.add(new WeightedRandomLootObject(Items.FOOD_PORKCHOP_RAW.getDefaultStack(), 0, 1));
		this.burntDrops.add(
			new WeightedRandomLootObject(Items.FOOD_PORKCHOP_COOKED.getDefaultStack(), 0, 1));

		this.brain.add(2, new TFTaskChargeAttack(this, this.brain, CHARGE_SPEED));
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected void updateAI() {
		this.brain.tick(super::updateAI);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

		this.entityData.define(DATA_CHARGING, (byte) 0, Byte.class);
	}

	public boolean isCharging() {
		return this.entityData.getByte(DATA_CHARGING) != 0;
	}

	@Override
	public void setCharging(boolean charging) {
		this.entityData.set(DATA_CHARGING, (byte) (charging ? 127 : 0));
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.isCharging()) {
			this.walkAnimSpeed += CHARGE_LEG_CHURN;
		}
	}

	@Override
	public boolean chargeAttack(Entity victim) {
		boolean success = victim.hurt(this, this.attackStrength, DamageType.COMBAT);
		if (success && this.isCharging()) {
			victim.yd += GORE_LIFT;

		}
		return success;
	}

	@Override
	protected List<WeightedRandomLootObject> getMobDrops() {
		return this.isOnFire() ? this.burntDrops : this.mobDrops;
	}

	@Override
	public ItemStack getHeldItem() {
		return HELD_AXE;
	}

	@Override
	public void setHeldItem(ItemStack itemStack) {
	}

	@Override
	public boolean isLeftHanded() {
		return false;
	}

	@Override
	public String getLivingSound() {
		return "mob.cow";
	}

	@Override
	protected String getHurtSound() {
		return "mob.cowhurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.cowhurt";
	}

	@Override
	public void onDeath(Entity killer) {
		super.onDeath(killer);
		TFAchievements.award(killer, TFAchievements.HUNTER);
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
}
