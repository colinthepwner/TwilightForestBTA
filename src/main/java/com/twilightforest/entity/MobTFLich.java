package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.projectile.EntityTFLichBolt;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.IItemHolding;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.joml.Vector3d;

public class MobTFLich extends MobMonster implements IItemHolding {

	private static final ItemStack DEFAULT_HELD_ITEM = new ItemStack(Items.TOOL_SWORD_GOLD, 1);

	private static final int ATTACK_CYCLE = 80;

	private static final int TELEPORT_AT = 60;

	private static final float ATTACK_RANGE = 20.0F;

	public MobTFLich(World world) {
		super(world);
		this.setSize(1.1F, 2.5F);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "twilightlich64");

		this.footSize = 0.25F;
	}

	@Override
	public int getMaxHealth() {
		return 50;
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
	public void onLivingUpdate() {
		int factor = (ATTACK_CYCLE - this.attackTime) / 10;
		int particles = factor > 0 ? this.random.nextInt(factor) : 0;

		for (int i = 0; i < particles; i++) {
			float sparkle = (ATTACK_CYCLE - this.attackTime) / 20.0F;
			float dx = (this.random.nextFloat() - 0.5F) * sparkle;
			float dy = (this.random.nextFloat() - 0.5F) * sparkle;
			float dz = (this.random.nextFloat() - 0.5F) * sparkle;

			float angle = this.yBodyRot * (float) Math.PI / 180.0F;
			double px = this.x + MathHelper.cos(angle) * 0.65;
			double py = this.y + this.getHeadHeight() * 0.82;
			double pz = this.z + MathHelper.sin(angle) * 0.65;

			this.world.spawnParticle("mobSpell", px, py, pz, dx, dy, dz, 0, false);
		}

		super.onLivingUpdate();
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if (this.attackTime == TELEPORT_AT) {
			this.teleportToSightOfEntity(target);
		}

		if (this.canEntityBeSeen(target) && this.attackTime == 0 && distance < ATTACK_RANGE) {
			float angle = this.yBodyRot * (float) Math.PI / 180.0F;
			double sx = this.x + MathHelper.cos(angle) * 0.65;
			double sy = this.y + this.getHeadHeight() * 0.82;
			double sz = this.z + MathHelper.sin(angle) * 0.65;

			double aimX = target.x - sx;
			double aimY = target.bb.minY + target.bbHeight / 2.0F - (this.y + this.bbHeight / 2.0F);
			double aimZ = target.z - sz;

			this.world.playSoundAtEntity(null, this, "mob.ghast.fireball", this.getSoundVolume(),
				(this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

			EntityTFLichBolt bolt = new EntityTFLichBolt(this.world, this, aimX, aimY, aimZ);

			bolt.moveTo(sx, sy, sz, this.yRot, this.xRot);
			this.world.entityJoinedWorld(bolt);

			this.attackTime = ATTACK_CYCLE;
		}

		this.hasAttacked = true;
	}

	@SuppressWarnings("unused")
	protected boolean teleportRandomly() {
		double tx = this.x + (this.random.nextDouble() - 0.5) * 64.0;
		double ty = this.y + (this.random.nextInt(64) - 32);
		double tz = this.z + (this.random.nextDouble() - 0.5) * 64.0;
		return this.teleportTo(tx, ty, tz);
	}

	@SuppressWarnings("unused")
	protected boolean teleportToEntity(Entity entity) {
		Vector3d away = new Vector3d(
			this.x - entity.x,
			this.bb.minY + this.bbHeight / 2.0F - entity.y + entity.getHeadHeight(),
			this.z - entity.z
		).normalize();

		double reach = 16.0;
		double tx = this.x + (this.random.nextDouble() - 0.5) * 8.0 - away.x * reach;
		double ty = this.y + (this.random.nextInt(16) - 8) - away.y * reach;
		double tz = this.z + (this.random.nextDouble() - 0.5) * 8.0 - away.z * reach;
		return this.teleportTo(tx, ty, tz);
	}

	protected boolean teleportToSightOfEntity(Entity entity) {
		double tx = 0.0;
		double ty = 0.0;
		double tz = 0.0;
		int tries = 100;

		for (int i = 0; i < tries; i++) {
			tx = entity.x + (this.random.nextDouble() - 0.5) * 16.0;
			ty = entity.y + (this.random.nextInt(16) - 8);
			tz = entity.z + (this.random.nextDouble() - 0.5) * 16.0;
			if (this.canEntitySee(entity, tx, ty, tz)) {
				break;
			}
		}

		if (tries == 99) {
			return false;
		}

		if (!this.teleportTo(tx, ty, tz)) {
			return false;
		}

		this.lookAt(entity, 100.0F, 100.0F);
		return true;
	}

	protected boolean canEntitySee(Entity entity, double tx, double ty, double tz) {
		HitResult hit = this.world.checkBlockCollisionBetweenPoints(
			new Vector3d(entity.x, entity.y + entity.getHeadHeight(), entity.z),
			new Vector3d(tx, ty, tz)
		);
		return hit == null;
	}

	protected boolean teleportTo(double tx, double ty, double tz) {
		double fromX = this.x;
		double fromY = this.y;
		double fromZ = this.z;

		this.x = tx;
		this.y = ty;
		this.z = tz;

		boolean landed = false;
		int bx = MathHelper.floor(this.x);
		int by = MathHelper.floor(this.y);
		int bz = MathHelper.floor(this.z);

		if (this.world.isBlockLoaded(bx, by, bz)) {
			boolean foundFooting = false;

			while (!foundFooting && by > 0) {
				if (this.world.getBlockMaterial(new TilePos(bx, by - 1, bz)).blocksMotion()) {
					foundFooting = true;
				} else {
					this.y--;
					by--;
				}
			}

			if (foundFooting) {
				this.setPos(this.x, this.y, this.z);
				if (this.world.getCubes(this, this.bb).isEmpty() && !this.world.getIsAnyLiquid(this.bb)) {
					landed = true;
				}
			}
		}

		if (!landed) {
			this.setPos(fromX, fromY, fromZ);
			return false;
		}

		int trail = 128;
		for (int i = 0; i < trail; i++) {
			double along = i / (trail - 1.0);
			float dx = (this.random.nextFloat() - 0.5F) * 0.2F;
			float dy = (this.random.nextFloat() - 0.5F) * 0.2F;
			float dz = (this.random.nextFloat() - 0.5F) * 0.2F;
			double px = fromX + (this.x - fromX) * along
				+ (this.random.nextDouble() - 0.5) * this.bbWidth * 2.0;
			double py = fromY + (this.y - fromY) * along + this.random.nextDouble() * this.bbHeight;
			double pz = fromZ + (this.z - fromZ) * along
				+ (this.random.nextDouble() - 0.5) * this.bbWidth * 2.0;
			this.world.spawnParticle("spell", px, py, pz, dx, dy, dz, 0, false);
		}

		this.world.playSoundEffect(null, net.minecraft.core.sound.SoundCategory.WORLD_SOUNDS,
			fromX, fromY, fromZ, "mob.endermen.portal", 1.0F, 1.0F);
		this.world.playSoundAtEntity(null, this, "mob.endermen.portal", 1.0F, 1.0F);
		return true;
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
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.LICH);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HUNTER);
	}
}
