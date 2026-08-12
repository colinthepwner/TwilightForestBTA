package com.twilightforest.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.item.TFItems;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MobTFNaga extends MobMonster {

	private static final int LEASH_X = 46;
	private static final int LEASH_Y = 7;
	private static final int LEASH_Z = 46;

	private int segments;
	private int segmentHealth;
	private MobTFNagaSegment[] body;

	private int homeX;
	private int homeY;
	private int homeZ;

	private int circleCount = 15;
	private int intimidateTimer;
	private int crumblePlayerTimer;
	private int chargeCount;
	private boolean clockwise;

	public MobTFNaga(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "nagahead");
		this.setSize(1.75F, 3.0F);
		this.moveSpeed = 0.6F;

		this.footSize = 2.0F;
		this.attackStrength = 6;
		this.scoreValue = 217;

		this.segmentHealth = this.getMaxHealth() / 10;
		this.setSegmentsPerHealth();

		this.mobDrops.add(new WeightedRandomLootObject(TFItems.NAGA_SCALE.getDefaultStack(), 6, 11));
	}

	@Override
	public int getMaxHealth() {
		if (this.world == null) {
			return 200;
		}
		switch (this.world.getDifficulty()) {
			case EASY: return 120;
			case HARD: return 250;
			default: return 200;
		}
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void causeFallDamage(float distance) {
	}

	protected int setSegmentsPerHealth() {
		int oldSegments = this.segments;
		int health = this.getHealth();
		int newSegments = this.segmentHealth <= 0
			? oldSegments
			: health / this.segmentHealth + (health > 0 ? 2 : 0);

		if (!this.world.isClientSide && newSegments != oldSegments) {
			if (newSegments < oldSegments) {
				for (int i = newSegments; i < oldSegments; i++) {
					if (this.body != null && i < this.body.length && this.body[i] != null) {
						this.body[i].selfDestruct();
					}
				}
			}
			this.segments = newSegments;
			this.setMovementFactorPerSegments();
		}

		return this.segments;
	}

	protected void setMovementFactorPerSegments() {
		this.speed = 0.6F - this.segments / 12.0F * 0.2F;
		this.flySpeed = this.speed / 2.0F;

		for (int i = 0; i < this.segments; i++) {
			if (this.body != null && i < this.body.length && this.body[i] != null) {
				this.body[i].speed = this.speed * 1.25F;
				this.body[i].flySpeed = this.flySpeed * 1.25F;
			}
		}
	}

	protected void spawnBodySegments() {
		if (this.world.isClientSide) {
			return;
		}

		this.body = new MobTFNagaSegment[this.segments];
		for (int i = 0; i < this.segments; i++) {
			this.body[i] = new MobTFNagaSegment(this.world, this, i);
			this.body[i].moveTo(this.x + 0.1 * i, this.y + 0.5, this.z + 0.1 * i,
				this.random.nextFloat() * 360.0F, 0.0F);
			this.world.entityJoinedWorld(this.body[i]);
		}
	}

	protected void pullSegments() {
		if (this.body == null || this.body.length < this.segments) {
			this.spawnBodySegments();
		}

		if (this.world.isClientSide || this.body == null || this.body.length == 0) {
			return;
		}

		this.body[0].pullTowards(this);
		for (int i = 1; i < this.segments && i < this.body.length; i++) {
			this.body[i].pullTowards(this.body[i - 1]);
		}
	}

	@Override
	protected void updateAI() {
		if (this.horizontalCollision && this.hasTarget()) {
			this.breakNearbyBlocks();
		}

		if (this.target == null) {
			this.target = this.findTarget();
			if (this.target != null) {
				this.acquireNewPath();
			}
		} else if (!this.target.isAlive()) {
			this.target = null;
		} else {
			float targetDistance = this.target.distanceTo(this);
			if (targetDistance > 80.0F) {
				this.target = null;
			} else if (this.canEntityBeSeen(this.target)) {
				this.attackEntity(this.target, targetDistance);
			}
		}

		if (!this.hasPath()) {
			this.acquireNewPath();
		}

		boolean inWater = this.isInWater();
		boolean inLava = this.isInLava();
		Vector3dc next = this.hasPath() ? this.pathToEntity.getPos(this) : null;
		double reach = this.bbWidth * 4.0F;

		while (next != null && next.distanceSquared(this.x, next.y(), this.z) < reach * reach) {
			this.pathToEntity.next();
			if (this.pathToEntity.isDone()) {
				next = null;
				this.pathToEntity = null;
			} else {
				next = this.pathToEntity.getPos(this);
			}
		}

		this.isJumping = false;
		if (next != null) {
			double dx = next.x() - this.x;
			double dz = next.z() - this.z;
			double dist = MathHelper.sqrt(dx * dx + dz * dz);
			int floor = MathHelper.floor(this.bb.minY + 0.5);
			double dy = next.y() - floor;

			float wantYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
			float turn = wantYaw - this.yRot;

			this.moveForward = this.moveSpeed;

			if (dist > 4.0 && this.chargeCount == 0) {
				this.moveStrafing = MathHelper.cos(this.walkAnimPos * 0.3F) * this.moveSpeed * 0.6F;
			}

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

		if (this.intimidateTimer > 0 && this.hasTarget()) {
			this.lookAt(this.target, 30.0F, 30.0F);
			this.moveForward = 0.1F;
		}

		if (this.random.nextFloat() < 0.8F && (inWater || inLava)) {
			this.isJumping = true;
		}

		this.pullSegments();
	}

	protected void acquireNewPath() {
		if (!this.hasTarget()) {
			this.wanderRandomly();
			return;
		}

		if (this.intimidateTimer > 0) {
			this.pathToEntity = null;
			this.intimidateTimer--;
			if (this.intimidateTimer == 0) {
				this.clockwise = !this.clockwise;

				if (this.target.bb.minY() > this.bb.maxY()) {
					this.doCrumblePlayer();
				} else {
					this.doCharge();
				}
			}
			return;
		}

		if (this.crumblePlayerTimer > 0) {
			this.pathToEntity = null;
			this.crumblePlayerTimer--;
			this.crumbleBelowTarget(2);
			this.crumbleBelowTarget(3);
			if (this.crumblePlayerTimer == 0) {
				this.doCharge();
			}
		}

		if (this.chargeCount > 0) {
			this.chargeCount--;

			Vector3dc point = this.findCirclePoint(this.target, 14.0, Math.PI);
			this.pathToEntity = this.world.getEntityPathToXYZ(
				this, (int) point.x(), (int) point.y(), (int) point.z(), 40.0F);
			if (this.chargeCount == 0) {
				this.doCircle();
			}
		}

		if (this.circleCount > 0) {
			this.circleCount--;

			double radius = this.circleCount % 2 == 0 ? 12.0 : 14.0;
			double rotation = 1.0;
			if (this.circleCount > 1 && this.circleCount < 3) {
				radius = 16.0;
			}
			if (this.circleCount == 1) {
				rotation = 0.1;
			}

			Vector3dc point = this.findCirclePoint(this.target, radius, rotation);
			this.pathToEntity = this.world.getEntityPathToXYZ(
				this, (int) point.x(), (int) point.y(), (int) point.z(), 40.0F);
			if (this.circleCount == 0) {
				this.doIntimidate();
			}
		}
	}

	protected Vector3dc findCirclePoint(Entity toCircle, double radius, double rotation) {
		double vecX = this.x - toCircle.x;
		double vecZ = this.z - toCircle.z;
		float angle = (float) Math.atan2(vecZ, vecX);
		angle = (float) (angle + (this.clockwise ? rotation : -rotation));

		return new Vector3d(
			toCircle.x + MathHelper.cos(angle) * radius,
			this.bb.minY(),
			toCircle.z + MathHelper.sin(angle) * radius);
	}

	protected void doCircle() {
		this.circleCount += 10 + this.random.nextInt(10);
		this.goNormal();
	}

	protected void doCrumblePlayer() {
		this.crumblePlayerTimer = 20 + this.random.nextInt(20);
		this.goSlow();
	}

	protected void doCharge() {
		this.chargeCount = 4;
		this.goFast();
	}

	protected void doIntimidate() {
		this.intimidateTimer += 15 + this.random.nextInt(10);
		this.goSlow();
	}

	protected void goSlow() {
		this.moveForward = 0.0F;
		this.moveStrafing = 0.0F;
		this.moveSpeed = 0.1F;
		this.pathToEntity = null;
	}

	protected void goNormal() {
		this.moveSpeed = 0.6F;
	}

	protected void goFast() {
		this.moveSpeed = 1.0F;
	}

	float chaseSpeed() {
		return this.moveSpeed;
	}

	protected Entity findTarget() {
		Player player = this.world.getClosestPlayerToEntity(this, 32.0);
		return player != null && this.canEntityBeSeen(player) ? player : null;
	}

	public boolean hasTarget() {
		return this.target != null;
	}

	protected void wanderRandomly() {
		this.goNormal();

		int tx = -1;
		int ty = -1;
		int tz = -1;
		float bestWeight = -99999.0F;
		boolean found = false;

		for (int i = 0; i < 10; i++) {
			int dx = MathHelper.floor(this.x + this.random.nextInt(21) - 6.0);
			int dy = MathHelper.floor(this.y + this.random.nextInt(7) - 3.0);
			int dz = MathHelper.floor(this.z + this.random.nextInt(21) - 6.0);

			if (this.isLeashed() && outsideLeash(dx, dy, dz)) {
				dx = this.homeX + this.random.nextInt(21) - this.random.nextInt(21);
				dy = this.homeY + this.random.nextInt(7) - this.random.nextInt(7);
				dz = this.homeZ + this.random.nextInt(21) - this.random.nextInt(21);
			}

			float weight = this.getBlockPathWeight(new TilePos(dx, dy, dz));
			if (weight > bestWeight) {
				bestWeight = weight;
				tx = dx;
				ty = dy;
				tz = dz;
				found = true;
			}
		}

		if (found) {
			this.pathToEntity = this.world.getEntityPathToXYZ(this, tx, ty, tz, 80.0F);
		}
	}

	private boolean outsideLeash(int x, int y, int z) {
		return x > this.homeX + LEASH_X || x < this.homeX - LEASH_X
			|| z > this.homeZ + LEASH_Z || z < this.homeZ - LEASH_Z
			|| y > this.homeY + LEASH_Y || y < this.homeY - LEASH_Y;
	}

	@Override
	protected float getBlockPathWeight(TilePosc pos) {
		if (!this.isLeashed()) {
			return 0.0F;
		}
		int distX = Math.abs(this.homeX - pos.x());
		int distY = Math.abs(this.homeY - pos.y());
		int distZ = Math.abs(this.homeZ - pos.z());

		return distX <= LEASH_X - 10 && distY <= LEASH_Y && distZ <= LEASH_Z - 10
			? 0.0F
			: Float.MIN_VALUE;
	}

	@Override
	protected void attackEntity(Entity toAttack, float distance) {
		if (this.attackTime <= 0 && distance < 4.0F
			&& toAttack.bb.maxY() > this.bb.minY() - 2.5
			&& toAttack.bb.minY() < this.bb.maxY() + 2.5) {

			this.attackTime = 20;
			toAttack.hurt(this, this.attackStrength, DamageType.COMBAT);

			if (this.moveSpeed > 0.8) {
				toAttack.push(
					-MathHelper.sin(this.yRot * (float) Math.PI / 180.0F),
					0.1,
					MathHelper.cos(this.yRot * (float) Math.PI / 180.0F));
			}
		}
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		if (!super.hurt(attacker, damage, type)) {
			return false;
		}

		this.setSegmentsPerHealth();

		if (this.passenger != attacker && this.vehicle != attacker && attacker != this) {
			this.target = attacker;
		}
		return true;
	}

	protected void breakNearbyBlocks() {
		int minX = MathHelper.floor(this.bb.minX() - 0.5);
		int minY = MathHelper.floor(this.bb.minY() + 1.01);
		int minZ = MathHelper.floor(this.bb.minZ() - 0.5);
		int maxX = MathHelper.floor(this.bb.maxX() + 0.5);
		int maxY = MathHelper.floor(this.bb.maxY() + 0.001);
		int maxZ = MathHelper.floor(this.bb.maxZ() + 0.5);

		if (!this.world.areBlocksLoaded(minX, minY, minZ, maxX, maxY, maxZ)) {
			return;
		}

		for (int bx = minX; bx <= maxX; bx++) {
			for (int by = minY; by <= maxY; by++) {
				for (int bz = minZ; bz <= maxZ; bz++) {
					if (this.world.getBlockId(bx, by, bz) > 0) {
						this.breakBlock(bx, by, bz);
					}
				}
			}
		}
	}

	protected void crumbleBelowTarget(int range) {
		if (this.target == null) {
			return;
		}

		int floor = (int) this.bb.minY();
		int targetY = (int) this.target.bb.minY();
		if (targetY <= floor) {
			return;
		}

		int dx = (int) this.target.x + this.random.nextInt(range) - this.random.nextInt(range);
		int dz = (int) this.target.z + this.random.nextInt(range) - this.random.nextInt(range);
		int dy = targetY - this.random.nextInt(range)
			+ this.random.nextInt(range > 1 ? range - 1 : range);
		if (dy <= floor) {
			dy = targetY;
		}

		if (this.world.getBlockId(dx, dy, dz) == 0) {
			return;
		}

		this.breakBlock(dx, dy, dz);

		for (int i = 0; i < 20; i++) {
			double vx = this.random.nextGaussian() * 0.02;
			double vy = this.random.nextGaussian() * 0.02;
			double vz = this.random.nextGaussian() * 0.02;
			this.world.spawnParticle("crit",
				this.x + this.random.nextFloat() * this.bbWidth * 2.0F - this.bbWidth,
				this.y + this.random.nextFloat() * this.bbHeight,
				this.z + this.random.nextFloat() * this.bbWidth * 2.0F - this.bbWidth,
				vx, vy, vz, 0, false);
		}
	}

	protected void breakBlock(int bx, int by, int bz) {
		TilePos pos = new TilePos(bx, by, bz);
		Block<?> block = this.world.getBlockType(pos);
		if (block == null) {
			return;
		}

		int meta = this.world.getBlockData(pos);
		TileEntity tile = this.world.getTileEntity(pos);
		block.dropWithCause(this.world, EnumDropCause.WORLD, pos, meta, tile, null);
		this.world.setBlockWithNotify(bx, by, bz, 0);
	}

	public void setHome(int x, int y, int z) {
		this.homeX = x;
		this.homeY = y;
		this.homeZ = z;
	}

	public boolean isLeashed() {
		return this.homeX != 0 && this.homeY != 0 && this.homeZ != 0;
	}

	@Override
	public void tick() {
		this.despawnIfInvalid();

		if (this.deathTime > 0) {
			for (int i = 0; i < 5; i++) {
				double vx = this.random.nextGaussian() * 0.02;
				double vy = this.random.nextGaussian() * 0.02;
				double vz = this.random.nextGaussian() * 0.02;
				String explosion = this.random.nextBoolean() ? "hugeexplosion" : "explode";
				this.world.spawnParticle(explosion,
					this.x + this.random.nextFloat() * this.bbWidth * 2.0F - this.bbWidth,
					this.y + this.random.nextFloat() * this.bbHeight,
					this.z + this.random.nextFloat() * this.bbWidth * 2.0F - this.bbWidth,
					vx, vy, vz, 0, false);
			}
		}

		super.tick();
	}

	protected void despawnIfInvalid() {
		if (this.world.isClientSide) {
			return;
		}

		if (!this.world.getDifficulty().canHostileMobsSpawn()) {
			this.despawnMe();
			return;
		}

		for (int i = 0; i < this.segments; i++) {
			if (this.body != null && i < this.body.length
				&& this.body[i] != null && this.body[i].removed) {
				this.despawnMe();
				return;
			}
		}

		if (this.isLeashed()) {
			int distX = Math.abs((int) (this.homeX - this.x));
			int distY = Math.abs((int) (this.homeY - this.y));
			int distZ = Math.abs((int) (this.homeZ - this.z));
			if (distX > LEASH_X || distY > LEASH_Y || distZ > LEASH_Z) {
				this.despawnMe();
			}
		}
	}

	protected void despawnMe() {
		if (this.isLeashed()) {
			this.world.setBlockWithNotify(this.homeX, this.homeY, this.homeZ,
				TFBlocks.BOSS_SPAWNER.id());
		}
		this.remove();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("HomeX", this.homeX);
		tag.putInt("HomeY", this.homeY);
		tag.putInt("HomeZ", this.homeZ);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setHome(tag.getInteger("HomeX"), tag.getInteger("HomeY"), tag.getInteger("HomeZ"));
		this.segmentHealth = this.getMaxHealth() / 10;
		this.setSegmentsPerHealth();
	}

	@Override
	public void onDeath(net.minecraft.core.entity.Entity killer) {
		super.onDeath(killer);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.NAGA);
		com.twilightforest.achievement.TFAchievements.award(killer,
			com.twilightforest.achievement.TFAchievements.HUNTER);
	}
}
