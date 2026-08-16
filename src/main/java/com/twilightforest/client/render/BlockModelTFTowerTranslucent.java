package com.twilightforest.client.render;

import com.twilightforest.block.BlockLogicTFTowerTranslucent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelTransparent;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Side;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BlockModelTFTowerTranslucent<T extends BlockLogic> extends BlockModelTransparent<T> {

	private static final String[] TEXTURES = {
		"twilightforest:block/tower_translucent_reappearing",
		"twilightforest:block/tower_translucent_reappearing_active",
		"twilightforest:block/tower_translucent_built",
		"twilightforest:block/tower_translucent_built_active",
		"twilightforest:block/tower_translucent_antibuilt",
	};

	public static final String[] BRIDGED = TEXTURES.clone();

	private final IconCoordinate[] icons = new IconCoordinate[BlockLogicTFTowerTranslucent.META_COUNT];

	public BlockModelTFTowerTranslucent(@NotNull Block<T> block, @NotNull String fallback) {
		super(block, true);
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
