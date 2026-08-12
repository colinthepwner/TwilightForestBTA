package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.IItemHolding;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;

public class MobTFRedcap extends MobMonster implements IItemHolding {

	private static final ItemStack DEFAULT_HELD_ITEM = new ItemStack(Items.TOOL_PICKAXE_IRON, 1);

	protected final boolean lefty;

	protected boolean shy;

	public MobTFRedcap(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "redcap");
		this.setSize(0.9F, 1.4F);
		this.moveSpeed = 0.5F;
		this.attackStrength = 2;
		this.lefty = this.random.nextBoolean();
		this.shy = true;

		this.mobDrops.add(new WeightedRandomLootObject(Items.ARMOR_BOOTS_IRON.getDefaultStack(), 1, 1));
		this.mobDrops.add(new WeightedRandomLootObject(Items.TOOL_PICKAXE_IRON.getDefaultStack(), 1, 1));
	}

	@Override
	public int getMaxHealth() {
		return 20;
	}

	@Override
	public ItemStack getHeldItem() {
		return DEFAULT_HELD_ITEM;
	}

	@Override
	public void setHeldItem(ItemStack itemStack) {
	}

	@Override
	public boolean isLeftHanded() {
		return this.lefty;
	}

	@Override
	public String getLivingSound() {
		return "mob.zombie";
	}

	@Override
	protected String getHurtSound() {
		return "mob.zombiehurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.zombiedeath";
	}

	@Override
	protected void updateAI() {
		super.updateAI();

		Entity target = this.getTarget();
		if (target == null) {
			return;
		}

		float enemyDist = target.distanceTo(this);

		this.moveSpeed = (enemyDist >= 4.0F && this.shy) ? 0.5F : 0.8F;

		if (enemyDist > 4.0F && enemyDist < 6.0F && this.shy && this.isTargetLookingAtMe()) {
			this.moveStrafing = this.lefty ? this.moveForward : -this.moveForward;
			this.moveForward = 0.0F;
		}
	}

	public boolean isTargetLookingAtMe() {
		Entity target = this.getTarget();
		if (target == null) {
			return false;
		}

		double dx = this.x - target.x;
		double dz = this.z - target.z;
		float angle = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		float difference = Math.abs((target.yRot - angle) % 360.0F);

		return difference < 60.0F || difference > 300.0F;
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HILL_1);
	}
}
