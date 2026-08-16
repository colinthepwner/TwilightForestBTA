package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobTFTowerTermite extends MobMonster {

	private static final int MAX_HEALTH = 15;

	private static final int ATTACK_STRENGTH = 5;

	private static final float MOVE_SPEED = 0.82F;

	private static final double SIGHT_RANGE = 8.0;

	private static final int SUMMON_DELAY = 20;

	private static final int SUMMON_RANGE_H = 10;
	private static final int SUMMON_RANGE_V = 5;

	private int allySummonCooldown;

	public MobTFTowerTermite(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "towertermite");
		this.setSize(0.3F, 0.7F);
		this.moveSpeed = MOVE_SPEED;
		this.attackStrength = ATTACK_STRENGTH;
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected boolean makeStepSound() {
		return false;
	}

	@Override
	@Nullable
	protected Entity findPlayerToAttack() {
		return this.world.getClosestPlayerToEntity(this, SIGHT_RANGE);
	}

	@Override
	public void tick() {
		this.yBodyRot = this.yRot;
		super.tick();
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		if (this.allySummonCooldown <= 0 && attacker != null) {
			this.allySummonCooldown = SUMMON_DELAY;
		}
		return super.hurt(attacker, damage, type);
	}

	@Override
	protected void updateAI() {
		super.updateAI();

		if (this.allySummonCooldown > 0) {
			this.allySummonCooldown--;
			if (this.allySummonCooldown == 0) {
				this.tryToSummonAllies();
			}
		}

		if (this.getTarget() == null && !this.hasPath()) {
			this.tryToBurrow();
		}
	}

	protected void tryToSummonAllies() {
		int sx = MathHelper.floor(this.x);
		int sy = MathHelper.floor(this.y);
		int sz = MathHelper.floor(this.z);

		boolean stopSummoning = false;
		int dy = 0;
		while (!stopSummoning && dy <= SUMMON_RANGE_V && dy >= -SUMMON_RANGE_V) {
			int dx = 0;
			while (!stopSummoning && dx <= SUMMON_RANGE_H && dx >= -SUMMON_RANGE_H) {
				int dz = 0;
				while (!stopSummoning && dz <= SUMMON_RANGE_H && dz >= -SUMMON_RANGE_H) {
					if (this.world.getBlockId(sx + dx, sy + dy, sz + dz)
						== TFBlocks.TOWER_WOOD_INFESTED.id()) {

						this.world.playBlockEvent(null, 2001, sx + dx, sy + dy, sz + dz,
							TFBlocks.TOWER_WOOD_INFESTED.id());
						this.world.setBlockWithNotify(sx + dx, sy + dy, sz + dz, 0);
						spawnFromBlock(this.world, sx + dx, sy + dy, sz + dz);

						if (this.random.nextBoolean()) {
							stopSummoning = true;
							break;
						}
					}
					dz = nextOffset(dz);
				}
				dx = nextOffset(dx);
			}
			dy = nextOffset(dy);
		}
	}

	public static int nextOffset(int offset) {
		return offset <= 0 ? 1 - offset : -offset;
	}

	protected void tryToBurrow() {
		int x = MathHelper.floor(this.x);
		int y = MathHelper.floor(this.y + 0.5);
		int z = MathHelper.floor(this.z);

		Side face = Side.sides[this.random.nextInt(Side.sides.length)];
		x += face.offsetX();
		y += face.offsetY();
		z += face.offsetZ();

		if (this.world.getBlockId(x, y, z) == TFBlocks.TOWER_WOOD.id()) {
			this.world.setBlockWithNotify(x, y, z, TFBlocks.TOWER_WOOD_INFESTED.id());
			this.spawnExplosionParticle();
			this.remove();
		}
	}

	public static void spawnFromBlock(World world, int x, int y, int z) {
		if (world.isClientSide) {
			return;
		}
		MobTFTowerTermite termite = new MobTFTowerTermite(world);
		termite.moveTo(x + 0.5, y, z + 0.5, 0.0F, 0.0F);
		world.entityJoinedWorld(termite);
		termite.spawnExplosionParticle();
	}

	@Override
	public String getLivingSound() {
		return "mob.spider";
	}

	@Override
	protected String getHurtSound() {
		return "mob.spider";
	}

	@Override
	protected String getDeathSound() {
		return "mob.spiderdeath";
	}

	public int getAllySummonCooldown() {
		return this.allySummonCooldown;
	}
}
