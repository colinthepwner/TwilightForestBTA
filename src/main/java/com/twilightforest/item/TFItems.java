package com.twilightforest.item;

import com.twilightforest.TwilightForest;
import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemArmor;
import net.minecraft.core.item.Items;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.util.helper.DamageType;
import turniplabs.halplibe.helper.ItemBuilder;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;

public final class TFItems {
	private TFItems() {}

	public static ArmorMaterial ARMOR_NAGA_SCALE;

	private static final float PROTECTION = 59.0f;

	private static final int DURABILITY = 400;

	public static Item NAGA_SCALE;
	public static Item NAGA_SCALE_TUNIC;
	public static Item NAGA_SCALE_LEGGINGS;

	public static Item TOWER_KEY;

	public static void register() {

		ARMOR_NAGA_SCALE = ArmorMaterial.register(
			new ArmorMaterial(NamespaceID.getPermanent(TwilightForest.MOD_ID, "naga_scale"), DURABILITY)
				.withProtectionPercentage(DamageType.COMBAT, PROTECTION)
				.withProtectionPercentage(DamageType.BLAST, PROTECTION)
				.withProtectionPercentage(DamageType.FIRE, PROTECTION)
				.withProtectionPercentage(DamageType.FALL, PROTECTION));

		NAGA_SCALE = new ItemBuilder(TwilightForest.MOD_ID)
			.setCreativeInventoryPlacement(new CreativeInventoryPlacement.After(() -> Items.LEATHER))
			.build(new Item("naga_scale", "twilightforest:item/naga_scale", 2400));

		NAGA_SCALE_TUNIC = new ItemBuilder(TwilightForest.MOD_ID)
			.setCreativeInventoryPlacement(
				new CreativeInventoryPlacement.After(() -> Items.ARMOR_CHESTPLATE_DIAMOND))
			.build(new ItemArmor<>("naga_scale_tunic", "twilightforest:item/naga_scale_tunic", 2401,
				ARMOR_NAGA_SCALE, HumanArmorShape.CHEST));

		NAGA_SCALE_LEGGINGS = new ItemBuilder(TwilightForest.MOD_ID)
			.setCreativeInventoryPlacement(
				new CreativeInventoryPlacement.After(() -> Items.ARMOR_LEGGINGS_DIAMOND))
			.build(new ItemArmor<>("naga_scale_leggings", "twilightforest:item/naga_scale_leggings",
				2402, ARMOR_NAGA_SCALE, HumanArmorShape.LEGS));

		TOWER_KEY = new ItemBuilder(TwilightForest.MOD_ID)
			.setCreativeInventoryPlacement(
				new CreativeInventoryPlacement.After(() -> Items.INGOT_IRON))
			.build(new ItemTFTowerKey("tower_key", "twilightforest:item/tower_key", 2403));

		TwilightForest.LOGGER.info("Registered 4 Twilight Forest items.");
	}
}
