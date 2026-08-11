package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityChest;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFHollowHill extends TFWorldFeature {

	private final int hsize;
	private final int radius;

	private final int sn;

	private final int mg;

	private final int tc;

	private int hx;
	private int hy;
	private int hz;
	private Random hillRNG;

	public WorldFeatureTFHollowHill(int size) {
		this.hsize = size;
		this.radius = (this.hsize * 2 + 1) * 8 - 6;

		int area = (int) (Math.PI * this.radius * this.radius);
		this.sn = area / 16;

		int[] mga = new int[]{0, 3, 9, 18};
		this.mg = mga[this.hsize];

		int[] tca = new int[]{0, 2, 6, 12};
		this.tc = tca[this.hsize];
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;
		this.hx = x;
		this.hy = y;
		this.hz = z;
		this.hillRNG = rand;

		for (int i = 0; i < this.mg; i++) {
			int[] dest = this.getCoordsInHill2D();
			this.placeMobSpawner(dest[0], this.hy + rand.nextInt(4), dest[1]);
		}

		for (int i = 0; i < this.tc; i++) {
			int[] dest = this.getCoordsInHill2D();
			this.placeTreasureChest(dest[0], this.hy, dest[1]);
		}

		for (int i = 0; i < this.sn; i++) {
			int[] dest = this.getCoordsInHill2D();
			WorldFeatureTFCaveStalactite stalag =
				WorldFeatureTFCaveStalactite.makeRandomOreStalactite(rand, this.hsize);
			stalag.place(this.worldObj, rand, dest[0], this.hy + 1, dest[1]);
		}

		for (int i = 0; i < this.sn; i++) {
			int[] dest = this.getCoordsInHill2D();
			new WorldFeatureTFCaveStalactite(Blocks.STONE.id(), rand.nextDouble(), true)
				.place(this.worldObj, rand, dest[0], this.hy + 1, dest[1]);
		}

		for (int i = 0; i < this.sn; i++) {
			int[] dest = this.getCoordsInHill2D();
			new WorldFeatureTFCaveStalactite(Blocks.STONE.id(), rand.nextDouble() * 0.7, false)
				.place(this.worldObj, rand, dest[0], this.hy + 1, dest[1]);
		}

		return true;
	}

	boolean isInHill(int cx, int cz) {
		int dx = this.hx - cx;
		int dz = this.hz - cz;
		int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
		return dist < this.radius;
	}

	int[] getCoordsInHill2D() {
		int rx;
		int rz;
		do {
			rx = this.hx + this.hillRNG.nextInt(2 * this.radius) - this.radius;
			rz = this.hz + this.hillRNG.nextInt(2 * this.radius) - this.radius;
		} while (!this.isInHill(rx, rz));

		return new int[]{rx, rz};
	}

	protected boolean placeMobSpawner(int dx, int dy, int dz) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Blocks.MOBSPAWNER.id());
		if (this.worldObj.getTileEntity(dx, dy, dz) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId(this.getMobID(this.hsize));
		}
		return true;
	}

	protected String getMobID(int level) {
		if (level == 1) {
			return "Spider";
		}
		if (level == 2) {
			return this.hillRNG.nextInt(4) != 0 ? "Zombie" : this.getMobID(1);
		}
		if (level == 3) {
			return this.hillRNG.nextInt(4) != 0 ? "Skeleton" : this.getMobID(2);
		}
		return "Spider";
	}

	protected boolean placeTreasureChest(int dx, int dy, int dz) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Blocks.CHEST_PLANKS_OAK.id());
		if (this.worldObj.getTileEntity(dx, dy, dz) instanceof TileEntityChest chest
			&& chest.getContainerSize() > 0) {
			int ni = this.hillRNG.nextInt(4) + this.hillRNG.nextInt(4) + 2;
			for (int i = 0; i < ni && i < chest.getContainerSize(); i++) {
				chest.setItem(i, this.getTreasure(this.hsize));
			}
		}
		return true;
	}

	protected ItemStack getTreasure(int level) {
		if (level == 1) {
			return switch (this.hillRNG.nextInt(6)) {
				case 0 -> new ItemStack(Items.INGOT_IRON, this.hillRNG.nextInt(4) + 1);
				case 1 -> new ItemStack(Items.BUCKET_IRON);
				case 2 -> new ItemStack(Items.FOOD_BREAD);
				case 4 -> new ItemStack(Items.WHEAT, this.hillRNG.nextInt(3) + 1);
				default -> new ItemStack(Blocks.TORCH_COAL, this.hillRNG.nextInt(16) + 1);
			};
		}

		if (level == 2) {
			return switch (this.hillRNG.nextInt(8)) {
				case 0, 1, 2 -> this.getTreasure(1);
				case 4 -> new ItemStack(Items.INGOT_GOLD, this.hillRNG.nextInt(6) + 1);
				case 5 -> new ItemStack(Items.SADDLE);
				case 6 -> new ItemStack(Items.DYE, this.hillRNG.nextInt(10) + 1, this.hillRNG.nextInt(16));
				default -> new ItemStack(Items.FOOD_STEW_MUSHROOM);
			};
		}

		if (level == 3) {
			return switch (this.hillRNG.nextInt(8)) {
				case 0, 1, 2 -> this.getTreasure(2);
				case 4 -> new ItemStack(Items.FOOD_APPLE_GOLD);
				case 5 -> new ItemStack(Items.RECORD_13);
				case 6 -> new ItemStack(Items.SADDLE);
				default -> new ItemStack(Items.DIAMOND);
			};
		}

		return new ItemStack(Items.COAL, this.hillRNG.nextInt(16) + 1);
	}
}
