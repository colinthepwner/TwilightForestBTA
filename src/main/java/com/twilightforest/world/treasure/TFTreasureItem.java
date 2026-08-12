package com.twilightforest.world.treasure;

import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.ItemStack;

import java.util.Random;

public final class TFTreasureItem {

	public static final int DEFAULT_RARITY = 10;

	private final IItemConvertible what;
	private final int quantity;
	private final int rarity;

	public TFTreasureItem(IItemConvertible what, int quantity, int rarity) {
		this.what = what;
		this.quantity = quantity;
		this.rarity = rarity;
	}

	public ItemStack getItemStack(Random rand) {
		return new ItemStack(this.what, rand.nextInt(this.quantity) + 1);
	}

	public int getRarity() {
		return this.rarity;
	}
}
