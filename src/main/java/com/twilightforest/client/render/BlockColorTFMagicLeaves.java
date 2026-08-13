package com.twilightforest.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColor;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;

@Environment(EnvType.CLIENT)
public class BlockColorTFMagicLeaves extends BlockColor {

	public static BlockColorTFMagicLeaves timewood() {
		return new BlockColorTFMagicLeaves(16, 16, 16, 106, 156, 23, 251, 108, 27);
	}

	public static BlockColorTFMagicLeaves transformation() {
		return new BlockColorTFMagicLeaves(27, 63, 39, 108, 204, 234, 96, 107, 121);
	}

	public static BlockColorTFMagicLeaves miners() {
		return new BlockColorTFMagicLeaves(31, 33, 32, 252, 241, 68, 237, 172, 9);
	}

	public static BlockColorTFMagicLeaves sorting() {
		return new BlockColorTFMagicLeaves(63, 63, 63, 54, 76, 3, 168, 199, 43);
	}

	private final int wx;
	private final int wy;
	private final int wz;
	private final int springR;
	private final int springG;
	private final int springB;
	private final int fallR;
	private final int fallG;
	private final int fallB;

	private BlockColorTFMagicLeaves(int wx, int wy, int wz,
	                                int springR, int springG, int springB,
	                                int fallR, int fallG, int fallB) {
		this.wx = wx;
		this.wy = wy;
		this.wz = wz;
		this.springR = springR;
		this.springG = springG;
		this.springB = springB;
		this.fallR = fallR;
		this.fallG = fallG;
		this.fallB = fallB;
	}

	@Override
	public int getWorldColor(WorldSource world, TilePosc pos, int meta) {
		return this.colorAt(pos.x(), pos.y(), pos.z());
	}

	public int colorAt(int x, int y, int z) {
		int fade = x * this.wx + y * this.wy + z * this.wz;
		if ((fade & 0x100) != 0) {
			fade = 255 - (fade & 0xFF);
		}
		fade &= 0xFF;

		float spring = (255 - fade) / 255.0f;
		float fall = fade / 255.0f;

		int red = (int) (spring * this.springR + fall * this.fallR);
		int green = (int) (spring * this.springG + fall * this.fallG);
		int blue = (int) (spring * this.springB + fall * this.fallB);

		return red << 16 | green << 8 | blue;
	}

	@Override
	public int getFallbackColor(int meta, int light) {
		return this.colorAt(0, 0, 0);
	}
}
