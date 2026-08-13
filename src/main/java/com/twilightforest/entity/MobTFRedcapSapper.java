package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;

public class MobTFRedcapSapper extends MobTFRedcap {

	public static final int CHARGES = 3;

	private static final double PLANT_RANGE_SQ = 25.0;

	private static final int LIT_CHECK_RANGE = 8;

	private static final int UNLIT_CHECK_RANGE = 5;

	public MobTFRedcapSapper(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "redcapsapper");
		this.setTntLeft(CHARGES);
	}

	@Override
	public int getMaxHealth() {
		return 30;
	}

	@Override
	protected boolean tickExplosives() {
		if (super.tickExplosives()) {
			return true;
		}
		this.tryPlant();

		return false;
	}

	private void tryPlant() {
		Entity target = this.getTarget();
		if (target == null
			|| this.getTntLeft() <= 0
			|| this.distanceToSqr(target.x, target.y, target.z) >= PLANT_RANGE_SQ
			|| this.isTargetLookingAtMe()
			|| this.isLitTNTNearby(LIT_CHECK_RANGE)
			|| this.findTNTBlockNearby(UNLIT_CHECK_RANGE) != null) {
			return;
		}

		int bx = MathHelper.floor(this.x);
		int by = MathHelper.floor(this.y);
		int bz = MathHelper.floor(this.z);

		this.hold(HELD_TNT);

		if (this.world.isAirBlock(bx, by, bz)) {
			this.setTntLeft(this.getTntLeft() - 1);
			this.world.setBlockWithNotify(bx, by, bz, Blocks.TNT.id());
		}

		this.hold(this.getPick());
	}
}
