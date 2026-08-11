package com.twilightforest.item;

import com.twilightforest.TwilightForest;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.helper.ItemBuilder;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;

public final class TFItems {
	private TFItems() {}

	public static Item NAGA_SCALE;

	public static void register() {
		NAGA_SCALE = new ItemBuilder(TwilightForest.MOD_ID)
			.setCreativeInventoryPlacement(new CreativeInventoryPlacement.After(() -> Items.LEATHER))
			.build(new Item("naga_scale", "twilightforest:item/naga_scale", 2400));

		TwilightForest.LOGGER.info("Registered 1 Twilight Forest item.");
	}
}
