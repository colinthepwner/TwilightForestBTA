package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFHollowTreeTrunk extends StructureComponentTF {

	public static final int HOLLOW_TREE = -2;

	private final int radius;
	private final int height;

	public ComponentTFHollowTreeTrunk(int componentType, Random rand, int x, int y, int z) {
		super(componentType);
		this.height = rand.nextInt(64) + 32;
		this.radius = rand.nextInt(4) + 1;
		this.coordBaseMode = 0;

		this.boundingBox = new BoundingBox(x, y, z,
			x + this.radius * 2, y + this.height, z + this.radius * 2);
	}

	@Override
	public int featureType() {
		return HOLLOW_TREE;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		int index = componentType();

		ComponentTFLeafSphere crown = new ComponentTFLeafSphere(index + 1,
			this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ, 3);
		pieces.add(crown);
		crown.buildComponent(this, pieces, rand);

		int numBranches = rand.nextInt(3) + 3;
		for (int i = 0; i <= numBranches; i++) {
			int branchHeight = (int) (this.height * rand.nextDouble() * 0.9) + this.height / 10;
			double branchRotation = rand.nextDouble();
			makeMedBranch(pieces, rand, index + i + 1, branchHeight, 4, branchRotation, 0.35, true);
		}

		buildFullCrown(pieces, rand, index + numBranches + 1);
	}

	protected void buildFullCrown(List<StructureComponentTF> pieces, Random rand, int index) {
		int crownRadius = this.radius * 4 + 4;
		int bvar = this.radius + 2;

		index += buildBranchRing(pieces, rand, index, this.height - crownRadius, 0,
			crownRadius, 0, 0.35, 0.0, bvar, bvar + 2, 2, true);
		index += buildBranchRing(pieces, rand, index, this.height - crownRadius / 2, 0,
			crownRadius, 0, 0.28, 0.0, bvar, bvar + 2, 1, true);
		index += buildBranchRing(pieces, rand, index, this.height, 0,
			crownRadius, 0, 0.15, 0.0, 2, 5, 2, true);
		buildBranchRing(pieces, rand, index, this.height, 0,
			crownRadius / 2, 0, 0.05, 0.0, bvar, bvar + 2, 1, true);
	}

	protected int buildBranchRing(List<StructureComponentTF> pieces, Random rand, int index,
	                              int branchHeight, int heightVar, int length, int lengthVar,
	                              double tilt, double tiltVar,
	                              int minBranches, int maxBranches, int size, boolean leafy) {
		int numBranches = rand.nextInt(maxBranches - minBranches + 1) + minBranches;
		double branchRotation = 1.0 / numBranches;
		double branchOffset = rand.nextDouble();

		for (int i = 0; i <= numBranches; i++) {
			int dHeight = heightVar > 0
				? branchHeight - heightVar + rand.nextInt(2 * heightVar)
				: branchHeight;

			double rotation = i * branchRotation + branchOffset;
			if (size == 2) {
				makeLargeBranch(pieces, rand, index, dHeight, length, rotation, tilt, leafy);
			} else if (size == 1) {
				makeMedBranch(pieces, rand, index, dHeight, length, rotation, tilt, leafy);
			} else if (size != 3) {
				makeSmallBranch(pieces, rand, index, dHeight, length, rotation, tilt, leafy);
			}
		}

		return numBranches;
	}

	private int[] branchSource(int branchHeight, double branchRotation) {
		return ComponentTFHollowTreeMedBranch.translate(
			this.boundingBox.minX + this.radius,
			this.boundingBox.minY + branchHeight,
			this.boundingBox.minZ + this.radius,
			this.radius, branchRotation, 0.5);
	}

	public void makeSmallBranch(List<StructureComponentTF> pieces, Random rand, int index,
	                            int branchHeight, int branchLength, double branchRotation,
	                            double branchAngle, boolean leafy) {
		int[] src = branchSource(branchHeight, branchRotation);
		ComponentTFHollowTreeSmallBranch branch = new ComponentTFHollowTreeSmallBranch(
			index, src[0], src[1], src[2], branchLength, branchRotation, branchAngle, leafy);
		pieces.add(branch);
		branch.buildComponent(this, pieces, rand);
	}

	public void makeMedBranch(List<StructureComponentTF> pieces, Random rand, int index,
	                          int branchHeight, int branchLength, double branchRotation,
	                          double branchAngle, boolean leafy) {
		int[] src = branchSource(branchHeight, branchRotation);
		ComponentTFHollowTreeMedBranch branch = new ComponentTFHollowTreeMedBranch(
			index, src[0], src[1], src[2], branchLength, branchRotation, branchAngle, leafy);
		pieces.add(branch);
		branch.buildComponent(this, pieces, rand);
	}

	public void makeLargeBranch(List<StructureComponentTF> pieces, Random rand, int index,
	                            int branchHeight, int branchLength, double branchRotation,
	                            double branchAngle, boolean leafy) {
		int[] src = branchSource(branchHeight, branchRotation);
		ComponentTFHollowTreeLargeBranch branch = new ComponentTFHollowTreeLargeBranch(
			index, src[0], src[1], src[2], branchLength, branchRotation, branchAngle, leafy);
		pieces.add(branch);
		branch.buildComponent(this, pieces, rand);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int log = TFBlocks.LOG_TWILIGHT_OAK.id();
		int ladder = Blocks.LADDER_OAK.id();
		int hollow = this.radius / 2;

		for (int dx = 0; dx <= 2 * this.radius; dx++) {
			for (int dz = 0; dz <= 2 * this.radius; dz++) {
				int ax = Math.abs(dx - this.radius);
				int az = Math.abs(dz - this.radius);
				int dist = (int) (Math.max(ax, az) + Math.min(ax, az) * 0.5);

				for (int dy = 0; dy <= this.height; dy++) {
					if (dist <= this.radius && dist > hollow) {
						placeBlock(world, log, 0, dx, dy, dz, clip);
					}
					if (dist == hollow && dx == hollow + this.radius) {
						placeBlock(world, ladder, 4, dx, dy, dz, clip);
					}
				}
			}
		}
		return true;
	}
}
