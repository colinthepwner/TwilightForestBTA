package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFHollowTree extends TFWorldFeature {

	private Random treeRNG;
	private int x;
	private int y;
	private int z;
	private int height;
	private int diameter;
	private int treeBlock;
	private int leafBlock;

	@Override
	public boolean generate(World world, Random random, int treeX, int treeY, int treeZ) {
		this.worldObj = world;
		this.treeRNG = random;
		this.x = treeX;
		this.y = treeY;
		this.z = treeZ;

		this.treeBlock = TFBlocks.LOG_TWILIGHT_OAK.id();
		this.leafBlock = TFBlocks.LEAVES_TWILIGHT_OAK.id();

		this.height = this.treeRNG.nextInt(64) + 32;
		this.diameter = this.treeRNG.nextInt(4) + 1;

		int maxY = world.getWorldType().getMaxY(world);
		if (this.y < 1 || this.y + this.height + this.diameter + 1 > maxY + 1) {
			return false;
		}

		for (int dx = -this.diameter; dx <= this.diameter; dx++) {
			for (int dz = -this.diameter; dz <= this.diameter; dz++) {
				for (int dy = 0; dy <= this.y + this.height; dy++) {
					int here = getBlockId(this.worldObj, dx + this.x, dy + this.y, dz + this.z);
					if (here != 0 && here != this.leafBlock) {
						return false;
					}
				}
			}
		}

		int crownClearance = this.diameter * 4 + 8;
		for (int dx = -crownClearance; dx <= crownClearance; dx++) {
			for (int dz = -crownClearance; dz <= crownClearance; dz++) {
				for (int dy = this.height - crownClearance; dy <= this.height + crownClearance; dy++) {
					int here = getBlockId(this.worldObj, dx + this.x, dy + this.y, dz + this.z);
					if (here != 0 && here != this.leafBlock) {
						return false;
					}
				}
			}
		}

		int below = getBlockId(world, this.x, this.y - 1, this.z);
		if ((below != Blocks.GRASS.id() && below != Blocks.DIRT.id())
			|| this.y >= maxY + 1 - this.height - 1) {
			return false;
		}

		this.buildTrunk();

		int numFireflies = this.treeRNG.nextInt(3 * this.diameter) + 5;
		for (int i = 0; i <= numFireflies; i++) {
			int fHeight = (int) (this.height * this.treeRNG.nextDouble() * 0.9) + this.height / 10;
			double fAngle = this.treeRNG.nextDouble();
			this.addFirefly(fHeight, fAngle);
		}

		int numCicadas = this.treeRNG.nextInt(3 * this.diameter) + 5;
		for (int i = 0; i <= numCicadas; i++) {
			int fHeight = (int) (this.height * this.treeRNG.nextDouble() * 0.9) + this.height / 10;
			double fAngle = this.treeRNG.nextDouble();
			this.addCicada(fHeight, fAngle);
		}

		this.buildFullCrown();

		int numBranches = this.treeRNG.nextInt(3) + 3;
		for (int i = 0; i <= numBranches; i++) {
			int branchHeight = (int) (this.height * this.treeRNG.nextDouble() * 0.9) + this.height / 10;
			double branchRotation = this.treeRNG.nextDouble();
			this.makeSmallBranch(branchHeight, 4.0, branchRotation, 0.35, true);
		}

		this.buildBranchRing(3, 2, 6, 0, 0.75, 0.0, 3, 5, 3, false);

		return true;
	}

	private void buildFullCrown() {
		int crownRadius = this.diameter * 4 + 4;
		int bvar = this.diameter + 2;
		this.buildBranchRing(this.height - crownRadius, 0, crownRadius, 0, 0.35, 0.0, bvar, bvar + 2, 2, true);
		this.buildBranchRing(this.height - crownRadius / 2, 0, crownRadius, 0, 0.28, 0.0, bvar, bvar + 2, 1, true);
		this.buildBranchRing(this.height, 0, crownRadius, 0, 0.15, 0.0, 2, 4, 2, true);
		this.buildBranchRing(this.height, 0, crownRadius / 2, 0, 0.05, 0.0, bvar, bvar + 2, 1, true);
	}

	@SuppressWarnings("unused")
	private void buildWeakCrown() {
		int crownRadius = 8;
		int bvar = 2;
		this.buildBranchRing(this.height - crownRadius, 0, crownRadius, 0, 0.35, 0.0, bvar, bvar + 2, 1, true);
		this.buildBranchRing(this.height - crownRadius / 2, 0, crownRadius, 0, 0.28, 0.0, bvar, bvar + 2, 1, true);
		this.buildBranchRing(this.height, 0, crownRadius, 0, 0.15, 0.0, 2, 4, 1, true);
		this.buildBranchRing(this.height, 0, crownRadius / 2, 0, 0.05, 0.0, bvar, bvar + 2, 1, true);
	}

	private void buildBranchRing(int branchHeight, int heightVar, int length, int lengthVar,
	                             double tilt, double tiltVar, int minBranches, int maxBranches,
	                             int size, boolean leafy) {
		int numBranches = this.treeRNG.nextInt(maxBranches - minBranches) + minBranches;
		double branchRotation = 1.0 / numBranches;
		double branchOffset = this.treeRNG.nextDouble();

		for (int i = 0; i <= numBranches; i++) {
			int dHeight = heightVar > 0
				? branchHeight - heightVar + this.treeRNG.nextInt(2 * heightVar)
				: branchHeight;

			double angle = i * branchRotation + branchOffset;

			switch (size) {
				case 2 -> this.makeLargeBranch(dHeight, length, angle, tilt, leafy);
				case 1 -> this.makeMedBranch(dHeight, length, angle, tilt, leafy);
				case 3 -> this.makeRoot(dHeight, length, angle, tilt);
				default -> this.makeSmallBranch(dHeight, length, angle, tilt, leafy);
			}
		}
	}

	private void buildTrunk() {
		int hollow = this.diameter / 2;
		int ladder = Blocks.LADDER_OAK.id();

		for (int dx = -this.diameter; dx <= this.diameter; dx++) {
			for (int dz = -this.diameter; dz <= this.diameter; dz++) {
				for (int dy = 0; dy <= this.height; dy++) {
					int ax = Math.abs(dx);
					int az = Math.abs(dz);
					int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);

					if (dist <= this.diameter && dist > hollow) {
						this.putBlock(dx + this.x, dy + this.y, dz + this.z, this.treeBlock, true);
					}

					if (dist == hollow && dx == hollow) {
						this.putBlockAndMetadata(dx + this.x, dy + this.y, dz + this.z, ladder, 4, true);
					}
				}
			}
		}

		for (int dx = -this.diameter; dx <= this.diameter; dx++) {
			for (int dz = -this.diameter; dz <= this.diameter; dz++) {
				for (int dy = -4; dy < 0; dy++) {
					int ax = Math.abs(dx);
					int az = Math.abs(dz);
					int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);
					if (dist <= this.diameter && dist > hollow) {
						this.putBlock(dx + this.x, dy + this.y, dz + this.z, this.treeBlock, false);
					}
				}
			}
		}
	}

	private void makeMedBranch(int branchHeight, double length, double angle, double tilt, boolean leafy) {
		int[] src = this.translate(this.x, this.y + branchHeight, this.z, this.diameter, angle, 0.5);
		this.makeMedBranch(src[0], src[1], src[2], length, angle, tilt, leafy);
	}

	private void makeMedBranch(int sx, int sy, int sz, double length, double angle, double tilt, boolean leafy) {
		int[] src = new int[]{sx, sy, sz};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);
		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, true);

		if (leafy) {
			this.drawBlob(dest[0], dest[1], dest[2], 2, this.leafBlock, false);
		}

		int numShoots = this.treeRNG.nextInt(2) + 1;
		double angleInc = 0.8 / numShoots;

		for (int i = 0; i <= numShoots; i++) {
			double angleVar = angleInc * i - 0.4;
			double outVar = this.treeRNG.nextDouble() * 0.8 + 0.2;
			double tiltVar = this.treeRNG.nextDouble() * 0.75 + 0.15;
			int[] bsrc = this.translate(src[0], src[1], src[2], length * outVar, angle, tilt);
			this.makeSmallBranch(bsrc[0], bsrc[1], bsrc[2], length * 0.4, angle + angleVar, tilt * tiltVar, leafy);
		}
	}

	private void makeSmallBranch(int branchHeight, double length, double angle, double tilt, boolean leafy) {
		int[] src = this.translate(this.x, this.y + branchHeight, this.z, this.diameter, angle, 0.5);
		this.makeSmallBranch(src[0], src[1], src[2], length, angle, tilt, leafy);
	}

	private void makeSmallBranch(int sx, int sy, int sz, double length, double angle, double tilt, boolean leafy) {
		int[] src = new int[]{sx, sy, sz};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);
		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, true);

		if (leafy) {
			int leafRad = this.treeRNG.nextInt(2) + 1;
			this.drawBlob(dest[0], dest[1], dest[2], leafRad, this.leafBlock, false);
		}
	}

	private void makeRoot(int branchHeight, double length, double angle, double tilt) {
		int[] src = this.translate(this.x, this.y + branchHeight, this.z, this.diameter, angle, 0.5);
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);
		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, true);
		this.drawBresehnam(src[0], src[1] - 1, src[2], dest[0], dest[1] - 1, dest[2], this.treeBlock, true);
	}

	private void makeLargeBranch(int branchHeight, double length, double angle, double tilt, boolean leafy) {
		int[] src = this.translate(this.x, this.y + branchHeight, this.z, this.diameter, angle, 0.5);
		this.makeLargeBranch(src[0], src[1], src[2], length, angle, tilt, leafy);
	}

	private void makeLargeBranch(int sx, int sy, int sz, double length, double angle, double tilt, boolean leafy) {
		int[] src = new int[]{sx, sy, sz};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);
		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2], this.treeBlock, true);

		int reinforcements = this.treeRNG.nextInt(3);
		for (int i = 0; i <= reinforcements; i++) {
			int vx = (i & 2) == 0 ? 1 : 0;
			int vy = (i & 1) == 0 ? 1 : -1;
			int vz = (i & 2) == 0 ? 0 : 1;
			this.drawBresehnam(src[0] + vx, src[1] + vy, src[2] + vz,
				dest[0], dest[1], dest[2], this.treeBlock, true);
		}

		if (leafy) {
			this.drawBlob(dest[0], dest[1] + 1, dest[2], 3, this.leafBlock, false);
		}

		int numMedBranches = this.treeRNG.nextInt((int) (length / 6.0)) + this.treeRNG.nextInt(2) + 1;
		for (int i = 0; i <= numMedBranches; i++) {
			double outVar = this.treeRNG.nextDouble() * 0.3 + 0.3;
			double angleVar = this.treeRNG.nextDouble() * 0.225 * ((i & 1) == 0 ? 1.0 : -1.0);
			int[] bsrc = this.translate(src[0], src[1], src[2], length * outVar, angle, tilt);
			this.makeMedBranch(bsrc[0], bsrc[1], bsrc[2], length * 0.6, angle + angleVar, tilt, leafy);
		}

		int numSmallBranches = this.treeRNG.nextInt(2) + 1;
		for (int i = 0; i <= numSmallBranches; i++) {
			double outVar = this.treeRNG.nextDouble() * 0.25 + 0.25;
			double angleVar = this.treeRNG.nextDouble() * 0.25 * ((i & 1) == 0 ? 1.0 : -1.0);
			int[] bsrc = this.translate(src[0], src[1], src[2], length * outVar, angle, tilt);
			this.makeSmallBranch(bsrc[0], bsrc[1], bsrc[2], Math.max(length * 0.3, 2.0),
				angle + angleVar, tilt, leafy);
		}
	}

	private void addFirefly(int fHeight, double fAngle) {
		this.putCritter(fHeight, fAngle, TFBlocks.FIREFLY.id());
	}

	private void addCicada(int fHeight, double fAngle) {
		this.putCritter(fHeight, fAngle, TFBlocks.CICADA.id());
	}

	private void putCritter(int fHeight, double fAngle, int blockId) {
		int[] src = this.translate(this.x, this.y + fHeight, this.z, this.diameter + 1, fAngle, 0.5);
		fAngle %= 1.0;

		if (fAngle > 0.875 || fAngle <= 0.125) {
			if (this.worldObj.isBlockNormalCube(src[0] + 1, src[1], src[2])) {
				this.putBlockAndMetadata(src[0], src[1], src[2], blockId, 2, false);
			}
		} else if (fAngle > 0.125 || fAngle <= 0.375) {
			if (this.worldObj.isBlockNormalCube(src[0], src[1], src[2] - 1)) {
				this.putBlockAndMetadata(src[0], src[1], src[2], blockId, 3, false);
			}
		} else if (fAngle > 0.375 || fAngle <= 0.625) {
			if (this.worldObj.isBlockNormalCube(src[0] - 1, src[1], src[2])) {
				this.putBlockAndMetadata(src[0], src[1], src[2], blockId, 1, false);
			}
		} else if ((fAngle > 0.625 || fAngle <= 0.875)
			&& this.worldObj.isBlockNormalCube(src[0], src[1], src[2] - 1)) {
			this.putBlockAndMetadata(src[0], src[1], src[2], blockId, 4, false);
		}
	}

	private void drawBlob(int sx, int sy, int sz, int rad, int blockValue, boolean priority) {
		for (int dx = 0; dx <= rad; dx++) {
			for (int dy = 0; dy <= rad; dy++) {
				for (int dz = 0; dz <= rad; dz++) {
					int dist;
					if (dx >= dy && dx >= dz) {
						dist = dx + (int) (Math.max(dy, dz) * 0.5 + Math.min(dy, dz) * 0.25);
					} else if (dy >= dx && dy >= dz) {
						dist = dy + (int) (Math.max(dx, dz) * 0.5 + Math.min(dx, dz) * 0.25);
					} else {
						dist = dz + (int) (Math.max(dx, dy) * 0.5 + Math.min(dx, dy) * 0.25);
					}

					if (dist > rad) {
						continue;
					}

					this.putBlock(sx + dx, sy + dy, sz + dz, blockValue, priority);
					this.putBlock(sx + dx, sy + dy, sz - dz, blockValue, priority);
					this.putBlock(sx - dx, sy + dy, sz + dz, blockValue, priority);
					this.putBlock(sx - dx, sy + dy, sz - dz, blockValue, priority);
					this.putBlock(sx + dx, sy - dy, sz + dz, blockValue, priority);
					this.putBlock(sx + dx, sy - dy, sz - dz, blockValue, priority);
					this.putBlock(sx - dx, sy - dy, sz + dz, blockValue, priority);
					this.putBlock(sx - dx, sy - dy, sz - dz, blockValue, priority);
				}
			}
		}
	}
}
