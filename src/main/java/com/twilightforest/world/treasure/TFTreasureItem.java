package com.twilightforest.world.treasure;

import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.ItemStack;

import java.util.Random;

public final class TFTreasureItem {

	public static final int DEFAULT_RARITY = 10;

	public static final int DEFAULT_METADATA = 0;

	private final IItemConvertible what;
	private final int quantity;
	private final int metadata;
	private final int rarity;

	public TFTreasureItem(IItemConvertible what, int quantity, int rarity) {
		this(what, quantity, DEFAULT_METADATA, rarity);
	}

	public TFTreasureItem(IItemConvertible what, int quantity, int metadata, int rarity) {
		this.what = what;
		this.quantity = quantity;
		this.metadata = metadata;
		this.rarity = rarity;
	}

	public ItemStack getItemStack(Random rand) {
		return new ItemStack(this.what, rand.nextInt(this.quantity) + 1, this.metadata);
	}

	public int getRarity() {
		return this.rarity;
	}
}
