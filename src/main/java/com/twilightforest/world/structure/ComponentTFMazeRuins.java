package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeRuins extends StructureComponentTF {

	private static final int MAZE_DEPTH = 14;

	private static final int MOUND_OFFSET = 14;

	public ComponentTFMazeRuins(int componentType, int x, int y, int z) {
		super(componentType);
		this.coordBaseMode = 0;
		this.boundingBox = componentBox(x, y, z, 0, 0, 0, 0, 0, 0, 0);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		super.buildComponent(parent, pieces, rand);

		ComponentTFMinotaurMaze maze = new ComponentTFMinotaurMaze(1,
			this.boundingBox.minX, this.boundingBox.minY - MAZE_DEPTH, this.boundingBox.minZ, 1);
		pieces.add(maze);
		maze.buildComponent(this, pieces, rand);

		ComponentTFMazeEntranceShaft shaft = new ComponentTFMazeEntranceShaft(2, rand,
			this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1);
		pieces.add(shaft);
		shaft.buildComponent(this, pieces, rand);

		ComponentTFMazeMound mound = new ComponentTFMazeMound(2, rand,
			this.boundingBox.minX - MOUND_OFFSET, this.boundingBox.minY,
			this.boundingBox.minZ - MOUND_OFFSET);
		pieces.add(mound);
		mound.buildComponent(this, pieces, rand);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		return true;
	}
}
