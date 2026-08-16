package com.twilightforest.client.render;

import com.twilightforest.block.BlockLogicTFTowerDevice;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Side;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BlockModelTFTowerDevice<T extends BlockLogic> extends BlockModelStandard<T> {

	private static final String[] TEXTURES = {
		"twilightforest:block/tower_device_reappearing",
		"twilightforest:block/tower_device_reappearing_active",
		"twilightforest:block/tower_device_vanish",
		"twilightforest:block/tower_device_vanish_active",
		"twilightforest:block/tower_device_vanish_locked",
		"twilightforest:block/tower_device_vanish_unlocked",
		"twilightforest:block/tower_device_builder",
		"twilightforest:block/tower_device_builder_active",
		"twilightforest:block/tower_device_builder_timeout",
		"twilightforest:block/tower_device_antibuilder",
		"twilightforest:block/tower_device_ghasttrap",
		"twilightforest:block/tower_device_ghasttrap_active",
		"twilightforest:block/tower_device_reactor",
		"twilightforest:block/tower_device_reactor_active",
	};

	public static final String[] BRIDGED = TEXTURES.clone();

	private final IconCoordinate[] icons = new IconCoordinate[BlockLogicTFTowerDevice.META_COUNT];

	public BlockModelTFTowerDevice(@NotNull Block<T> block, @NotNull String fallback) {
		super(block);
		for (int meta = 0; meta < this.icons.length; meta++) {
			String id = TFBlockModels.hasTexture(TEXTURES[meta]) ? TEXTURES[meta] : fallback;
			this.icons[meta] = TextureRegistry.getTexture(id);
		}

		this.setAllTextures(TFBlockModels.hasTexture(TEXTURES[0]) ? TEXTURES[0] : fallback);
	}

	@NotNull
	@Override
	public IconCoordinate getBlockTextureFromSideAndMetadata(@NotNull Side side, int data) {
		return this.icons[data >= 0 && data < this.icons.length ? data : 0];
	}
}
