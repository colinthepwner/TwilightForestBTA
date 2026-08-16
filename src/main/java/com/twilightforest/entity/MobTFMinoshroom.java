package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import com.twilightforest.entity.ai.TFTaskChargeAttack;
import net.minecraft.core.WeightedRandomLootObject;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;

import java.util.List;

public class MobTFMinoshroom extends MobTFMinotaur {

	private static final int MAX_HEALTH = 120;

	private static final int SCORE_VALUE = 100;

	private static final float WIDTH = 1.49F;
	private static final float HEIGHT = 2.9F;

	private static final int DROP_MIN = 2;
	private static final int DROP_MAX = 5;

	public MobTFMinoshroom(World world) {
		super(world);

		this.setTextureIdentifier(TwilightForest.MOD_ID, "minoshroomtaur");
		this.setSize(WIDTH, HEIGHT);
		this.scoreValue = SCORE_VALUE;

		this.mobDrops.clear();
		this.mobDrops.add(
			new WeightedRandomLootObject(Items.FOOD_STEW_MUSHROOM.getDefaultStack(), DROP_MIN, DROP_MAX));
	}

	@Override
	public int getMaxHealth() {
		return MAX_HEALTH;
	}

	@Override
	protected List<WeightedRandomLootObject> getMobDrops() {
		return this.mobDrops;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}
}
