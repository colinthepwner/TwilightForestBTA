package com.twilightforest.client.render;

import com.twilightforest.block.BlockLogicTFCritter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BlockModelTFCritter<T extends BlockLogic> extends BlockModelStandard<T> {

	private static final double OFFSET = 0.01;

	public BlockModelTFCritter(Block<T> block) {
		super(block);
	}

	@Override
	public boolean shouldItemRender3d() {
		return false;
	}

	@Override
	public boolean render(@NotNull TessellatorGeneral tessellator, @NotNull WorldSource worldSource,
	                      @NotNull TilePosc tilePos) {
		int orientation = worldSource.getBlockData(tilePos) & BlockLogicTFCritter.MASK_DIRECTION;

		IconCoordinate icon = this.getBlockTextureFromSideAndMetadata(Side.NORTH, orientation);
		if (renderBlocks.overrideBlockTexture != null) {
			icon = renderBlocks.overrideBlockTexture;
		}

		tessellator.setLightmapCoord1i(this.block.getLightIndex(worldSource, tilePos));
		tessellator.setColorOpaque3f(1.0F, 1.0F, 1.0F);

		double uMin = icon.getIconUMin();
		double uMax = icon.getIconUMax();
		double vMin = icon.getIconVMin();
		double vMax = icon.getIconVMax();

		double x = tilePos.x();
		double y = tilePos.y();
		double z = tilePos.z();

		switch (orientation) {
			case BlockLogicTFCritter.SIDE_WEST -> {
				double px = x + OFFSET;
				tessellator.addVertexWithUV(px, y + 1.0, z + 1.0, uMin, vMin);
				tessellator.addVertexWithUV(px, y, z + 1.0, uMin, vMax);
				tessellator.addVertexWithUV(px, y, z, uMax, vMax);
				tessellator.addVertexWithUV(px, y + 1.0, z, uMax, vMin);
			}
			case BlockLogicTFCritter.SIDE_EAST -> {
				double px = x + 1.0 - OFFSET;
				tessellator.addVertexWithUV(px, y, z + 1.0, uMax, vMax);
				tessellator.addVertexWithUV(px, y + 1.0, z + 1.0, uMax, vMin);
				tessellator.addVertexWithUV(px, y + 1.0, z, uMin, vMin);
				tessellator.addVertexWithUV(px, y, z, uMin, vMax);
			}
			case BlockLogicTFCritter.SIDE_NORTH -> {
				double pz = z + OFFSET;
				tessellator.addVertexWithUV(x + 1.0, y, pz, uMax, vMax);
				tessellator.addVertexWithUV(x + 1.0, y + 1.0, pz, uMax, vMin);
				tessellator.addVertexWithUV(x, y + 1.0, pz, uMin, vMin);
				tessellator.addVertexWithUV(x, y, pz, uMin, vMax);
			}
			case BlockLogicTFCritter.SIDE_SOUTH -> {
				double pz = z + 1.0 - OFFSET;
				tessellator.addVertexWithUV(x + 1.0, y + 1.0, pz, uMin, vMin);
				tessellator.addVertexWithUV(x + 1.0, y, pz, uMin, vMax);
				tessellator.addVertexWithUV(x, y, pz, uMax, vMax);
				tessellator.addVertexWithUV(x, y + 1.0, pz, uMax, vMin);
			}

			case BlockLogicTFCritter.SIDE_BOTTOM -> {
				double py = y + OFFSET;
				tessellator.addVertexWithUV(x, py, z + 1.0, uMin, vMin);
				tessellator.addVertexWithUV(x, py, z, uMin, vMax);
				tessellator.addVertexWithUV(x + 1.0, py, z, uMax, vMax);
				tessellator.addVertexWithUV(x + 1.0, py, z + 1.0, uMax, vMin);
			}
			case BlockLogicTFCritter.SIDE_CEILING -> {
				double py = y + 1.0 - OFFSET;
				tessellator.addVertexWithUV(x + 1.0, py, z + 1.0, uMax, vMin);
				tessellator.addVertexWithUV(x + 1.0, py, z, uMax, vMax);
				tessellator.addVertexWithUV(x, py, z, uMin, vMax);
				tessellator.addVertexWithUV(x, py, z + 1.0, uMin, vMin);
			}
			default -> {

				return false;
			}
		}
		return true;
	}

	@Override
	public void renderStandalone(@NotNull TessellatorGeneral tessellator, int metadata, byte lightIndex) {
		IconCoordinate icon = this.getBlockTextureFromSideAndMetadata(Side.NORTH, metadata);
		if (renderBlocks.overrideBlockTexture != null) {
			icon = renderBlocks.overrideBlockTexture;
		}
		double uMin = icon.getIconUMin();
		double uMax = icon.getIconUMax();
		double vMin = icon.getIconVMin();
		double vMax = icon.getIconVMax();

		tessellator.offsetTranslation(-0.5, -0.5, -0.5);
		tessellator.startDrawingQuads();
		tessellator.setColor1i(this.getStandaloneTintColor(metadata));
		tessellator.setLightmapCoord1i(lightIndex);

		tessellator.addVertexWithUV(0.0, 1.0, 0.5, uMin, vMin);
		tessellator.addVertexWithUV(0.0, 0.0, 0.5, uMin, vMax);
		tessellator.addVertexWithUV(1.0, 0.0, 0.5, uMax, vMax);
		tessellator.addVertexWithUV(1.0, 1.0, 0.5, uMax, vMin);

		tessellator.addVertexWithUV(1.0, 1.0, 0.5, uMin, vMin);
		tessellator.addVertexWithUV(1.0, 0.0, 0.5, uMin, vMax);
		tessellator.addVertexWithUV(0.0, 0.0, 0.5, uMax, vMax);
		tessellator.addVertexWithUV(0.0, 1.0, 0.5, uMax, vMin);

		tessellator.draw();
		tessellator.offsetTranslation(0.5, 0.5, 0.5);
	}
}
