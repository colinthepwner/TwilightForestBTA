package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.projectile.EntityTFNatureBolt;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.IItemHolding;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;

public class MobTFSkeletonDruid extends MobMonster implements IItemHolding {

	private static final ItemStack DEFAULT_HELD_ITEM = new ItemStack(Items.TOOL_HOE_GOLD, 1);

	private static final int ATTACK_COOLDOWN = 40;

	private static final float ATTACK_RANGE = 10.0F;

	public MobTFSkeletonDruid(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "skeletondruid");

		this.mobDrops.add(new WeightedRandomLootObject(Items.STICK.getDefaultStack(), 1, 1));
		this.mobDrops.add(new WeightedRandomLootObject(Items.BONE.getDefaultStack(), 0, 2));
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
		return false;
	}

	@Override
	public String getLivingSound() {
		return "mob.skeleton";
	}

	@Override
	protected String getHurtSound() {
		return "mob.skeletonhurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.skeletonhurt";
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		double aimX = target.x - this.x;
		double aimY = target.bb.minY() + target.bbHeight / 2.0F - (this.y + this.bbHeight / 2.0F);
		double aimZ = target.z - this.z;

		if (this.canEntityBeSeen(target) && this.attackTime == 0 && distance < ATTACK_RANGE) {
			this.world.playSoundAtEntity(null, this, "mob.ghast.fireball", this.getSoundVolume(),
				(this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

			this.world.entityJoinedWorld(new EntityTFNatureBolt(this.world, this, aimX, aimY, aimZ));
			this.attackTime = ATTACK_COOLDOWN;
		}

		this.hasAttacked = true;
	}
}
