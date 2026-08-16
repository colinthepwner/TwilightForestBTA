package com.twilightforest.world.treasure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

class TFTreasureTableTest {

	@BeforeAll
	static void bootstrap() {
		Blocks.init();
		Items.init();
	}

	private static TFTreasureTable noSubstitutionTiers() {
		TFTreasureTable table = new TFTreasureTable("test_no_junk");
		table.common.add(Items.INGOT_IRON, 4);
		table.uncommon.add(Items.INGOT_STEEL, 4);
		table.rare.add(Items.DIAMOND, 1);
		return table;
	}

	private static TFTreasureTable everyTier() {
		TFTreasureTable table = noSubstitutionTiers();
		table.useless.add(Blocks.SAND, 4);
		table.ultrarare.add(Items.RECORD_CAT, 1);
		return table;
	}

	@Test
	void aTableWithNoJunkTierStillFillsEverySlot() {
		TFTreasureTable table = noSubstitutionTiers();
		Random rand = new Random(1234L);

		for (int i = 0; i < 4000; i++) {
			assertNotNull(table.getCommonItem(rand), "common roll " + i + " came back empty");
		}
	}

	@Test
	void aTableWithNoUltrarareTierStillFillsEverySlot() {
		TFTreasureTable table = noSubstitutionTiers();
		Random rand = new Random(5678L);

		for (int i = 0; i < 4000; i++) {
			assertNotNull(table.getRareItem(rand), "rare roll " + i + " came back empty");
		}
	}

	@Test
	void theJunkSubstitutionStillHappensWhereThereIsJunk() {
		TFTreasureTable table = everyTier();
		Random rand = new Random(99L);

		int junk = 0;
		for (int i = 0; i < 4000; i++) {
			ItemStack drawn = table.getCommonItem(rand);
			assertNotNull(drawn);
			if (drawn.itemID == Blocks.SAND.asItem().id) {
				junk++;
			}
		}

		assertTrue(junk > 800 && junk < 1200,
			"expected roughly a quarter junk, got " + junk + " of 4000");
	}

	@Test
	void theUltrarareSubstitutionStillHappensWhereThereIsOne() {
		TFTreasureTable table = everyTier();
		Random rand = new Random(4242L);

		int jackpots = 0;
		for (int i = 0; i < 4000; i++) {
			ItemStack drawn = table.getRareItem(rand);
			assertNotNull(drawn);
			if (drawn.itemID == Items.RECORD_CAT.id) {
				jackpots++;
			}
		}

		assertTrue(jackpots > 800 && jackpots < 1200,
			"expected roughly a quarter ultrarare, got " + jackpots + " of 4000");
	}

	@Test
	void completenessIgnoresTheTwoOptionalTiers() {
		assertTrue(noSubstitutionTiers().isComplete());
		assertTrue(everyTier().isComplete());
	}

	@Test
	void completenessStillCatchesAGapInATierThatIsAlwaysDrawn() {
		TFTreasureTable missingRare = new TFTreasureTable("test_broken");
		missingRare.common.add(Items.INGOT_IRON, 4);
		missingRare.uncommon.add(Items.INGOT_STEEL, 4);

		assertFalse(missingRare.isComplete(), "a chest always draws one rare; an empty rare tier is a bug");
	}

	@Test
	void anEntryKeepsItsSubtype() {
		TFTreasureTable table = new TFTreasureTable("test_metadata");
		table.common.addWithMetadata(Items.COAL, 12, 1);
		Random rand = new Random(31337L);

		for (int i = 0; i < 500; i++) {
			ItemStack drawn = table.getCommonItem(rand);
			assertNotNull(drawn);
			assertEquals(Items.COAL.id, drawn.itemID);
			assertEquals(1, drawn.getMetadata(), "charcoal became coal on roll " + i);
		}
	}

	@Test
	void anEntryWithNoSubtypeIsStillTheFirstOne() {
		TFTreasureTable table = new TFTreasureTable("test_no_metadata");
		table.common.add(Items.COAL, 12);

		assertEquals(0, table.getCommonItem(new Random(1L)).getMetadata());
	}

	@Test
	void aReweightedEntryIsRarerThanADefaultOne() {
		TFTreasureTable table = new TFTreasureTable("test_weights");
		table.rare.add(Items.DIAMOND, 1);
		table.rare.add(Items.FOOD_APPLE_GOLD, 1, 4);
		Random rand = new Random(864L);

		int reweighted = 0;
		for (int i = 0; i < 4200; i++) {
			if (table.getRareItem(rand).itemID == Items.FOOD_APPLE_GOLD.id) {
				reweighted++;
			}
		}

		assertTrue(reweighted > 1000 && reweighted < 1400,
			"expected roughly 4/14 of the draws, got " + reweighted + " of 4200");
	}

	@Test
	void aQuantityIsAMaximumAndNotAFixedStack() {
		TFTreasureTable table = new TFTreasureTable("test_stacks");
		table.common.add(Items.AMMO_ARROW, 12);
		Random rand = new Random(7L);

		int min = Integer.MAX_VALUE;
		int max = 0;
		for (int i = 0; i < 2000; i++) {
			int size = table.getCommonItem(rand).stackSize;
			min = Math.min(min, size);
			max = Math.max(max, size);
		}

		assertEquals(1, min);
		assertEquals(12, max);
	}
}
