package com.twilightforest.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.entity.ai.TFPathfinder;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityPrimedTNT;
import net.minecraft.core.entity.IItemHolding;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePos;
import org.joml.primitives.AABBd;

import java.util.HashSet;
import java.util.Set;

public class MobTFRedcap extends MobMonster implements IItemHolding {

	private static final ItemStack HELD_PICK = new ItemStack(Items.TOOL_PICKAXE_IRON, 1);

	static final ItemStack HELD_STRIKER = new ItemStack(Items.TOOL_FIRESTRIKER_IRON, 1);

	static final ItemStack HELD_TNT = new ItemStack(Blocks.TNT, 1);

	private static final int LIGHT_RANGE = 8;

	private static final double LIGHT_REACH_SQ = 2.4;

	private static final int LIGHT_DELAY = 20;

	private static final double FLEE_TRIGGER = 2.0;

	private static final int FLEE_XZ = 16;
	private static final int FLEE_Y = 7;

	private static final int FLEE_TRIES = 10;

	private static final int FLEE_AVOID_XZ = 2;
	private static final int FLEE_AVOID_Y = 1;

	protected final boolean lefty;

	protected boolean shy;

	private ItemStack heldItem = HELD_PICK;

	private int tntLeft;

	private boolean lighting;
	private int tntX;
	private int tntY;
	private int tntZ;
	private int lightDelay;

	private boolean fleeing;

	public MobTFRedcap(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "redcap");
		this.setSize(0.9F, 1.4F);
		this.moveSpeed = 0.5F;
		this.attackStrength = 2;
		this.lefty = this.random.nextBoolean();
		this.shy = true;

		this.mobDrops.add(new WeightedRandomLootObject(Items.COAL.getDefaultStack(), 0, 2));
	}

	@Override
	protected void dropDeathItems() {
		super.dropDeathItems();

		if (this.random.nextFloat() < EQUIPMENT_DROP_CHANCE) {
			this.dropItem(Items.TOOL_PICKAXE_IRON.getDefaultStack(), 0.0F);
		}
		if (this.random.nextFloat() < EQUIPMENT_DROP_CHANCE) {
			this.dropItem(Items.ARMOR_BOOTS_IRON.getDefaultStack(), 0.0F);
		}
	}

	private static final float EQUIPMENT_DROP_CHANCE = 0.2F;

	@Override
	public int getMaxHealth() {
		return 20;
	}

	@Override
	public ItemStack getHeldItem() {
		return this.heldItem;
	}

	@Override
	public void setHeldItem(ItemStack itemStack) {
	}

	protected ItemStack getPick() {
		return HELD_PICK;
	}

	void hold(ItemStack itemStack) {
		this.heldItem = itemStack;
	}

	public int getTntLeft() {
		return this.tntLeft;
	}

	public void setTntLeft(int tntLeft) {
		this.tntLeft = tntLeft;
	}

	@Override
	public boolean isLeftHanded() {
		return this.lefty;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.redcap.redcap";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForest.MOD_ID + ":mob.tf.redcap.hurt";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForest.MOD_ID + ":mob.tf.redcap.die";
	}

	@Override
	protected void updateAI() {
		if (this.tickExplosives()) {
			return;
		}

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

	protected boolean tickExplosives() {
		if (this.tickFlee()) {
			return true;
		}
		return this.tickLightTNT();
	}

	private boolean tickFlee() {
		if (this.fleeing) {

			if (this.hasPath()) {
				this.steerAlongPath();
				return true;
			}
			this.fleeing = false;
		}

		EntityPrimedTNT charge = this.findLitCharge(FLEE_TRIGGER);
		if (charge == null) {
			return false;
		}

		Path route = this.routeAwayFrom(charge);
		if (route == null) {
			return false;
		}

		this.pathToEntity = route;
		this.fleeing = true;

		this.moveSpeed = 0.8F;
		this.target = null;
		this.steerAlongPath();
		return true;
	}

	private Path routeAwayFrom(Entity charge) {
		double currentSq = this.distanceToSqr(charge.x, charge.y, charge.z);

		int bestX = 0;
		int bestY = 0;
		int bestZ = 0;
		float bestWeight = -99999.0F;
		boolean found = false;

		for (int i = 0; i < FLEE_TRIES; i++) {
			int cx = MathHelper.floor(this.x + this.random.nextInt(FLEE_XZ * 2 + 1) - FLEE_XZ);
			int cy = MathHelper.floor(this.y + this.random.nextInt(FLEE_Y * 2 + 1) - FLEE_Y);
			int cz = MathHelper.floor(this.z + this.random.nextInt(FLEE_XZ * 2 + 1) - FLEE_XZ);

			double dx = cx + 0.5 - charge.x;
			double dy = cy + 0.5 - charge.y;
			double dz = cz + 0.5 - charge.z;
			if (dx * dx + dy * dy + dz * dz <= currentSq) {
				continue;
			}

			float weight = this.getBlockPathWeight(new TilePos(cx, cy, cz));
			if (weight > bestWeight) {
				bestWeight = weight;
				bestX = cx;
				bestY = cy;
				bestZ = cz;
				found = true;
			}
		}

		if (!found) {
			return null;
		}

		return TFPathfinder.findPath(this.world, this, bestX, bestY, bestZ, 1.0,
			cellsAround(charge.x, charge.y, charge.z, FLEE_AVOID_XZ, FLEE_AVOID_Y));
	}

	private boolean tickLightTNT() {
		if (this.lighting) {

			if (this.world.getBlockId(this.tntX, this.tntY, this.tntZ) == Blocks.TNT.id()) {
				this.walkToAndLight();
				return true;
			}

			this.lighting = false;
			this.pathToEntity = null;
			this.hold(this.getPick());
			this.lightDelay = LIGHT_DELAY;
			return false;
		}

		if (this.lightDelay > 0) {
			this.lightDelay--;
			return false;
		}

		TilePos charge = this.findTNTBlockNearby(LIGHT_RANGE);
		if (charge == null) {
			return false;
		}

		this.tntX = charge.x;
		this.tntY = charge.y;
		this.tntZ = charge.z;
		this.lighting = true;
		this.hold(HELD_STRIKER);
		this.walkToAndLight();
		return true;
	}

	private void walkToAndLight() {
		this.lookAtBlock(this.tntX, this.tntY, this.tntZ);

		if (this.distanceToSqr(this.tntX + 0.5, this.tntY + 0.5, this.tntZ + 0.5) < LIGHT_REACH_SQ) {

			Blocks.TNT.getLogic().ignite(this.world, new TilePos(this.tntX, this.tntY, this.tntZ), true);
			this.pathToEntity = null;
			return;
		}

		if (!this.hasPath()) {
			this.pathToEntity = this.world.getEntityPathToXYZ(this,
				this.tntX, this.tntY, this.tntZ, 16.0F);
		}
		this.steerAlongPath();
	}

	protected TilePos findTNTBlockNearby(int range) {
		int ox = MathHelper.floor(this.x);
		int oy = MathHelper.floor(this.y);
		int oz = MathHelper.floor(this.z);

		for (int x = -range; x <= range; x++) {
			for (int y = -range; y <= range; y++) {
				for (int z = -range; z <= range; z++) {
					if (this.world.getBlockId(ox + x, oy + y, oz + z) == Blocks.TNT.id()) {
						return new TilePos(ox + x, oy + y, oz + z);
					}
				}
			}
		}
		return null;
	}

	protected EntityPrimedTNT findLitCharge(double range) {
		AABBd box = MathHelper.aabbGrow(this.bb, range, range, range, new AABBd());

		EntityPrimedTNT closest = null;
		double closestSq = Double.MAX_VALUE;
		for (EntityPrimedTNT charge : this.world.getEntitiesWithinAABB(EntityPrimedTNT.class, box)) {
			double distSq = this.distanceToSqr(charge.x, charge.y, charge.z);
			if (distSq < closestSq) {
				closestSq = distSq;
				closest = charge;
			}
		}
		return closest;
	}

	protected boolean isLitTNTNearby(int range) {
		return this.findLitCharge(range) != null;
	}

	private static Set<Long> cellsAround(double x, double y, double z, int spreadXZ, int spreadY) {
		int cx = MathHelper.floor(x);
		int cy = MathHelper.floor(y);
		int cz = MathHelper.floor(z);

		Set<Long> cells = new HashSet<>();
		for (int dx = -spreadXZ; dx <= spreadXZ; dx++) {
			for (int dy = -spreadY; dy <= spreadY; dy++) {
				for (int dz = -spreadXZ; dz <= spreadXZ; dz++) {
					cells.add(TFPathfinder.packKey(cx + dx, cy + dy, cz + dz));
				}
			}
		}
		return cells;
	}

	private void steerAlongPath() {

		if (this.pathToEntity == null || this.pathToEntity.isDone()) {
			this.pathToEntity = null;
			return;
		}

		org.joml.Vector3dc next = this.pathToEntity.getPos(this);
		double reach = this.bbWidth * 2.0F;

		while (next != null && next.distanceSquared(this.x, next.y(), this.z) < reach * reach) {
			this.pathToEntity.next();
			if (this.pathToEntity.isDone()) {
				this.pathToEntity = null;
				next = null;
			} else {
				next = this.pathToEntity.getPos(this);
			}
		}

		this.isJumping = false;
		if (next == null) {
			return;
		}

		int floor = MathHelper.floor(this.bb.minY + 0.5);
		double dx = next.x() - this.x;
		double dz = next.z() - this.z;
		double dy = next.y() - floor;

		float turn = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F - this.yRot;
		this.moveForward = this.moveSpeed;

		while (turn < -180.0F) {
			turn += 360.0F;
		}
		while (turn >= 180.0F) {
			turn -= 360.0F;
		}
		if (turn > 30.0F) {
			turn = 30.0F;
		}
		if (turn < -30.0F) {
			turn = -30.0F;
		}

		this.yRot += turn;
		if (dy > 0.0) {
			this.isJumping = true;
		}
	}

	private void lookAtBlock(int bx, int by, int bz) {
		double dx = bx + 0.5 - this.x;
		double dz = bz + 0.5 - this.z;
		double dy = by + 0.5 - (this.y + this.getHeadHeight());
		double flat = MathHelper.sqrt(dx * dx + dz * dz);

		float wantYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
		float turn = wantYaw - this.yRot;
		while (turn < -180.0F) {
			turn += 360.0F;
		}
		while (turn >= 180.0F) {
			turn -= 360.0F;
		}
		this.yRot += MathHelper.clamp(turn, -30.0F, 30.0F);
		this.xRot = (float) (-(Math.atan2(dy, flat) * 180.0 / Math.PI));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("TNTLeft", this.getTntLeft());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setTntLeft(tag.getInteger("TNTLeft"));
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HILL_1);
	}
}
