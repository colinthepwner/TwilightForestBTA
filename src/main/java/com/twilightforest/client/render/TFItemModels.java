package com.twilightforest.client.render;

import com.twilightforest.TwilightForest;
import com.twilightforest.asset.TFBlockTextureBridge;
import com.twilightforest.item.TFItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.item.Item;

@Environment(EnvType.CLIENT)
public final class TFItemModels {
	private TFItemModels() {}

	public static void registerItemModels(ItemModelDispatcher dispatcher) {
		int registered = 0;
		int bridged = 0;

		for (Item item : new Item[]{
			TFItems.NAGA_SCALE,
			TFItems.NAGA_SCALE_TUNIC,
			TFItems.NAGA_SCALE_LEGGINGS,
		}) {
			if (item == null) continue;
			registered++;
			if (register(dispatcher, item)) bridged++;
		}

		TwilightForest.LOGGER.info(
			"Registered item models for {} Twilight Forest item(s); {} of them had a texture bridged "
				+ "out of the original's items.png, the rest draw as the placeholder.",
			registered, bridged);
	}

	private static boolean register(ItemModelDispatcher dispatcher, Item item) {

		boolean bridged = hasTexture(item.namespaceID);

		ItemModelStandard model = new ItemModelStandard(item, bridged);
		if (!bridged) {
			model.icon = ItemModelStandard.ITEM_TEXTURE_MISSING;
		}
		dispatcher.addDispatch(model);
		return bridged;
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
