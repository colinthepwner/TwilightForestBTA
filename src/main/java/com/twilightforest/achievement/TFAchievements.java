package com.twilightforest.achievement;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.item.TFItems;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.collection.NamespaceID;

public final class TFAchievements {
	private TFAchievements() {}

	@SuppressWarnings({"java:S1104", "java:S1444", "java:S3008"})
	public static Achievement PORTAL;
	public static Achievement ARRIVAL;
	public static Achievement HUNTER;
	public static Achievement NAGA;
	public static Achievement LICH;
	public static Achievement HILL_1;
	public static Achievement HILL_3;
	public static Achievement HEDGE;

	private static boolean registered;

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		PORTAL = make("twilight_portal", TFBlocks.PORTAL_TWILIGHT != null
			? new ItemStack(TFBlocks.PORTAL_TWILIGHT) : new ItemStack(Items.DIAMOND), null)
			.setType(Achievement.TYPE_SPECIAL);
		ARRIVAL = make("twilight_arrival", new ItemStack(TFBlocks.LOG_TWILIGHT_OAK), PORTAL);
		HUNTER = make("twilight_hunter", new ItemStack(Items.FEATHER_CHICKEN), ARRIVAL);
		NAGA = make("twilight_naga", new ItemStack(TFItems.NAGA_SCALE), HUNTER);

		LICH = make("twilight_lich", new ItemStack(Items.BONE), HUNTER);

		HILL_1 = make("twilight_hill_1", new ItemStack(Blocks.ORE_IRON_STONE), ARRIVAL);
		HILL_3 = make("twilight_hill_3", new ItemStack(Blocks.ORE_DIAMOND_STONE), ARRIVAL);

		HEDGE = make("twilight_hedge", new ItemStack(TFBlocks.HEDGE), ARRIVAL);

		TwilightForest.LOGGER.info("Registered 8 Twilight Forest achievements.");
	}

	public static void award(Entity killer, Achievement achievement) {
		if (achievement != null && killer instanceof Player player) {
			player.triggerAchievement(achievement);
		}
	}

	private static Achievement make(String name, ItemStack icon, Achievement parent) {
		return new Achievement(NamespaceID.getPermanent(TwilightForest.MOD_ID, name),
			"achievement." + name, icon, parent).registerAchievement();
	}
}
