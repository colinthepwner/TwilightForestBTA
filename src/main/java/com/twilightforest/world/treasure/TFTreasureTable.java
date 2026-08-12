package com.twilightforest.world.treasure;

import com.twilightforest.TwilightForest;
import net.minecraft.core.item.IItemConvertible;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TFTreasureTable {

	static final class Tier {
		private final List<TFTreasureItem> entries = new ArrayList<>();

		void add(IItemConvertible what, int quantity) {
			if (what == null) {
				TwilightForest.LOGGER.error("A treasure table entry was null -- a block or item was "
					+ "referenced before it was registered. That entry has been dropped.");
				return;
			}
			this.entries.add(new TFTreasureItem(what, quantity, TFTreasureItem.DEFAULT_RARITY));
		}

		boolean isEmpty() {
			return this.entries.isEmpty();
		}

		private int total() {
			int value = 0;
			for (TFTreasureItem entry : this.entries) {
				value += entry.getRarity();
			}
			return value;
		}

		@Nullable
		ItemStack getRandomItem(Random rand) {
			if (this.entries.isEmpty()) {
				return null;
			}
			int value = rand.nextInt(this.total());
			for (TFTreasureItem entry : this.entries) {
				if (entry.getRarity() > value) {
					return entry.getItemStack(rand);
				}
				value -= entry.getRarity();
			}
			return null;
		}
	}

	private static final int SUBSTITUTION_CHANCE = 4;

	final Tier useless = new Tier();
	final Tier common = new Tier();
	final Tier uncommon = new Tier();
	final Tier rare = new Tier();
	final Tier ultrarare = new Tier();

	private final String name;

	TFTreasureTable(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return "TFTreasureTable[" + this.name + "]";
	}

	@Nullable
	ItemStack getCommonItem(Random rand) {
		return rand.nextInt(SUBSTITUTION_CHANCE) == 0
			? this.useless.getRandomItem(rand)
			: this.common.getRandomItem(rand);
	}

	@Nullable
	ItemStack getUncommonItem(Random rand) {
		return this.uncommon.getRandomItem(rand);
	}

	@Nullable
	ItemStack getRareItem(Random rand) {
		return rand.nextInt(SUBSTITUTION_CHANCE) == 0
			? this.ultrarare.getRandomItem(rand)
			: this.rare.getRandomItem(rand);
	}

	boolean isComplete() {
		return !this.useless.isEmpty() && !this.common.isEmpty() && !this.uncommon.isEmpty()
			&& !this.rare.isEmpty() && !this.ultrarare.isEmpty();
	}
}
