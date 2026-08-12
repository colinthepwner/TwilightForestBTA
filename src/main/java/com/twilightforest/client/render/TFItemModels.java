package com.twilightforest.client.render;

import com.twilightforest.TwilightForest;
import com.twilightforest.asset.TFBlockTextureBridge;
import com.twilightforest.item.TFItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;

@Environment(EnvType.CLIENT)
public final class TFItemModels {
	private TFItemModels() {}

	public static void registerItemModels(ItemModelDispatcher dispatcher) {
		if (TFItems.NAGA_SCALE == null) {

			return;
		}

		boolean bridged = hasTexture(TFItems.NAGA_SCALE.namespaceID);

		ItemModelStandard model = new ItemModelStandard(TFItems.NAGA_SCALE, bridged);
		if (!bridged) {
			model.icon = ItemModelStandard.ITEM_TEXTURE_MISSING;
		}
		dispatcher.addDispatch(model);

		TwilightForest.LOGGER.info(
			"Registered item models for 1 Twilight Forest item; its texture {}.",
			bridged ? "was bridged out of the original's items.png"
				: "is missing, so it draws as the placeholder");
	}

	private static boolean hasTexture(net.minecraft.core.util.collection.NamespaceID id) {
		TFBlockTextureBridge.ensureScanned();
		if (TFBlockTextureBridge.writtenTextureIds.contains(id.toString())) {
			return true;
		}
		try {
			return TextureRegistry.hasTexture(id);
		} catch (RuntimeException e) {
			return false;
		}
	}
}
