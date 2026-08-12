package com.twilightforest.client.render;

import com.twilightforest.block.BlockLogicTFGiantMushroom;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockModelTFGiantMushroom<T extends BlockLogic> extends BlockModelStandard<T> {

	@NotNull private final IconCoordinate skin;
	@NotNull private final IconCoordinate pores;

	private boolean standalone;

	public BlockModelTFGiantMushroom(@NotNull Block<T> block, @NotNull String skinTexture) {
		super(block);
		this.skin = TextureRegistry.getTexture(skinTexture);
		this.pores = TextureRegistry.getTexture("twilightforest:block/mushroom_skin_stem");

		this.setAllTextures(skinTexture);
	}

	@Nullable
	@Override
	public IconCoordinate getBlockTextureFromSideAndMetadata(@NotNull Side side, int data) {
		if (this.standalone && data == BlockLogicTFGiantMushroom.PORES) {
			return this.skin;
		}

		int skinned = BlockLogicTFGiantMushroom.skinnedFaces(data);
		IconCoordinate faceSkin = data == BlockLogicTFGiantMushroom.STEM ? this.pores : this.skin;
		return (skinned & bit(side)) != 0 ? faceSkin : this.pores;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral tessellator, int metadata,
								 byte lightIndex) {
		this.standalone = true;
		try {
			super.renderStandalone(tessellator, metadata, lightIndex);
		} finally {
			this.standalone = false;
		}
	}

	private static int bit(@NotNull Side side) {
		return switch (side) {
			case TOP -> BlockLogicTFGiantMushroom.FACE_TOP;
			case BOTTOM -> BlockLogicTFGiantMushroom.FACE_BOTTOM;
			case NORTH -> BlockLogicTFGiantMushroom.FACE_NORTH;
			case SOUTH -> BlockLogicTFGiantMushroom.FACE_SOUTH;
			case WEST -> BlockLogicTFGiantMushroom.FACE_WEST;
			case EAST -> BlockLogicTFGiantMushroom.FACE_EAST;
			default -> 0;
		};
	}
}
