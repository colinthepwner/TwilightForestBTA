package com.twilightforest.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColor;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

@Environment(EnvType.CLIENT)
public class BlockColorTFTowerWood extends BlockColor {

	@Override
	public int getWorldColor(WorldSource world, TilePosc pos, int meta) {
		return shadeAt(pos.x(), pos.y(), pos.z());
	}

	public static int shadeAt(int x, int y, int z) {
		int value = x * 31 + y * 15 + z * 33;
		if ((value & 0x100) != 0) {
			value = 255 - (value & 0xFF);
		}
		value &= 0xFF;
		value >>= 1;
		value |= 0x80;
		return value << 16 | value << 8 | value;
	}

	@Override
	public int getFallbackColor(int meta, int light) {
		return shadeAt(0, 0, 0);
	}
}
