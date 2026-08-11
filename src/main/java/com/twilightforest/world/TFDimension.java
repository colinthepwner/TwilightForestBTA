package com.twilightforest.world;

import com.twilightforest.TFConfig;
import com.twilightforest.TwilightForest;
import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicPortal;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.type.WorldTypeGroups;

import java.lang.reflect.Field;

public final class TFDimension {
	private TFDimension() {}

	@SuppressWarnings({"java:S1104", "java:S1444"})
	public static Dimension TWILIGHT_FOREST;

	private static int claimedId = -1;

	public static void create() {
		TWILIGHT_FOREST = new Dimension(
			"twilightforest",
			Dimension.OVERWORLD,
			1.0F,
			null,
			WorldTypeTwilightForest.TWILIGHT_FOREST);
	}

	public static void attachPortalBlock(Block<? extends BlockLogicPortal> portal) {
		try {
			Field field = Dimension.class.getField("portalBlock");
			field.setAccessible(true);
			field.set(TWILIGHT_FOREST, portal);
		} catch (ReflectiveOperationException | RuntimeException e) {
			TwilightForest.LOGGER.error(
				"Could not attach the Twilight Forest's portal block; travel to and from the "
					+ "dimension will fail inside PortalHandler.", e);
		}
	}

	public static void register() {
		int id = TFConfig.resolveDimensionId();
		if (id < 0) {
			return;
		}

		if (Dimension.getDimensionList().containsKey(id)) {
			TwilightForest.LOGGER.error(
				"Dimension id {} was taken between resolution and registration; the Twilight Forest "
					+ "will not be reachable.", id);
			return;
		}

		Dimension.registerDimension(id, TWILIGHT_FOREST);
		claimedId = id;
		TwilightForest.LOGGER.info("Registered the Twilight Forest as dimension {}.", id);
	}

	public static int getDimensionId() {
		return claimedId;
	}

	public static boolean isRegistered() {
		return claimedId >= 0;
	}

	public static void registerWorldTypeGroups() {
		if (!isRegistered()) {
			return;
		}

		WorldType twilight = WorldTypeTwilightForest.TWILIGHT_FOREST;
		for (WorldTypeGroups.Group group : WorldTypeGroups.GROUPS) {
			group.with(TWILIGHT_FOREST, twilight);
		}

		TwilightForest.LOGGER.info("Added the Twilight Forest to {} world-type groups.",
			WorldTypeGroups.GROUPS.size());
	}
}
