package com.twilightforest.entity;

import net.minecraft.core.entity.MobFlying;
import net.minecraft.core.entity.animal.AmbientCreature;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

public class MobTFMobileFirefly extends MobFlying implements AmbientCreature {

	private TilePos flightTarget;

	public MobTFMobileFirefly(World world) {
		super(world);
		this.setSize(0.5F, 0.5F);
	}

	@Override
	public int getMaxHealth() {
		return 6;
	}

	@Override
	protected String getHurtSound() {
		return "mob.bat.hurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.bat.death";
	}

	@Override
	protected void causeFallDamage(float distance) {

	}

	@Override
	public void tick() {
		super.tick();

		this.yd *= 0.6D;
	}

	@Override
	protected void updateAI() {
		if (this.flightTarget != null
			&& (!this.world.isAirBlock(this.flightTarget.x, this.flightTarget.y, this.flightTarget.z)
				|| this.flightTarget.y < 1)) {
			this.flightTarget = null;
		}

		if (this.flightTarget == null
			|| this.random.nextInt(30) == 0
			|| distanceToTargetSq() < 4.0D) {
			this.flightTarget = new TilePos(
				(int) this.x + this.random.nextInt(7) - this.random.nextInt(7),
				(int) this.y + this.random.nextInt(6) - 2,
				(int) this.z + this.random.nextInt(7) - this.random.nextInt(7));
		}

		double dx = this.flightTarget.x + 0.5D - this.x;
		double dy = this.flightTarget.y + 0.1D - this.y;
		double dz = this.flightTarget.z + 0.5D - this.z;
		double speed = 0.05D;

		this.xd += (Math.signum(dx) * 0.5D - this.xd) * speed;

		this.yd += (Math.signum(dy) * 0.7D - this.yd) * speed * 2.0D;
		this.zd += (Math.signum(dz) * 0.5D - this.zd) * speed;

		float toTarget = (float) (Math.atan2(this.zd, this.xd) * 180.0D / Math.PI) - 90.0F;
		this.yRot += MathHelper.wrapDegrees(toTarget - this.yRot);
	}

	public float getGlowBrightness() {
		return (float) Math.sin(this.tickCount / 7.0D) + 1.0F;
	}

	private double distanceToTargetSq() {
		double dx = this.flightTarget.x - this.x;
		double dy = this.flightTarget.y - this.y;
		double dz = this.flightTarget.z - this.z;
		return dx * dx + dy * dy + dz * dz;
	}
}
