package com.twilightforest.world.chunk;

import com.twilightforest.world.layer.TFBiomeIds;

public final class TFBiomeHeights {
	private TFBiomeHeights() {}

	private static final float DEFAULT_MIN = 0.1f;
	private static final float DEFAULT_MAX = 0.3f;

	public static float minHeight(int layerId) {
		switch (layerId) {
			case TFBiomeIds.LAKE: return -1.9f;
			case TFBiomeIds.STREAM: return -0.75f;
			case TFBiomeIds.SWAMP: return -0.25f;
			case TFBiomeIds.CLEARING: return 0.01f;
			case TFBiomeIds.TWILIGHT_FOREST_VARIANT:
			case TFBiomeIds.DEEP_MUSHROOMS: return 0.15f;
			case TFBiomeIds.HIGHLANDS: return 1.0f;

			case TFBiomeIds.DARK_FOREST: return 0.05f;
			default: return DEFAULT_MIN;
		}
	}

	public static float maxHeight(int layerId) {
		switch (layerId) {
			case TFBiomeIds.STREAM: return -0.1f;
			case TFBiomeIds.SWAMP: return 0.0f;
			case TFBiomeIds.CLEARING: return 0.0f;
			case TFBiomeIds.LAKE: return 0.5f;
			case TFBiomeIds.TWILIGHT_FOREST_VARIANT:
			case TFBiomeIds.DEEP_MUSHROOMS: return 0.4f;
			case TFBiomeIds.HIGHLANDS: return 2.0f;
			case TFBiomeIds.DARK_FOREST: return 0.05f;
			default: return DEFAULT_MAX;
		}
	}
}
