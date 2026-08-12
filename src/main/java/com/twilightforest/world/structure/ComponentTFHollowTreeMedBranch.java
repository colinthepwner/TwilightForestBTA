package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFHollowTreeMedBranch extends StructureComponentTF {

	protected final int[] src;
	protected final int[] dest;
	protected final double length;
	protected final double angle;
	protected final double tilt;
	protected final boolean leafy;

	protected ComponentTFHollowTreeMedBranch(int componentType, int sx, int sy, int sz,
	                                         double length, double angle, double tilt,
	                                         boolean leafy) {
		super(componentType);
		this.src = new int[]{sx, sy, sz};
		this.dest = translate(sx, sy, sz, length, angle, tilt);
		this.length = length;
		this.angle = angle;
		this.tilt = tilt;
		this.leafy = leafy;
		this.coordBaseMode = 0;

		this.boundingBox = new BoundingBox(
			Math.min(this.src[0], this.dest[0]),
			Math.min(this.src[1], this.dest[1]),
			Math.min(this.src[2], this.dest[2]),
			Math.max(this.src[0], this.dest[0]),
			Math.max(this.src[1], this.dest[1]),
			Math.max(this.src[2], this.dest[2]));
	}

	@Override
	public int featureType() {
		return ComponentTFHollowTreeTrunk.HOLLOW_TREE;
	}

	protected static int[] translate(int sx, int sy, int sz,
	                                 double distance, double angle, double tilt) {
		double rangle = angle * 2.0 * Math.PI;
		double rtilt = tilt * Math.PI;
		return new int[]{
			(int) (sx + Math.round(Math.sin(rangle) * Math.sin(rtilt) * distance)),
			(int) (sy + Math.round(Math.cos(rtilt) * distance)),
			(int) (sz + Math.round(Math.cos(rangle) * Math.sin(rtilt) * distance)),
		};
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		int index = componentType();

		if (this.leafy) {
			int numLeafBalls = Math.min(rand.nextInt(3) + 1, (int) (this.length / 5.0));
			for (int i = 0; i < numLeafBalls; i++) {
				double slength = (rand.nextDouble() * 0.6 + 0.2) * this.length;
				int[] bdst = translate(this.src[0], this.src[1], this.src[2],
					slength, this.angle, this.tilt);
				addLeafSphere(pieces, rand, index + 1, bdst, 2);
			}
			addLeafSphere(pieces, rand, index + 1, this.dest, 2);
		}

		int numShoots = Math.min(rand.nextInt(3) + 1, (int) (this.length / 5.0));
		double angleInc = 0.8 / numShoots;

		for (int i = 0; i < numShoots; i++) {
			double angleVar = angleInc * i - 0.4;
			double outVar = rand.nextDouble() * 0.8 + 0.2;
			double tiltVar = rand.nextDouble() * 0.75 + 0.15;
			int[] bsrc = translate(this.src[0], this.src[1], this.src[2],
				this.length * outVar, this.angle, this.tilt);
			makeSmallBranch(pieces, rand, index + 1, bsrc[0], bsrc[1], bsrc[2],
				this.length * 0.4, this.angle + angleVar, this.tilt * tiltVar, this.leafy);
		}
	}

	protected void addLeafSphere(List<StructureComponentTF> pieces, Random rand, int index,
	                             int[] at, int radius) {
		ComponentTFLeafSphere sphere =
			new ComponentTFLeafSphere(index, at[0], at[1], at[2], radius);
		pieces.add(sphere);
		sphere.buildComponent(this, pieces, rand);
	}

	public void makeSmallBranch(List<StructureComponentTF> pieces, Random rand, int index,
	                            int x, int y, int z, double branchLength, double branchRotation,
	                            double branchAngle, boolean leafy) {
		ComponentTFHollowTreeSmallBranch branch = new ComponentTFHollowTreeSmallBranch(
			index, x, y, z, branchLength, branchRotation, branchAngle, leafy);
		pieces.add(branch);
		branch.buildComponent(this, pieces, rand);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int[] rSrc = relative(this.src);
		int[] rDest = relative(this.dest);
		drawBresehnam(world, clip, rSrc[0], rSrc[1], rSrc[2], rDest[0], rDest[1], rDest[2],
			TFBlocks.LOG_TWILIGHT_OAK.id(), 0);
		return true;
	}

	protected int[] relative(int[] world) {
		return new int[]{
			world[0] - this.boundingBox.minX,
			world[1] - this.boundingBox.minY,
			world[2] - this.boundingBox.minZ,
		};
	}

	protected void drawBresehnam(World world, BoundingBox clip,
	                             int x1, int y1, int z1, int x2, int y2, int z2,
	                             int blockId, int meta) {
		int[] pixel = {x1, y1, z1};
		int dx = x2 - x1;
		int dy = y2 - y1;
		int dz = z2 - z1;
		int xInc = dx < 0 ? -1 : 1;
		int l = Math.abs(dx);
		int yInc = dy < 0 ? -1 : 1;
		int m = Math.abs(dy);
		int zInc = dz < 0 ? -1 : 1;
		int n = Math.abs(dz);
		int dx2 = l << 1;
		int dy2 = m << 1;
		int dz2 = n << 1;

		if (l >= m && l >= n) {
			int err1 = dy2 - l;
			int err2 = dz2 - l;
			for (int i = 0; i < l; i++) {
				placeBlock(world, blockId, meta, pixel[0], pixel[1], pixel[2], clip);
				if (err1 > 0) {
					pixel[1] += yInc;
					err1 -= dx2;
				}
				if (err2 > 0) {
					pixel[2] += zInc;
					err2 -= dx2;
				}
				err1 += dy2;
				err2 += dz2;
				pixel[0] += xInc;
			}
		} else if (m >= l && m >= n) {
			int err1 = dx2 - m;
			int err2 = dz2 - m;
			for (int i = 0; i < m; i++) {
				placeBlock(world, blockId, meta, pixel[0], pixel[1], pixel[2], clip);
				if (err1 > 0) {
					pixel[0] += xInc;
					err1 -= dy2;
				}
				if (err2 > 0) {
					pixel[2] += zInc;
					err2 -= dy2;
				}
				err1 += dx2;
				err2 += dz2;
				pixel[1] += yInc;
			}
		} else {
			int err1 = dy2 - n;
			int err2 = dx2 - n;
			for (int i = 0; i < n; i++) {
				placeBlock(world, blockId, meta, pixel[0], pixel[1], pixel[2], clip);
				if (err1 > 0) {
					pixel[1] += yInc;
					err1 -= dz2;
				}
				if (err2 > 0) {
					pixel[0] += xInc;
					err2 -= dz2;
				}
				err1 += dy2;
				err2 += dx2;
				pixel[2] += zInc;
			}
		}

		placeBlock(world, blockId, meta, pixel[0], pixel[1], pixel[2], clip);
	}
}
