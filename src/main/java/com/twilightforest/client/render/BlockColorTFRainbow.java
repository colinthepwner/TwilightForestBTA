package com.twilightforest.client.render;

import net.minecraft.client.render.block.color.BlockColor;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlockColorTFRainbow extends BlockColor {

	@Override
	public int getWorldColor(WorldSource world, TilePosc pos, int meta) {
		return rainbowAt(pos.x(), pos.y(), pos.z());
	}

	public static int rainbowAt(int x, int y, int z) {
		int red = x * 32 + y * 16;
		if ((red & 0x100) != 0) {
			red = 255 - (red & 0xFF);
		}
		red &= 0xFF;

		int blue = y * 32 + z * 16;
		if ((blue & 0x100) != 0) {
			blue = 255 - (blue & 0xFF);
		}
		blue ^= 0xFF;

		int green = x * 16 + z * 32;
		if ((green & 0x100) != 0) {
			green = 255 - (green & 0xFF);
		}
		green &= 0xFF;

		return red << 16 | blue << 8 | green;
	}

	@Override
	public int getFallbackColor(int meta, int light) {
		return rainbowAt(0, 0, 0);
	}
}
