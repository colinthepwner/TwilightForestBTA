package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.world.World;

public class MobTFPinchBeetle extends MobMonster {

	private static final float WIDTH_FREE = 1.2F;
	private static final float HEIGHT_FREE = 1.1F;
	private static final float WIDTH_CARRYING = 1.9F;
	private static final float HEIGHT_CARRYING = 2.0F;

	private boolean wasCarrying;

	public MobTFPinchBeetle(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "pinchbeetle");
		this.setSize(WIDTH_FREE, HEIGHT_FREE);

		this.moveSpeed = 0.7F;
		this.attackStrength = 8;
	}

	@Override
	public int getMaxHealth() {
		return 40;
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if (this.passenger == null && target.vehicle == null && this.attackTime <= 0) {
			target.startRiding(this);
		}
		super.attackEntity(target, distance);
	}

	@Override
	protected void updateAI() {
		super.updateAI();
		boolean carrying = this.passenger != null;
		if (carrying != this.wasCarrying) {
			this.wasCarrying = carrying;
			if (carrying) {
				this.setSize(WIDTH_CARRYING, HEIGHT_CARRYING);
			} else {
				this.setSize(WIDTH_FREE, HEIGHT_FREE);
			}
		}
	}
}
