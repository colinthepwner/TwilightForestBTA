package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFHollowTreeLargeBranch extends ComponentTFHollowTreeMedBranch {

	protected ComponentTFHollowTreeLargeBranch(int componentType, int sx, int sy, int sz,
	                                           double length, double angle, double tilt,
	                                           boolean leafy) {
		super(componentType, sx, sy, sz, length, angle, tilt, leafy);
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		int index = componentType();

		if (this.leafy) {
			addLeafSphere(pieces, rand, index + 1, this.dest, 3);
		}

		int numMedBranches = rand.nextInt((int) (this.length / 6.0)) + rand.nextInt(2) + 1;
		for (int i = 0; i <= numMedBranches; i++) {
			double outVar = rand.nextDouble() * 0.3 + 0.3;
			double angleVar = rand.nextDouble() * 0.225 * ((i & 1) == 0 ? 1.0 : -1.0);
			int[] bsrc = translate(this.src[0], this.src[1], this.src[2],
				this.length * outVar, this.angle, this.tilt);
			makeMedBranch(pieces, rand, index + 2 + i, bsrc[0], bsrc[1], bsrc[2],
				this.length * 0.6, this.angle + angleVar, this.tilt, this.leafy);
		}

		int numSmallBranches = rand.nextInt(2) + 1;
		for (int i = 0; i <= numSmallBranches; i++) {
			double outVar = rand.nextDouble() * 0.25 + 0.25;
			double angleVar = rand.nextDouble() * 0.25 * ((i & 1) == 0 ? 1.0 : -1.0);
			int[] bsrc = translate(this.src[0], this.src[1], this.src[2],
				this.length * outVar, this.angle, this.tilt);

			makeSmallBranch(pieces, rand, index + numMedBranches + 1 + i,
				bsrc[0], bsrc[1], bsrc[2], Math.max(this.length * 0.3, 2.0),
				this.angle + angleVar, this.tilt, this.leafy);
		}
	}

	public void makeMedBranch(List<StructureComponentTF> pieces, Random rand, int index,
	                          int x, int y, int z, double branchLength, double branchRotation,
	                          double branchAngle, boolean leafy) {
		ComponentTFHollowTreeMedBranch branch = new ComponentTFHollowTreeMedBranch(
			index, x, y, z, branchLength, branchRotation, branchAngle, leafy);
		pieces.add(branch);
		branch.buildComponent(this, pieces, rand);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int log = TFBlocks.LOG_TWILIGHT_OAK.id();
		int[] rSrc = relative(this.src);
		int[] rDest = relative(this.dest);

		drawBresehnam(world, clip, rSrc[0], rSrc[1], rSrc[2], rDest[0], rDest[1], rDest[2], log, 0);

		int reinforcements = rand.nextInt(3);
		for (int i = 0; i <= reinforcements; i++) {
			int vx = (i & 2) == 0 ? 1 : 0;
			int vy = (i & 1) == 0 ? 1 : -1;
			int vz = (i & 2) == 0 ? 0 : 1;
			drawBresehnam(world, clip, rSrc[0] + vx, rSrc[1] + vy, rSrc[2] + vz,
				rDest[0], rDest[1], rDest[2], log, 0);
		}
		return true;
	}
}
