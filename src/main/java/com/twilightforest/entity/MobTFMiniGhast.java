package com.twilightforest.entity;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.joml.Vector3dc;

public class MobTFMiniGhast extends MobTFTowerGhast {

	private static final int MAX_PER_CHUNK = 16;

	private static final float POINT_BLANK = 3.5F;

	private static final double STARE_CONE = 0.025;

	public MobTFMiniGhast(World world) {
		super(world);
		this.setSize(1.1F, 1.5F);

		this.aggroRange = 16.0F;
		this.stareRange = 8.0F;
		this.wanderFactor = 4.0F;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return MAX_PER_CHUNK;
	}

	@Override
	protected boolean shouldAttackPlayer(Player player) {
		if (isWearingPumpkin(player)) {
			return false;
		}

		if (player.distanceTo(this) <= POINT_BLANK && player.canEntityBeSeen(this)) {
			return true;
		}

		Vector3dc look = player.getViewVector(1.0F);
		double toX = this.x - player.x;
		double toY = this.bb.minY + this.bbHeight / 2.0F - (player.y + player.getHeadHeight());
		double toZ = this.z - player.z;
		double distance = Math.sqrt(toX * toX + toY * toY + toZ * toZ);
		if (distance <= 0.0) {
			return player.canEntityBeSeen(this);
		}

		double dot = (look.x() * toX + look.y() * toY + look.z() * toZ) / distance;
		return isLookingAt(dot, distance) && player.canEntityBeSeen(this);
	}

	public static boolean isLookingAt(double dot, double distance) {
		return dot > 1.0 - STARE_CONE / distance;
	}

	private static boolean isWearingPumpkin(Player player) {
		ItemStack helmet = player.getItemInArmorSlot(HumanArmorShape.HEAD);
		if (helmet == null) {
			return false;
		}
		return helmet.itemID == Blocks.PUMPKIN.id()
			|| helmet.itemID == Blocks.PUMPKIN_CARVED_IDLE.id()
			|| helmet.itemID == Blocks.PUMPKIN_CARVED_ACTIVE.id();
	}

	@Override
	protected boolean isValidLightLevel() {
		TilePos pos = new TilePos(this.x, this.bb.minY, this.z);

		if (this.world.getSavedLightValue(LightLayer.Sky, pos) > this.random.nextInt(32)) {
			return false;
		}

		int blockLight = this.world.getBlockLightValue(pos);
		if (this.world.getCurrentWeather() != null
			&& this.world.getCurrentWeather().isMobDaylightSpawnAllowed()) {
			blockLight /= 2;
		}
		return blockLight <= this.random.nextInt(8);
	}
}
