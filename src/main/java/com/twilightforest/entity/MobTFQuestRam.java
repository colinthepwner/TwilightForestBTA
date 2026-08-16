package com.twilightforest.entity;

import com.mojang.nbt.tags.CompoundTag;
import com.twilightforest.TwilightForest;
import com.twilightforest.entity.ai.TFBrain;
import com.twilightforest.entity.ai.TFTaskEatLoose;
import com.twilightforest.entity.ai.TFTaskFindLoose;
import com.twilightforest.entity.ai.TFTaskPanic;
import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.animal.MobAnimal;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Color;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MobTFQuestRam extends MobAnimal implements TFTaskEatLoose.WoolEater {

	private static final int MAX_HEALTH = 7;

	private static final float WIDTH = 1.25F;
	private static final float HEIGHT = 2.9F;

	private static final float NORMAL_SPEED = 0.70F;
	private static final float PURSUE_SPEED = 0.70F;
	private static final float PANIC_SPEED = 1.06F;

	private static final float HOME_RADIUS = 13.0F;

	private static final int HOME_CHECK_MIN = 70;
	private static final int HOME_CHECK_SPREAD = 50;

	private static final float OUT_OF_BOUNDS_WEIGHT = -100.0F;

	private static final int DATA_COLOR_FLAGS = 16;
	private static final int DATA_REWARDED = 17;

	private static final int COLOR_COUNT = 16;

	private static final int COLORS_NEEDED = 15;

	private static final int ACCEPT_PARTICLES = 50;

	private static final int SHIMMER_PARTICLES = 5;

	private static final float SOUND_VOLUME = 5.0F;

	private static final float PITCH_DROP = -0.3F;

	private static final int WOOL = Blocks.WOOL.id();

	private final TFBrain brain = new TFBrain(this, NORMAL_SPEED);

	private final TFTaskPanic panic = new TFTaskPanic(this, this.brain, PANIC_SPEED);

	private int homeCheckCountdown;

	private int homeX;
	private int homeZ;
	private float homeRadius = -1.0F;

	public MobTFQuestRam(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "questram");
		this.setSize(WIDTH, HEIGHT);
		this.moveSpeed = NORMAL_SPEED;

		this.brain.add(1, this.panic);
		this.brain.add(3, new TFTaskEatLoose(this, this.brain, WOOL));
		this.brain.add(4, new TFTaskFindLoose(this, this.brain, PURSUE_SPEED, WOOL));
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected void updateAI() {
		this.checkGroveAndReward();
		this.brain.tick(super::updateAI);
	}

	private void checkGroveAndReward() {
		if (--this.homeCheckCountdown > 0) {
			return;
		}
		this.homeCheckCountdown = HOME_CHECK_MIN + this.random.nextInt(HOME_CHECK_SPREAD);

		int chunkX = MathHelper.floor(this.x) >> 4;
		int chunkZ = MathHelper.floor(this.z) >> 4;

		if (TFFeature.nearestFeatureType(this.world, chunkX, chunkZ) != TFFeature.QUEST_GROVE) {

			this.homeRadius = -1.0F;
		} else {

			int[] centre = TFFeature.nearestFeatureCenter(this.world, chunkX, chunkZ);
			this.homeX = (chunkX << 4) + centre[0];
			this.homeZ = (chunkZ << 4) + centre[1];
			this.homeRadius = HOME_RADIUS;
		}

		if (this.countColorsSet() > COLORS_NEEDED && !this.getRewarded()) {
			this.rewardQuest();
			this.setRewarded(true);
		}
	}

	@Override
	protected float getBlockPathWeight(@NotNull TilePosc pos) {
		float base = super.getBlockPathWeight(pos);
		if (this.homeRadius < 0.0F) {
			return base;
		}
		double dx = pos.x() - this.homeX;
		double dz = pos.z() - this.homeZ;
		if (dx * dx + dz * dz >= this.homeRadius * this.homeRadius) {
			return base + OUT_OF_BOUNDS_WEIGHT;
		}
		return base;
	}

	@Override
	public boolean isFavouriteItem(ItemStack stack) {
		return stack != null && stack.itemID == WOOL;
	}

	@Override
	public boolean hurt(@Nullable Entity attacker, int damage, DamageType type) {
		if (attacker != this) {
			this.panic.alarm(attacker);
		}
		return super.hurt(attacker, damage, type);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_COLOR_FLAGS, 0, Integer.class);
		this.entityData.define(DATA_REWARDED, (byte) 0, Byte.class);
	}

	@Override
	public boolean interact(Player player) {
		ItemStack held = player.inventory.getCurrentItem();
		if (held == null || held.itemID != WOOL || this.isColorPresent(held.getMetadata())) {
			return super.interact(player);
		}

		int color = held.getMetadata();
		this.setColorPresent(color);
		this.animateAddColor(color, ACCEPT_PARTICLES);

		if (player.getGamemode().hasBlockConsumption()) {
			held.stackSize--;
			if (held.stackSize <= 0) {
				player.inventory.setItem(player.inventory.getCurrentSlot(), null);
			}
		}
		return true;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.checkAndAnimateColors();
	}

	public void checkAndAnimateColors() {
		if (this.countColorsSet() > COLORS_NEEDED && !this.getRewarded()) {
			this.animateAddColor(this.random.nextInt(COLOR_COUNT), SHIMMER_PARTICLES);
		}
	}

	private void rewardQuest() {
		this.dropItem(Blocks.BLOCK_DIAMOND.id(), 1, 1.0F);
		this.dropItem(Blocks.BLOCK_IRON.id(), 1, 1.0F);
		this.dropItem(Blocks.BLOCK_OLIVINE.id(), 1, 1.0F);
		this.dropItem(Blocks.BLOCK_GOLD.id(), 1, 1.0F);
		this.dropItem(Blocks.BLOCK_LAPIS.id(), 1, 1.0F);

	}

	public int getColorFlags() {
		return this.entityData.getInt(DATA_COLOR_FLAGS);
	}

	public void setColorFlags(int flags) {
		this.entityData.set(DATA_COLOR_FLAGS, flags);
	}

	@Override
	public boolean isColorPresent(int color) {
		return (this.getColorFlags() & (1 << color)) != 0;
	}

	@Override
	public void setColorPresent(int color) {
		this.setColorFlags(this.getColorFlags() | (1 << color));
	}

	public int countColorsSet() {
		return Integer.bitCount(this.getColorFlags() & ((1 << COLOR_COUNT) - 1));
	}

	public boolean getRewarded() {
		return this.entityData.getByte(DATA_REWARDED) != 0;
	}

	public void setRewarded(boolean rewarded) {
		this.entityData.set(DATA_REWARDED, (byte) (rewarded ? 1 : 0));
	}

	@Override
	public void animateAddColor(int color, int iterations) {
		DyeColor dye = DyeColor.colorFromBlockMeta(color);
		Color rgb = dye == null ? null : dye.color;
		float red = rgb == null ? 1.0F : rgb.getRed() / 255.0F;
		float green = rgb == null ? 1.0F : rgb.getGreen() / 255.0F;
		float blue = rgb == null ? 1.0F : rgb.getBlue() / 255.0F;

		for (int i = 0; i < iterations; i++) {
			double px = this.x + (this.random.nextDouble() - 0.5) * this.bbWidth * 1.5;
			double py = this.y + this.random.nextDouble() * this.bbHeight * 1.5;
			double pz = this.z + (this.random.nextDouble() - 0.5) * this.bbWidth * 1.5;
			this.world.spawnParticle("puffrgb", px, py, pz, red, green, blue, 0, false);
		}
		this.bleat();
	}

	@Override
	public void bleat() {
		this.playLivingSound();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("ColorFlags", this.getColorFlags());
		tag.putBoolean("Rewarded", this.getRewarded());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setColorFlags(tag.getInteger("ColorFlags"));
		this.setRewarded(tag.getBoolean("Rewarded"));
	}

	@Override
	public String getLivingSound() {
		return "mob.sheep";
	}

	@Override
	protected String getHurtSound() {
		return "mob.sheep";
	}

	@Override
	protected String getDeathSound() {
		return "mob.sheep";
	}

	@Override
	protected float getSoundVolume() {
		return SOUND_VOLUME;
	}

	@Override
	protected float getPitchModifier() {
		return PITCH_DROP;
	}

	@Override
	public MobPathfinder asMob() {
		return this;
	}

	@Override
	public float tfBlockPathWeight(TilePosc pos) {
		return this.getBlockPathWeight(pos);
	}

	@Override
	public void tfSetSpeed(float speed) {
		this.moveSpeed = speed;
	}

	@Override
	public void tfSetRandomWalk(boolean enabled) {
		this.doRandomWalk = enabled;
	}

	@Override
	public void tfDrive(float yRot, float moveForward, boolean jumping) {
		this.yRot = yRot;
		this.moveForward = moveForward;
		this.isJumping = jumping;
	}
}
