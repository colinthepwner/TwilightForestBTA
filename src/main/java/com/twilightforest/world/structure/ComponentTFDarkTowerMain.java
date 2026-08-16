package com.twilightforest.world.structure;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.BlockLogicTFTowerDevice;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.block.entity.TileEntityTFBossSpawner;
import com.twilightforest.entity.MobTFTowerBoss;
import com.twilightforest.world.chunk.TFWorldConstants;
import com.twilightforest.world.feature.TFMaze;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.BlockLogicLever;
import net.minecraft.core.block.BlockLogicStairs;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeFancy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerMain extends ComponentTFDarkTowerWing {

	private static final int TOWER_SIZE = 19;

	private static final int MIN_HEIGHT = 56;

	private static final int HEIGHT_VARIANCE = 24;

	private static final String BLAZE_STANDIN = TwilightForest.MOD_ID + ":firebeetle";

	private static final String MINI_GHAST = TwilightForest.MOD_ID + ":minighast";

	private boolean placedKeys = false;

	public ComponentTFDarkTowerMain(int componentType, Random rand, int x, int y, int z) {
		this(componentType, rand, x + 19, y, z + 19, 2);
	}

	public ComponentTFDarkTowerMain(World world, Random rand, int componentType, int x, int y, int z,
	                                int rotation) {
		this(componentType, rand, x, y, z, rotation);
	}

	public ComponentTFDarkTowerMain(int componentType, Random rand, int x, int y, int z,
	                                int rotation) {
		super(componentType, x, y, z, TOWER_SIZE,
			MIN_HEIGHT + rand.nextInt(HEIGHT_VARIANCE) / 5 * 5, rotation);

		if (this.deco == null) {
			this.deco = new StructureDecoratorDarkTower();
		}
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		if (parent != null) {
			this.deco = parent.deco;
		}

		if (componentType() > 0) {
			addOpening(0, 1, this.size / 2, 2);
		}

		int mainDir = -1;

		if (componentType() < 2) {

			mainDir = rand.nextInt(4);
			for (int i = 0; i < 4; i++) {
				if (i == mainDir) {
					continue;
				}
				int[] dest = getValidOpening(rand, i);
				int childHeight = validateChildHeight(21 + rand.nextInt(10), 11);
				makeTowerWing(pieces, rand, componentType(),
					dest[0], dest[1], dest[2], 11, childHeight, i);
			}
		} else {

			for (int i = 0; i < 4; i++) {
				int[] dest = getValidOpening(rand, i);
				makeBossTrapWing(pieces, rand, componentType(), dest[0], dest[1], dest[2], i);
			}
		}

		if (componentType() > 0) {

			for (int i = 0; i < 4; i++) {
				if (i == 2) {
					continue;
				}
				int[] dest = getValidOpening(rand, i);
				dest[1] = 1;
				int childHeight = validateChildHeight(21 + rand.nextInt(10), 11);
				makeTowerWing(pieces, rand, componentType(),
					dest[0], dest[1], dest[2], 11, childHeight, i);
			}
			makeABeard(parent, pieces, rand);
		} else {

			for (int i = 0; i < 4; i += 2) {
				int[] dest = getValidOpening(rand, i);
				dest[1] = 1;
				int childHeight = validateChildHeight(10 + rand.nextInt(5), 9);
				makeEntranceTower(pieces, rand, 5, dest[0], dest[1], dest[2], 9, childHeight, i);
			}
		}

		if (mainDir > -1) {
			int[] dest = getValidOpening(rand, mainDir);
			makeNewLargeTower(pieces, rand, componentType() + 1, dest[0], dest[1], dest[2], mainDir);
		}

		makeARoof(parent, pieces, rand);

		if (!this.placedKeys && componentType() < 2) {
			List<ComponentTFDarkTowerWing> possibleKeyTowers = new ArrayList<>();
			for (StructureComponentTF piece : pieces) {
				if (!(piece instanceof ComponentTFDarkTowerWing wing)) {
					continue;
				}
				if (wing.size != 9 || wing.componentType() != componentType()) {
					continue;
				}
				possibleKeyTowers.add(wing);
			}

			for (int i = 0; i < 4; i++) {
				if (possibleKeyTowers.isEmpty()) {
					TwilightForest.LOGGER.warn(
						"Dark tower could not find four small towers to place keys in.");
					break;
				}
				int towerNum = rand.nextInt(possibleKeyTowers.size());
				possibleKeyTowers.get(towerNum).setKeyTower(true);
				possibleKeyTowers.remove(towerNum);
			}

			this.placedKeys = true;
		}
	}

	private boolean makeEntranceTower(List<StructureComponentTF> pieces, Random rand, int index,
	                                  int x, int y, int z, int childSize, int childHeight,
	                                  int rotation) {
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 5, direction);

		ComponentTFDarkTowerEntranceBridge bridge = new ComponentTFDarkTowerEntranceBridge(
			index, dx[0], dx[1], dx[2], childSize, childHeight, direction);

		pieces.add(bridge);
		bridge.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	private boolean makeNewLargeTower(List<StructureComponentTF> pieces, Random rand, int index,
	                                  int x, int y, int z, int rotation) {
		int wingSize = 15;
		int wingHeight = 56;
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 5, direction);

		ComponentTFDarkTowerMainBridge bridge = new ComponentTFDarkTowerMainBridge(
			index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);

		pieces.add(bridge);
		bridge.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation, EnumDarkTowerDoor.LOCKED);
		return true;
	}

	private boolean makeBossTrapWing(List<StructureComponentTF> pieces, Random rand, int index,
	                                 int x, int y, int z, int rotation) {
		int wingSize = 11;
		int wingHeight = 9;
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 5, direction);

		ComponentTFDarkTowerBossBridge bridge = new ComponentTFDarkTowerBossBridge(
			index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);

		pieces.add(bridge);
		bridge.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	@Override
	public void makeARoof(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                      Random rand) {
		if (componentType() < 2) {
			super.makeARoof(parent, pieces, rand);
		}
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		Random decoRNG = new Random(world.getRandomSeed()
			+ (long) (this.boundingBox.minX * 321534781) ^ (long) (this.boundingBox.minZ * 756839));

		makeEncasedWalls(world, rand, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1);
		fillWithBlocks(world, clip, 1, 1, 1,
			this.size - 2, this.height - 2, this.size - 2, 0, 0, false);

		if (componentType() == 0) {
			for (int x = 0; x < this.size; x++) {
				for (int z = 0; z < this.size; z++) {
					fillBlocksDownwards(world, this.deco.accentID, this.deco.accentMeta, x, -1, z, clip);
				}
			}
		}

		nullifySkyLightForBoundingBox(world);

		int totalFloors = this.height / 5;
		boolean beamMaze = decoRNG.nextBoolean();
		int centerFloors = beamMaze ? 4 : totalFloors / 2;
		int bottomFloors = (totalFloors - centerFloors) / 2;
		centerFloors = totalFloors - bottomFloors * 2;
		int topFloorsStartY = this.height - (bottomFloors * 5 + 1);

		addThreeQuarterFloors(world, decoRNG, clip, 0, bottomFloors * 5);

		if (componentType() < 2) {
			addThreeQuarterFloors(world, decoRNG, clip, topFloorsStartY, this.height - 1);
		} else {
			addThreeQuarterFloorsDecorateBoss(world, decoRNG, clip, topFloorsStartY, this.height - 1);

			destroyTower(world, decoRNG, 12, this.height + 4, 3, 4, clip);
			destroyTower(world, decoRNG, 3, this.height + 4, 12, 4, clip);
			destroyTower(world, decoRNG, 3, this.height + 4, 3, 4, clip);
			destroyTower(world, decoRNG, 12, this.height + 4, 12, 4, clip);
			destroyTower(world, decoRNG, 8, this.height + 4, 8, 5, clip);

			placeBossSpawner(world, clip);
		}

		if (beamMaze) {
			addTimberMaze(world, decoRNG, clip, bottomFloors * 5, topFloorsStartY);
		} else {
			addBuilderPlatforms(world, decoRNG, clip, bottomFloors * 5, topFloorsStartY);
		}

		makeOpenings(world, clip);
		return true;
	}

	private void placeBossSpawner(World world, BoundingBox clip) {
		int x = this.size / 2;
		int z = this.size / 2;

		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(this.height);
		int wz = getZWithOffset(x, z);

		if (!clip.contains(wx, wy, wz)) {
			return;
		}
		if (world.getBlockId(wx, wy, wz) == TFBlocks.BOSS_SPAWNER.id()) {
			return;
		}

		world.setBlockWithNotify(wx, wy, wz, TFBlocks.BOSS_SPAWNER.id());

		if (world.getTileEntity(wx, wy, wz) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId(MobTFTowerBoss.ENTITY_ID);
		}
	}

	protected void addThreeQuarterFloors(World world, Random decoRNG, BoundingBox clip, int bottom,
	                                     int top) {
		int spacing = 5;
		int rotation = (this.boundingBox.minY + bottom) % 4;

		if (bottom == 0) {
			makeLargeStairsUp(world, clip, rotation, 0);
			rotation += 3;
			rotation %= 4;
			makeBottomEntrance(world, decoRNG, clip, rotation, bottom);
			bottom += spacing;
		}

		for (int y = bottom; y < top; y += spacing) {
			boolean isBottomFloor = y == bottom && bottom != spacing;
			boolean isTopFloor = y >= top - spacing;
			boolean isTowerTopFloor = y >= this.height - spacing - 2;

			makeThreeQuarterFloor(world, clip, rotation, y, isBottomFloor, isTowerTopFloor);

			if (!isTopFloor) {
				makeLargeStairsUp(world, clip, rotation, y);
			}

			if (!isTopFloor || isTowerTopFloor) {
				decorateFloor(world, decoRNG, clip, rotation, y, isBottomFloor, isTopFloor);
			}

			rotation += 3;
			rotation %= 4;
		}
	}

	protected void addThreeQuarterFloorsDecorateBoss(World world, Random decoRNG, BoundingBox clip,
	                                                 int bottom, int top) {
		int spacing = 5;
		int rotation = (this.boundingBox.minY + bottom) % 4;

		if (bottom == 0) {
			makeLargeStairsUp(world, clip, rotation, 0);
			rotation += 3;
			rotation %= 4;
			bottom += spacing;
		}

		for (int y = bottom; y < top; y += spacing) {
			boolean isBottomFloor = y == bottom && bottom != spacing;
			boolean isTopFloor = y >= top - spacing;
			boolean isTowerTopFloor = y >= this.height - spacing - 2;

			makeThreeQuarterFloor(world, clip, rotation, y, isBottomFloor, isTowerTopFloor);

			if (!isTopFloor) {
				makeLargeStairsUp(world, clip, rotation, y);
				decorateExperiment(world, decoRNG, clip, rotation, y);
			}

			rotation += 3;
			rotation %= 4;
		}
	}

	private void decorateFloor(World world, Random decoRNG, BoundingBox clip, int rotation, int y,
	                           boolean isBottom, boolean isTop) {
		if (isTop) {
			switch (decoRNG.nextInt(3)) {
				case 1:
					decorateBotanical(world, decoRNG, clip, rotation, y);
					break;
				case 2:
					decorateNetherwart(world, decoRNG, clip, rotation, y, isTop);
					break;
				default:
					decorateAquarium(world, decoRNG, clip, rotation, y);
					break;
			}
		} else if (isBottom) {
			switch (decoRNG.nextInt(4)) {
				case 1:
					decorateBotanical(world, decoRNG, clip, rotation, y);
					break;
				case 2:
					if (y + this.boundingBox.minY > TFWorldConstants.SEA_LEVEL) {
						decorateNetherwart(world, decoRNG, clip, rotation, y, isTop);
						break;
					}

				case 3:
					decorateForge(world, decoRNG, clip, rotation, y);
					break;
				default:
					decorateAquarium(world, decoRNG, clip, rotation, y);
					break;
			}
		} else {
			switch (decoRNG.nextInt(8)) {
				case 2:
					decorateUnbuilderMaze(world, decoRNG, clip, rotation, y);
					break;
				case 3:
					decorateAquarium(world, decoRNG, clip, rotation, y);
					break;
				case 4:
					decorateBotanical(world, decoRNG, clip, rotation, y);
					break;
				case 5:
					if (y + this.boundingBox.minY > TFWorldConstants.SEA_LEVEL) {
						decorateNetherwart(world, decoRNG, clip, rotation, y, isTop);
						break;
					}

				case 6:
					decorateLounge(world, decoRNG, clip, rotation, y);
					break;
				case 7:
					decorateForge(world, decoRNG, clip, rotation, y);
					break;
				default:
					decorateReappearingMaze(world, decoRNG, clip, rotation, y);
					break;
			}
		}
	}

	protected void makeThreeQuarterFloor(World world, BoundingBox clip, int rotation, int y,
	                                     boolean isBottom, boolean isTowerTopFloor) {
		int half = this.size / 2;

		fillBlocksRotated(world, clip, half + 1, y, 1, this.size - 2, y, half + 1,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 1, y, half + 1, this.size - 2, y, this.size - 2,
			this.deco.blockID, this.deco.blockMeta, rotation);

		int startZ = isBottom ? 1 : 3;

		fillBlocksRotated(world, clip, 1, y, half, half, y, half,
			this.deco.accentID, this.deco.accentMeta, rotation);
		fillBlocksRotated(world, clip, half, y, startZ, half, y, half,
			this.deco.accentID, this.deco.accentMeta, rotation);
		fillBlocksRotated(world, clip, 1, y + 1, half, half, y + 1, half,
			this.deco.fenceID, this.deco.fenceMeta, rotation);
		fillBlocksRotated(world, clip, half, y + 1, startZ, half, y + 1, half,
			this.deco.fenceID, this.deco.fenceMeta, rotation);

		if (isTowerTopFloor) {
			fillBlocksRotated(world, clip, 1, y, half - 2, 3, y, half,
				this.deco.accentID, this.deco.accentMeta, rotation);
			fillBlocksRotated(world, clip, 1, y + 1, half - 2, 3, y + 1, half,
				this.deco.fenceID, this.deco.fenceMeta, rotation);
			fillBlocksRotated(world, clip, 1, y, half - 1, 2, y, half,
				this.deco.blockID, this.deco.blockMeta, rotation);
			fillBlocksRotated(world, clip, 1, y + 1, half - 1, 2, y + 1, half, 0, 0, rotation);
		}
	}

	protected void makeLargeStairsUp(World world, BoundingBox clip, int rotation, int y) {
		for (int i = 0; i < 5; i++) {
			int z = this.size / 2 - i + 4;
			int sy = y + i + 1;

			placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation), 1, sy, z, rotation, clip);
			placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation), 2, sy, z, rotation, clip);
			placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, 1, sy, z - 1, rotation, clip);
			placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, 2, sy, z - 1, rotation, clip);
			placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, 3, sy, z - 1, rotation, clip);

			if (i > 0 && i < 4) {
				placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 3, sy, z, rotation, clip);
				placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 3, sy + 1, z, rotation, clip);
				placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 3, sy + 2, z, rotation, clip);
			} else if (i == 0) {
				placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation), 3, sy, z, rotation, clip);
			}
		}
	}

	private void decorateReappearingMaze(World world, Random decoRNG, BoundingBox clip, int rotation,
	                                     int y) {
		int mazeSize = 6;
		TFMaze maze = new TFMaze(mazeSize, mazeSize);

		maze.setSeed(world.getRandomSeed() + (long) (this.boundingBox.minX * 90342903)
			+ (long) (y * 90342903) ^ (long) this.boundingBox.minZ);

		for (int i = 0; i < 13; i++) {
			maze.putRaw(i, 0, 5);
			maze.putRaw(i, 12, 5);
			maze.putRaw(0, i, 5);
			maze.putRaw(12, i, 5);
		}

		maze.doorRarity = 0.3f;

		switch (rotation) {
			case 1 -> {
				for (int x = 7; x < 12; x++) {
					for (int z = 1; z < 6; z++) {
						maze.putRaw(x, z, 5);
					}
				}
				maze.putRaw(6, 1, 5);
				maze.putRaw(5, 1, 5);
				maze.putRaw(4, 1, 5);
				maze.putRaw(3, 1, 5);
				maze.putRaw(2, 1, 6);
				maze.putRaw(11, 6, 5);
				maze.putRaw(11, 7, 5);
				maze.putRaw(11, 8, 6);
				maze.generateRecursiveBacktracker(0, 0);
			}
			case 2 -> {
				for (int x = 7; x < 12; x++) {
					for (int z = 7; z < 12; z++) {
						maze.putRaw(x, z, 5);
					}
				}
				maze.putRaw(11, 6, 5);
				maze.putRaw(11, 5, 5);
				maze.putRaw(11, 4, 5);
				maze.putRaw(11, 3, 5);
				maze.putRaw(11, 2, 6);
				maze.putRaw(6, 11, 5);
				maze.putRaw(5, 11, 5);
				maze.putRaw(4, 11, 6);
				maze.generateRecursiveBacktracker(5, 0);
			}
			case 3 -> {
				for (int x = 1; x < 6; x++) {
					for (int z = 7; z < 12; z++) {
						maze.putRaw(x, z, 5);
					}
				}
				maze.putRaw(6, 11, 5);
				maze.putRaw(7, 11, 5);
				maze.putRaw(8, 11, 5);
				maze.putRaw(9, 11, 5);
				maze.putRaw(10, 11, 6);
				maze.putRaw(1, 6, 5);
				maze.putRaw(1, 5, 5);
				maze.putRaw(1, 4, 6);
				maze.generateRecursiveBacktracker(5, 5);
			}
			default -> {
				for (int x = 1; x < 6; x++) {
					for (int z = 1; z < 6; z++) {
						maze.putRaw(x, z, 5);
					}
				}
				maze.putRaw(1, 6, 5);
				maze.putRaw(1, 7, 5);
				maze.putRaw(1, 8, 5);
				maze.putRaw(1, 9, 5);
				maze.putRaw(1, 10, 6);
				maze.putRaw(6, 1, 5);
				maze.putRaw(7, 1, 5);
				maze.putRaw(8, 1, 6);
				maze.generateRecursiveBacktracker(0, 5);
			}
		}

		maze.wallBlockId = this.deco.blockID;
		maze.wallBlockMeta = this.deco.blockMeta;
		maze.headBlockId = this.deco.accentID;
		maze.headBlockMeta = this.deco.accentMeta;
		maze.pillarBlockId = this.deco.accentID;
		maze.pillarBlockMeta = this.deco.accentMeta;
		maze.doorBlockId = TFBlocks.TOWER_DEVICE.id();
		maze.doorBlockMeta = BlockLogicTFTowerDevice.META_REAPPEARING_INACTIVE;
		maze.torchRarity = 0.0f;
		maze.tall = 3;
		maze.head = 1;
		maze.oddBias = 2;

		maze.copyToStructure(world, 0, y + 1, 0, this, clip);
		decorateMazeDeadEnds(world, decoRNG, maze, y, rotation, clip);
	}

	protected void decorateMazeDeadEnds(World world, Random decoRNG, TFMaze maze, int y, int rotation,
	                                    BoundingBox clip) {
		for (int x = 0; x < maze.width; x++) {
			for (int z = 0; z < maze.depth; z++) {
				boolean west = maze.isWall(x, z, x - 1, z);
				boolean east = maze.isWall(x, z, x + 1, z);
				boolean north = maze.isWall(x, z, x, z - 1);
				boolean south = maze.isWall(x, z, x, z + 1);

				if (!west && east && north && south) {
					decorateDeadEnd(world, decoRNG, x, y, z, 3, rotation, clip);
				}
				if (west && !east && north && south) {
					decorateDeadEnd(world, decoRNG, x, y, z, 1, rotation, clip);
				}
				if (west && east && !north && south) {
					decorateDeadEnd(world, decoRNG, x, y, z, 0, rotation, clip);
				}
				if (west && east && north && !south) {
					decorateDeadEnd(world, decoRNG, x, y, z, 2, rotation, clip);
				}
			}
		}
	}

	private void decorateDeadEnd(World world, Random decoRNG, int mx, int y, int mz, int facing,
	                             int rotation, BoundingBox clip) {
		int x = mx * 3 + 1;
		int z = mz * 3 + 1;
		int chest = Blocks.CHEST_PLANKS_OAK.id();

		switch (facing) {
			case 1 -> {
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, y + 1, z, clip);
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, y + 1, z + 1, clip);
				placeBlock(world, chest, rotation, x, y + 2, z, clip);
				placeTreasure(world, decoRNG, x, y + 2, z + 1, TFTreasure.DARKTOWER_CACHE, clip);
			}
			case 2 -> {
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, y + 1, z, clip);
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z, clip);
				placeBlock(world, chest, rotation, x, y + 2, z, clip);
				placeTreasure(world, decoRNG, x + 1, y + 2, z, TFTreasure.DARKTOWER_CACHE, clip);
			}
			case 3 -> {
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z, clip);
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z + 1, clip);
				placeBlock(world, chest, rotation, x + 1, y + 2, z, clip);
				placeTreasure(world, decoRNG, x + 1, y + 2, z + 1, TFTreasure.DARKTOWER_CACHE, clip);
			}
			default -> {
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, y + 1, z + 1, clip);
				placeBlock(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 1, z + 1, clip);
				placeBlock(world, chest, 0, x, y + 2, z + 1, clip);
				placeTreasure(world, decoRNG, x + 1, y + 2, z + 1, TFTreasure.DARKTOWER_CACHE, clip);
			}
		}
	}

	private void decorateUnbuilderMaze(World world, Random decoRNG, BoundingBox clip, int rotation,
	                                   int y) {
		for (int x = this.size / 2; x < this.size - 1; x++) {
			for (int z = 3; z < this.size - 1; z++) {
				if (x % 2 == 1 && z % 2 == 1) {
					for (int py = 1; py < 5; py++) {
						placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta,
							x, y + py, z, rotation, clip);
					}
					continue;
				}
				if (x % 2 != 1 && z % 2 != 1) {
					continue;
				}

				for (int py = 1; py < 5; py++) {
					placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta,
						x, y + py, z, rotation, clip);
				}

				if (x == this.size / 2 || x == this.size - 2 || z == this.size - 2) {
					continue;
				}

				int ay = decoRNG.nextInt(4) + 1;
				placeBlockRotated(world, 0, 0, x, y + ay, z, rotation, clip);

				if (x <= this.size - 7) {
					continue;
				}
				ay = decoRNG.nextInt(3) + 1;
				placeBlockRotated(world, 0, 0, x, y + ay, z, rotation, clip);
			}
		}

		int device = TFBlocks.TOWER_DEVICE.id();
		int antibuilder = BlockLogicTFTowerDevice.META_ANTIBUILDER;
		placeBlockRotated(world, device, antibuilder, 15, y + 2, 7, rotation, clip);
		placeBlockRotated(world, device, antibuilder, 11, y + 3, 7, rotation, clip);
		placeBlockRotated(world, device, antibuilder, 15, y + 2, 13, rotation, clip);
		placeBlockRotated(world, device, antibuilder, 11, y + 3, 13, rotation, clip);
		placeBlockRotated(world, device, antibuilder, 5, y + 3, 13, rotation, clip);
	}

	private void decorateLounge(World world, Random decoRNG, BoundingBox clip, int rotation, int y) {
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;

		fillBlocksRotated(world, clip, 17, y + 1, 1, 17, y + 4, 6,
			this.deco.pillarID, this.deco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 1, 17, y + 4, 1,
			this.deco.pillarID, this.deco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 13, y + 1, 2, 16, y + 1, 5,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 2, 12, y + 1, 6,
			this.deco.stairID, getStairMeta(rotation), rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 6, 16, y + 1, 6,
			this.deco.stairID, getStairMeta(3 + rotation), rotation);

		makeDispenserPillar(world, this.deco, 13, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeDispenserPillar(world, this.deco, 15, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeDispenserPillar(world, this.deco, 17, y, 3, getStairMeta(rotation), rotation, clip);
		makeDispenserPillar(world, this.deco, 17, y, 5, getStairMeta(rotation), rotation, clip);
		makeStonePillar(world, this.deco, 12, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeStonePillar(world, this.deco, 17, y, 6, getStairMeta(rotation), rotation, clip);

		fillBlocksRotated(world, clip, 10, y + 1, 17, 17, y + 4, 17,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 17, y + 1, 10, 17, y + 4, 17,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 11, y + 1, 17, 12, y + 4, 17, BOOKSHELF, 0, rotation);
		fillBlocksRotated(world, clip, 14, y + 1, 17, 15, y + 4, 17, BOOKSHELF, 0, rotation);
		fillBlocksRotated(world, clip, 17, y + 1, 11, 17, y + 4, 12, BOOKSHELF, 0, rotation);
		fillBlocksRotated(world, clip, 17, y + 1, 14, 17, y + 4, 15, BOOKSHELF, 0, rotation);

		placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation) | flip, 13, y + 1, 14, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation) | flip, 14, y + 1, 14, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation) | flip, 14, y + 1, 13, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(1 + rotation) | flip, 13, y + 1, 13, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation), 11, y + 1, 13, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation), 13, y + 1, 11, rotation, clip);

		placeBlockRotated(world, LAMP, 0, 8, y + 3, 8, rotation, clip);
		placeBlockRotated(world, LEVER,
			decoRNG.nextBoolean() ? BlockLogicLever.ROTATION_BOTTOM_NS : BlockLogicLever.ROTATION_BOTTOM_WE,
			8, y + 2, 8, rotation, clip);

		placeTreePlanter(world, decoRNG.nextInt(5), 6, y + 1, 12, rotation, clip);
	}

	private void makeDispenserPillar(World world, StructureTFDecorator myDeco, int x, int y, int z,
	                                 int stairMeta, int rotation, BoundingBox clip) {
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;

		placeBlockRotated(world, myDeco.stairID, stairMeta | flip, x, y + 2, z, rotation, clip);
		placeBlockRotated(world, Blocks.DISPENSER_COBBLE_STONE.id(), stairMeta + 4, x, y + 3, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, stairMeta, x, y + 4, z, rotation, clip);
	}

	private void decorateExperiment(World world, Random decoRNG, BoundingBox clip, int rotation,
	                                int y) {
		fillBlocksRotated(world, clip, 17, y + 1, 1, 17, y + 4, 6,
			this.deco.pillarID, this.deco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 1, 17, y + 4, 1,
			this.deco.pillarID, this.deco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 13, y + 1, 2, 16, y + 1, 5,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 2, 12, y + 1, 6,
			this.deco.stairID, getStairMeta(rotation), rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 6, 16, y + 1, 6,
			this.deco.stairID, getStairMeta(3 + rotation), rotation);

		makeWoodPillar(world, 13, y, 1, rotation, clip);
		makeWoodPillar(world, 15, y, 1, rotation, clip);
		makeWoodPillar(world, 17, y, 3, rotation, clip);
		makeWoodPillar(world, 17, y, 5, rotation, clip);
		makeStonePillar(world, this.deco, 12, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeStonePillar(world, this.deco, 17, y, 6, getStairMeta(rotation), rotation, clip);

		placeBlockRotated(world, Blocks.WORKBENCH.id(), 0, 14, y + 2, 4, rotation, clip);

		int obsidian = Blocks.OBSIDIAN.id();
		int redstoneOre = Blocks.ORE_REDSTONE_STONE.id();

		placeBlockRotated(world, obsidian, 0, 13, y + 1, 13, rotation, clip);
		placeBlockRotated(world, obsidian, 0, 15, y + 1, 13, rotation, clip);
		placeBlockRotated(world, obsidian, 0, 13, y + 1, 15, rotation, clip);
		placeBlockRotated(world, obsidian, 0, 15, y + 1, 15, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 13, y + 1, 14, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 14, y + 1, 13, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 15, y + 1, 14, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 14, y + 1, 15, rotation, clip);
		placeBlockRotated(world, redstoneOre, 0, 14, y + 1, 14, rotation, clip);

		placeBlockRotated(world, NETHERRACK, 0, 13, y + 2, 13, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 15, y + 2, 13, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 13, y + 2, 15, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 15, y + 2, 15, rotation, clip);
		placeBlockRotated(world, TFBlocks.TOWER_DEVICE.id(),
			BlockLogicTFTowerDevice.META_REACTOR_INACTIVE, 14, y + 2, 14, rotation, clip);

		placeBlockRotated(world, obsidian, 0, 13, y + 3, 13, rotation, clip);
		placeBlockRotated(world, obsidian, 0, 15, y + 3, 13, rotation, clip);
		placeBlockRotated(world, obsidian, 0, 13, y + 3, 15, rotation, clip);
		placeBlockRotated(world, obsidian, 0, 15, y + 3, 15, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 13, y + 3, 14, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 14, y + 3, 13, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 15, y + 3, 14, rotation, clip);
		placeBlockRotated(world, NETHERRACK, 0, 14, y + 3, 15, rotation, clip);
		placeBlockRotated(world, redstoneOre, 0, 14, y + 3, 14, rotation, clip);

		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 14, y + 1, 17, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 4), 13, y + 1, 17, rotation, clip);
		placeBlockRotated(world, PISTON, getPistonMeta(3 + rotation), 14, y + 2, 17, rotation, clip);
		placeBlockRotated(world, redstoneOre, 0, 14, y + 2, 16, rotation, clip);

		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 17, y + 1, 14, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 2), 17, y + 1, 13, rotation, clip);
		placeBlockRotated(world, PISTON, getPistonMeta(2 + rotation), 17, y + 2, 14, rotation, clip);
		placeBlockRotated(world, redstoneOre, 0, 16, y + 2, 14, rotation, clip);

		placeBlockRotated(world, redstoneOre, 0, 14, y + 2, 11, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 14, y + 1, 11, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 4) | BlockLogicLever.MASK_POWERED,
			13, y + 1, 11, rotation, clip);
		placeBlockRotated(world, PISTON, getPistonMeta(1 + rotation), 14, y + 2, 10, rotation, clip);

		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 14, y + 1, 9, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 4), 13, y + 1, 9, rotation, clip);
		placeBlockRotated(world, PISTON_STICKY, getPistonMeta(1 + rotation), 14, y + 2, 9, rotation, clip);

		placeBlockRotated(world, redstoneOre, 0, 11, y + 2, 14, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 11, y + 1, 14, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 2) | BlockLogicLever.MASK_POWERED,
			11, y + 1, 13, rotation, clip);
		placeBlockRotated(world, PISTON, getPistonMeta(rotation), 10, y + 2, 14, rotation, clip);

		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 9, y + 1, 14, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 2), 9, y + 1, 13, rotation, clip);
		placeBlockRotated(world, PISTON_STICKY, getPistonMeta(rotation), 9, y + 2, 14, rotation, clip);
	}

	private void makeWoodPillar(World world, int x, int y, int z, int rotation, BoundingBox clip) {
		placeBlockRotated(world, beamBlock(), LOG_AXIS_Y, x, y + 2, z, rotation, clip);
		placeBlockRotated(world, beamBlock(), LOG_AXIS_Y, x, y + 3, z, rotation, clip);
		placeBlockRotated(world, beamBlock(), LOG_AXIS_Y, x, y + 4, z, rotation, clip);
	}

	private void decorateAquarium(World world, Random decoRNG, BoundingBox clip, int rotation,
	                              int y) {

		int water = Blocks.FLUID_WATER_FLOWING.id();

		makePillarFrame(world, clip, this.deco, rotation, 12, y, 3, 4, 4, 13, false);
		fillBlocksRotated(world, clip, 13, y + 4, 4, 14, y + 4, 14, water, 0, rotation);

		makePillarFrame(world, clip, this.deco, rotation, 6, y, 12, 4, 4, 4, false);
		fillBlocksRotated(world, clip, 6, y + 5, 12, 9, y + 5, 15,
			this.deco.accentID, this.deco.accentMeta, rotation);
		fillBlocksRotated(world, clip, 7, y + 4, 13, 8, y + 5, 14, water, 0, rotation);
	}

	private void decorateForge(World world, Random decoRNG, BoundingBox clip, int rotation, int y) {
		StructureTFDecorator forgeDeco = this.deco;

		fillBlocksRotated(world, clip, 17, y + 1, 1, 17, y + 4, 6, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 1, 17, y + 4, 1, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 17, 17, y + 4, 17, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);
		fillBlocksRotated(world, clip, 17, y + 1, 12, 17, y + 4, 17, forgeDeco.pillarID, forgeDeco.pillarMeta, rotation);

		fillBlocksRotated(world, clip, 13, y + 1, 2, 16, y + 1, 5, forgeDeco.blockID, forgeDeco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 2, 12, y + 1, 6, forgeDeco.stairID, getStairMeta(rotation), rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 6, 16, y + 1, 6, forgeDeco.stairID, getStairMeta(3 + rotation), rotation);
		fillBlocksRotated(world, clip, 13, y + 1, 13, 16, y + 1, 16, forgeDeco.blockID, forgeDeco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 12, 12, y + 1, 16, forgeDeco.stairID, getStairMeta(rotation), rotation);
		fillBlocksRotated(world, clip, 12, y + 1, 12, 16, y + 1, 12, forgeDeco.stairID, getStairMeta(1 + rotation), rotation);

		makeFurnacePillar(world, forgeDeco, decoRNG, 13, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 15, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 3, getStairMeta(rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 5, getStairMeta(rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 13, y, 17, getStairMeta(1 + rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 15, y, 17, getStairMeta(1 + rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 13, getStairMeta(rotation), rotation, clip);
		makeFurnacePillar(world, forgeDeco, decoRNG, 17, y, 15, getStairMeta(rotation), rotation, clip);

		makeStonePillar(world, forgeDeco, 12, y, 1, getStairMeta(3 + rotation), rotation, clip);
		makeStonePillar(world, forgeDeco, 17, y, 6, getStairMeta(rotation), rotation, clip);
		makeStonePillar(world, forgeDeco, 12, y, 17, getStairMeta(1 + rotation), rotation, clip);
		makeStonePillar(world, forgeDeco, 17, y, 12, getStairMeta(rotation), rotation, clip);
		makeStonePillar(world, forgeDeco, 17, y, 9, getStairMeta(rotation), rotation, clip);
		makeStonePillar(world, forgeDeco, 9, y, 17, getStairMeta(1 + rotation), rotation, clip);

		decoRNG.nextInt(16);
		placeBlockRotated(world, Blocks.BLOCK_IRON.id(), 0, 13, y + 2, 5, rotation, clip);
		decoRNG.nextInt(16);
		placeBlockRotated(world, Blocks.BLOCK_IRON.id(), 0, 13, y + 2, 13, rotation, clip);

		makeFirePit(world, forgeDeco, 6, y + 1, 12, rotation, clip);
	}

	private void makeFurnacePillar(World world, StructureTFDecorator forgeDeco, Random rand,
	                               int x, int y, int z, int stairMeta, int rotation,
	                               BoundingBox clip) {
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;
		int furnace = Blocks.FURNACE_STONE_IDLE.id();

		placeBlockRotated(world, forgeDeco.stairID, stairMeta | flip, x, y + 2, z, rotation, clip);
		placeBlockRotated(world, furnace, stairMeta + 4, x, y + 3, z, rotation, clip);

		int amount = rand.nextBoolean() ? rand.nextInt(5) + 4 : 0;
		if (amount > 0) {
			int dx = getXWithOffsetAsIfRotated(x, z, rotation);
			int dy = getYWithOffset(y + 3);
			int dz = getZWithOffsetAsIfRotated(x, z, rotation);

			if (clip.contains(dx, dy, dz) && world.getBlockId(dx, dy, dz) == furnace) {
				TileEntity tileEntity = world.getTileEntity(dx, dy, dz);
				if (tileEntity instanceof Container inventory) {
					inventory.setItem(1, new ItemStack(Items.COAL, amount, 1));
				}
			}
		}

		placeBlockRotated(world, forgeDeco.stairID, stairMeta, x, y + 4, z, rotation, clip);
	}

	private void makeStonePillar(World world, StructureTFDecorator forgeDeco, int x, int y, int z,
	                             int stairMeta, int rotation, BoundingBox clip) {
		for (int py = 1; py <= 4; py++) {
			placeBlockRotated(world, forgeDeco.pillarID, forgeDeco.pillarMeta,
				x, y + py, z, rotation, clip);
		}

		int sx = getXWithOffsetAsIfRotated(x, z, rotation);
		int sy = getYWithOffset(y + 1);
		int sz = getZWithOffsetAsIfRotated(x, z, rotation);

		switch (stairMeta) {
			case 0 -> sx++;
			case 1 -> sx--;
			case 2 -> sz++;
			case 3 -> sz--;
			default -> { }
		}

		if (clip.contains(sx, sy, sz)) {
			world.setBlockAndMetadataRaw(sx, sy, sz, forgeDeco.stairID, stairMeta);
			world.setBlockAndMetadataRaw(sx, sy + 3, sz, forgeDeco.stairID,
				stairMeta | BlockLogicStairs.MASK_ROTATION_VERTICAL);
		}
	}

	private void makeFirePit(World world, StructureTFDecorator myDeco, int x, int y, int z,
	                         int rotation, BoundingBox clip) {
		placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y, z + 1, rotation, clip);
		placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y, z - 1, rotation, clip);
		placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y, z + 1, rotation, clip);
		placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y, z - 1, rotation, clip);

		placeBlockRotated(world, myDeco.stairID, getStairMeta(rotation), x - 1, y, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(2 + rotation), x + 1, y, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(3 + rotation), x, y, z + 1, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(1 + rotation), x, y, z - 1, rotation, clip);

		placeBlockRotated(world, NETHERRACK, 0, x, y, z, rotation, clip);
		placeBlockRotated(world, FIRE, 0, x, y + 1, z, rotation, clip);
	}

	private void decorateNetherwart(World world, Random decoRNG, BoundingBox clip, int rotation,
	                                int y, boolean isTop) {
		StructureTFDecorator netherDeco = this.deco;
		int soulSand = Blocks.SOULSAND.id();
		int wartStandIn = Blocks.MUSHROOM_RED.id();

		makePillarFrame(world, clip, netherDeco, rotation, 12, y, 9, 4, 4, 7, true);
		fillBlocksRotated(world, clip, 13, y + 1, 10, 14, y + 1, 14, soulSand, 0, rotation);
		fillBlocksRotated(world, clip, 13, y + 2, 10, 14, y + 2, 14, wartStandIn, 0, rotation);
		fillBlocksRotated(world, clip, 13, y + 4, 10, 14, y + 4, 14, soulSand, 0, rotation);

		makePillarFrame(world, clip, netherDeco, rotation, 5, y, 12, 3, isTop ? 4 : 9, 3, true);
		placeBlockRotated(world, netherDeco.blockID, netherDeco.blockMeta, 6, y + 1, 13, rotation, clip);
		placeBlockRotated(world, netherDeco.blockID, netherDeco.blockMeta,
			6, y + (isTop ? 4 : 9), 13, rotation, clip);
		placeSpawnerRotated(world, 6, y + 3, 13, rotation, BLAZE_STANDIN, clip);

		destroyTower(world, decoRNG, 12, y, 3, 2, clip);
	}

	private void decorateBotanical(World world, Random decoRNG, BoundingBox clip, int rotation,
	                               int y) {
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;

		makePillarFrame(world, clip, this.deco, rotation, 12, y, 12, 4, 4, 4, true);
		fillBlocksRotated(world, clip, 13, y + 1, 13, 14, y + 1, 14,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, 13, y + 4, 13, 14, y + 4, 14,
			this.deco.blockID, this.deco.blockMeta, rotation);

		placeRandomPlant(world, decoRNG, 13, y + 2, 13, rotation, clip);
		placeRandomPlant(world, decoRNG, 13, y + 2, 14, rotation, clip);
		placeRandomPlant(world, decoRNG, 14, y + 2, 13, rotation, clip);
		placeRandomPlant(world, decoRNG, 14, y + 2, 14, rotation, clip);

		for (int py = 1; py <= 4; py++) {
			placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, 12, y + py, 4, rotation, clip);
			placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, 15, y + py, 4, rotation, clip);
		}

		placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation) | flip, 13, y + 1, 4, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation) | flip, 14, y + 1, 4, rotation, clip);
		placeTreasureRotated(world, world.rand, 13, y + 2, 4, rotation, TFTreasure.BASEMENT, clip);
		placeBlockRotated(world, Blocks.WORKBENCH.id(), 0, 14, y + 2, 4, rotation, clip);

		placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation) | flip, 12, y + 1, 7, rotation, clip);
		placeBlockRotated(world, Blocks.SLAB_PLANKS_OAK.id(), SLAB_UPPER, 13, y + 1, 7, rotation, clip);
		placeBlockRotated(world, Blocks.SLAB_PLANKS_OAK.id(), SLAB_UPPER, 14, y + 1, 7, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation) | flip, 15, y + 1, 7, rotation, clip);

		placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation) | flip, 12, y + 1, 10, rotation, clip);
		placeBlockRotated(world, Blocks.SLAB_PLANKS_OAK.id(), SLAB_UPPER, 13, y + 1, 10, rotation, clip);
		placeBlockRotated(world, Blocks.SLAB_PLANKS_OAK.id(), SLAB_UPPER, 14, y + 1, 10, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation) | flip, 15, y + 1, 10, rotation, clip);

		for (int x = 12; x <= 15; x++) {
			placeRandomPlant(world, decoRNG, x, y + 2, 7, rotation, clip);
			placeRandomPlant(world, decoRNG, x, y + 2, 10, rotation, clip);
		}

		placeTreePlanter(world, decoRNG.nextInt(5), 6, y + 1, 12, rotation, clip);
	}

	private void placeTreePlanter(World world, int treeNum, int x, int y, int z, int rotation,
	                              BoundingBox clip) {
		placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x + 1, y, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x + 1, y, z - 1, rotation, clip);
		placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x - 1, y, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.pillarID, this.deco.pillarMeta, x - 1, y, z - 1, rotation, clip);

		placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation), x - 1, y, z, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(2 + rotation), x + 1, y, z, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation), x, y, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(1 + rotation), x, y, z - 1, rotation, clip);

		placeBlockRotated(world, Blocks.DIRT.id(), 0, x, y, z, rotation, clip);

		int dx = getXWithOffsetAsIfRotated(x, z, rotation);
		int dy = getYWithOffset(y + 1);
		int dz = getZWithOffsetAsIfRotated(x, z, rotation);

		if (!clip.contains(dx, dy, dz)) {
			return;
		}

		WorldFeature treeGen = switch (treeNum) {
			case 1 -> new WorldFeatureTree(Blocks.LEAVES_EUCALYPTUS.id(), Blocks.LOG_EUCALYPTUS.id(), 3);
			case 2 -> new WorldFeatureTreeFancy(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id());
			case 3 -> new WorldFeatureTree(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
			case 4 -> new WorldFeatureTree(TFBlocks.LEAVES_RAINBOW.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
			default -> new WorldFeatureTree(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id(), 4);
		};

		for (int i = 0; i < 100 && !treeGen.place(world, world.rand, dx, dy, dz); i++) {

		}
	}

	private void placeRandomPlant(World world, Random decoRNG, int x, int y, int z, int rotation,
	                              BoundingBox clip) {
		int potMeta = decoRNG.nextInt(12);

		int plant = switch (potMeta) {
			case 1 -> Blocks.FLOWER_RED.id();
			case 2 -> Blocks.FLOWER_YELLOW.id();
			case 3 -> Blocks.SAPLING_OAK.id();
			case 4 -> Blocks.SAPLING_PINE.id();
			case 5 -> Blocks.SAPLING_BIRCH.id();
			case 6 -> Blocks.SAPLING_EUCALYPTUS.id();
			case 7 -> Blocks.MUSHROOM_RED.id();
			case 8 -> Blocks.MUSHROOM_BROWN.id();
			case 9 -> Blocks.CACTUS.id();
			case 10 -> Blocks.DEADBUSH.id();
			case 11 -> Blocks.TALLGRASS_FERN.id();
			default -> 0;
		};

		if (plant != 0) {
			placeBlockRotated(world, plant, 0, x, y, z, rotation, clip);
		}
	}

	private void makeBottomEntrance(World world, Random decoRNG, BoundingBox clip, int rotation,
	                                int y) {
		makeFirePit(world, this.deco, 13, y + 1, 3, rotation, clip);
		makeFirePit(world, this.deco, 3, y + 1, 13, rotation, clip);
		makeFirePit(world, this.deco, 13, y + 1, 13, rotation, clip);
		makePillarFrame(world, clip, this.deco, rotation, 7, y, 7, 3, 4, 3, false);
	}

	protected void addTimberMaze(World world, Random rand, BoundingBox clip, int bottom, int top) {
		int spacing = 5;
		int floorside = 0;

		if (bottom == 0) {
			bottom += spacing;
		}

		for (int y = bottom; y < top; y += spacing) {
			floorside++;
			floorside %= 4;
			makeTimberBeams(world, rand, clip, floorside, y,
				y == bottom && bottom != spacing, y >= top - spacing, top);
		}
	}

	protected void makeTimberBeams(World world, Random rand, BoundingBox clip, int rotation, int y,
	                               boolean isBottom, boolean isTop, int top) {
		int beamId = beamBlock();
		int beamMetaNS = (this.coordBaseMode + rotation) % 2 == 0 ? LOG_AXIS_X : LOG_AXIS_Z;
		int beamMetaEW = beamMetaNS == LOG_AXIS_X ? LOG_AXIS_Z : LOG_AXIS_X;

		for (int z = 1; z < this.size - 1; z++) {
			placeBlockRotated(world, beamId, beamMetaEW, 4, y, z, rotation, clip);
			placeBlockRotated(world, beamId, beamMetaEW, 9, y, z, rotation, clip);
			placeBlockRotated(world, beamId, beamMetaEW, 14, y, z, rotation, clip);
		}

		int z1cross = pickBetweenExcluding(3, this.size - 3, rand, 4, 9, 14);
		for (int x = 5; x < 9; x++) {
			placeBlockRotated(world, beamId, beamMetaNS, x, y, z1cross, rotation, clip);
		}

		int z2cross = pickBetweenExcluding(3, this.size - 3, rand, 4, 9, 14);
		for (int x = 10; x < 14; x++) {
			placeBlockRotated(world, beamId, beamMetaNS, x, y, z2cross, rotation, clip);
		}

		int x1 = 4;
		int z1 = pickFrom(rand, 4, 9, 14);
		int x2 = 9;
		int z2 = pickFrom(rand, 4, 9, 14);
		int x3 = 14;
		int z3 = pickFrom(rand, 4, 9, 14);

		for (int by = 1; by < 5; by++) {
			if (!isBottom || checkPost(world, x1, y - 5, z1, rotation, clip)) {
				placeBlockRotated(world, beamId, LOG_AXIS_Y, x1, y - by, z1, rotation, clip);
				placeBlockRotated(world, LADDER, getLadderMeta(2, rotation), x1 + 1, y - by, z1, rotation, clip);
			}
			if (!isBottom || checkPost(world, x2, y - 5, z2, rotation, clip)) {

				placeBlockRotated(world, beamId, LOG_AXIS_Y, x2, y - by, z2, rotation, clip);
			}
			if (isBottom && !checkPost(world, x3, y - 5, z3, rotation, clip)) {
				continue;
			}
			placeBlockRotated(world, beamId, LOG_AXIS_Y, x3, y - by, z3, rotation, clip);
			placeBlockRotated(world, LADDER, getLadderMeta(4, rotation), x3 - 1, y - by, z3, rotation, clip);
		}

		if (isTop) {
			int topFloorRotation = (this.boundingBox.minY + top + 1) % 4;
			int ladderX = 4;
			int ladderZ = 10;
			int ladderMeta = 3;

			for (int by = 1; by < 5; by++) {
				placeBlockRotated(world, beamId, LOG_AXIS_Y, ladderX, y + by, 9, topFloorRotation, clip);
				placeBlockRotated(world, LADDER, getLadderMeta(ladderMeta, topFloorRotation),
					ladderX, y + by, ladderZ, topFloorRotation, clip);
			}

			placeBlockRotated(world, 0, 0, ladderX, y + 6, 9, topFloorRotation, clip);
			placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX + 1, y + 5, ladderZ, topFloorRotation, clip);
			placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX - 1, y + 5, ladderZ, topFloorRotation, clip);
			placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX + 1, y + 6, ladderZ, topFloorRotation, clip);
			placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, ladderX - 1, y + 6, ladderZ, topFloorRotation, clip);
		}

		if (!isBottom && !isTop) {
			int sx = pickFrom(rand, 6, 7, 11);
			int sz = pickFrom(rand, 6, 11, 12);
			makeMiniGhastSpawner(world, rand, y, sx, sz, clip);
		}

		int lx = pickFrom(rand, 2, 12, 16);
		int lz = 2 + rand.nextInt(15);
		placeBlockRotated(world, LAMP, 0, lx, y + 2, lz, rotation, clip);
		placeBlockRotated(world, LEVER,
			rand.nextBoolean() ? BlockLogicLever.ROTATION_BOTTOM_NS : BlockLogicLever.ROTATION_BOTTOM_WE,
			lx, y + 1, lz, rotation, clip);
	}

	private void makeMiniGhastSpawner(World world, Random rand, int y, int sx, int sz,
	                                  BoundingBox clip) {
		placeSpawner(world, rand, sx, y + 2, sz, MINI_GHAST, clip);
	}

	protected void addBuilderPlatforms(World world, Random rand, BoundingBox clip, int bottom,
	                                   int top) {
		int spacing = 5;
		int floorside = 0;

		if (bottom == 0) {
			bottom += spacing;
		}

		for (int y = bottom; y < top - spacing; y += spacing) {
			makeBuilderPlatforms(world, rand, clip, floorside, y,
				y == bottom && bottom != spacing, y >= top - spacing);
			floorside += 1 + rand.nextInt(3);
			floorside %= 4;
		}

		makeBuilderPlatform(world, rand, 1, bottom, 5, true, clip);
		makeBuilderPlatform(world, rand, 3, bottom, 5, true, clip);

		for (int y = bottom - 4; y < bottom; y++) {
			placeBlockRotated(world, LADDER, getLadderMeta(2, 1), 1, y, 5, 1, clip);
			placeBlockRotated(world, LADDER, getLadderMeta(2, 3), 1, y, 5, 3, clip);
		}

		addTopBuilderPlatform(world, rand, bottom, top, spacing, clip);
	}

	protected void makeBuilderPlatforms(World world, Random rand, BoundingBox clip, int rotation,
	                                    int y, boolean bottom, boolean top) {
		int z = this.size / 2 + rand.nextInt(5) - rand.nextInt(5);

		makeBuilderPlatform(world, rand, rotation, y, z, false, clip);

		placeBlockRotated(world, LADDER, getLadderMeta(2, rotation), 1, y + 1, z, rotation, clip);
		placeBlockRotated(world, LADDER, getLadderMeta(2, rotation), 1, y + 2, z, rotation, clip);
		placeBlockRotated(world, LADDER, getLadderMeta(2, rotation), 1, y + 3, z, rotation, clip);
		placeBlockRotated(world, LADDER, getLadderMeta(2, rotation), 1, y + 4, z, rotation, clip);

		makeBuilderPlatform(world, rand, rotation, y + 5, z, true, clip);

		if (y % 2 == 1) {
			int sx = pickFrom(rand, 5, 9, 13);
			int sz = sx == 9 ? (rand.nextBoolean() ? 5 : 13) : 9;
			placeBlockRotated(world, TFBlocks.TOWER_DEVICE.id(),
				BlockLogicTFTowerDevice.META_ANTIBUILDER, sx, y + 2, sz, rotation, clip);
		} else {
			int sx = rand.nextBoolean() ? 5 : 13;
			int sz = rand.nextBoolean() ? 5 : 13;
			makeLampCluster(world, rand, sx, y, sz, rotation, clip);
		}
	}

	private void addTopBuilderPlatform(World world, Random rand, int bottom, int top, int spacing,
	                                   BoundingBox clip) {
		int rotation = (this.boundingBox.minY + top + 1) % 4;

		fillBlocksRotated(world, clip, 5, top - spacing, 9, 7, top - spacing, 11,
			this.deco.accentID, this.deco.accentMeta, rotation);
		fillBlocksRotated(world, clip, 6, top - spacing, 9, 6, top, 9,
			this.deco.accentID, this.deco.accentMeta, rotation);
		fillBlocksRotated(world, clip, 6, top - spacing + 1, 10, 6, top - 1, 10,
			LADDER, getLadderMeta(3, rotation), rotation);

		placeBlockRotated(world, 0, 0, 6, top + 1, 9, rotation, clip);
		placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 5, top, 10, rotation, clip);
		placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 7, top, 10, rotation, clip);
		placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 5, top + 1, 10, rotation, clip);
		placeBlockRotated(world, this.deco.fenceID, this.deco.fenceMeta, 7, top + 1, 10, rotation, clip);

		placeBlockRotated(world, TFBlocks.TOWER_DEVICE.id(),
			BlockLogicTFTowerDevice.META_BUILDER_INACTIVE, 7, top - spacing, 10, rotation, clip);
		placeBlockRotated(world, LEVER,
			rand.nextBoolean() ? BlockLogicLever.ROTATION_TOP_NS : BlockLogicLever.ROTATION_TOP_WE,
			7, top - spacing + 1, 11, rotation, clip);
	}

	private void makeBuilderPlatform(World world, Random rand, int rotation, int y, int z,
	                                 boolean hole, BoundingBox clip) {
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 1, y, z - 1, rotation, clip);
		if (!hole) {
			placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 1, y, z, rotation, clip);
		}
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 1, y, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 2, y, z - 1, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 2, y, z, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, 2, y, z + 1, rotation, clip);

		placeBlockRotated(world, TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.META_BUILDER_INACTIVE,
			2, y, hole ? z + 1 : z - 1, rotation, clip);
		placeBlockRotated(world, LEVER,
			rand.nextBoolean() ? BlockLogicLever.ROTATION_TOP_NS : BlockLogicLever.ROTATION_TOP_WE,
			2, y + 1, z, rotation, clip);
	}

	private void makeLampCluster(World world, Random rand, int sx, int y, int sz, int rotation,
	                             BoundingBox clip) {
		int radius = 4;

		for (int i = 0; i < 5; i++) {
			int lx = sx;
			int ly = y;
			int lz = sz;

			for (int move = 0; move < 10; move++) {
				placeBlockRotated(world, LAMP, 0, lx, ly, lz, rotation, clip);

				int direction = rand.nextInt(8);
				if (direction > 5) {
					direction -= 2;
				}

				lx += OFFSET_X[direction];
				if (lx > sx + radius || lx < sx - radius) {
					break;
				}
				ly += OFFSET_Y[direction];
				if (ly > y + radius || ly < y - radius) {
					break;
				}
				lz += OFFSET_Z[direction];
				if (lz > sz + radius || lz < sz - radius) {
					break;
				}
			}
		}

		for (int i = 0; i < 5; i++) {
			int lx = sx;
			int ly = y;
			int lz = sz;

			int[] directions = new int[10];
			for (int move = 0; move < 10; move++) {
				directions[move] = rand.nextInt(8);
				if (directions[move] > 5) {
					directions[move] -= 2;
				}
			}

			for (int move = 0; move < 10; move++) {
				int direction = directions[move];

				lx += OFFSET_X[direction];
				if (lx > sx + radius || lx < sx - radius) {
					break;
				}
				ly += OFFSET_Y[direction];
				if (ly > y + radius || ly < y - radius) {
					break;
				}
				lz += OFFSET_Z[direction];
				if (lz > sz + radius || lz < sz - radius) {
					break;
				}

				if (getBlockIdRotated(world, lx, ly, lz, rotation, clip) == LAMP) {
					continue;
				}
				placeBlockRotated(world, LEVER, getLeverMeta(rotation, direction),
					lx, ly, lz, rotation, clip);
				break;
			}
		}
	}
}
