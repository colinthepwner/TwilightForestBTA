package com.twilightforest.world.structure;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.BlockLogicTFTowerDevice;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.item.TFItems;
import com.twilightforest.world.feature.TFFeature;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.BlockLogicLever;
import net.minecraft.core.block.BlockLogicSlab;
import net.minecraft.core.block.BlockLogicStairs;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerWing extends ComponentTFTowerWing {

	protected static final int NETHERRACK = Blocks.NETHERRACK.id();
	protected static final int FIRE = Blocks.FIRE.id();
	protected static final int LEVER = Blocks.LEVER_COBBLE_STONE.id();
	protected static final int LAMP = Blocks.LAMP_IDLE.id();
	protected static final int PRESSURE_PLATE = Blocks.PRESSURE_PLATE_PLANKS_OAK.id();
	protected static final int REDSTONE_WIRE = Blocks.WIRE_REDSTONE.id();
	protected static final int REPEATER = Blocks.REPEATER_IDLE.id();
	protected static final int PISTON = Blocks.PISTON_BASE.id();
	protected static final int PISTON_STICKY = Blocks.PISTON_BASE_STICKY.id();

	protected static int beamBlock() {
		return TFBlocks.LOG_DARKWOOD.id();
	}

	protected static final int LOG_AXIS_Y = 0;
	protected static final int LOG_AXIS_Z = 1;
	protected static final int LOG_AXIS_X = 2;

	protected static final int SLAB_UPPER = BlockLogicSlab.STATE_UPPER;

	protected static final String TOWER_GOLEM = TwilightForest.MOD_ID + ":towergolem";
	protected static final String REDSCALE_BROODLING = TwilightForest.MOD_ID + ":redscalebroodling";

	protected static final int WING_CEILING =
		com.twilightforest.world.chunk.TFWorldConstants.WORLD_HEIGHT - 6;

	protected boolean keyTower = false;

	protected final List<EnumDarkTowerDoor> openingTypes = new ArrayList<>();

	protected ComponentTFDarkTowerWing(int componentType, int x, int y, int z,
	                                   int size, int height, int direction) {
		super(componentType, x, y, z, size, height, direction);
	}

	@Override
	public int featureType() {
		return TFFeature.DARK_TOWER;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		if (parent != null) {
			this.deco = parent.deco;
		}

		addOpening(0, 1, this.size / 2, 2);
		makeARoof(parent, pieces, rand);
		makeABeard(parent, pieces, rand);

		if (this.size > 10) {
			for (int direction = 0; direction < 4; direction++) {
				int[] dest = getValidOpening(rand, direction);
				int childSize = this.size - 2;
				int childHeight = validateChildHeight(
					this.height - 4 + rand.nextInt(10) - rand.nextInt(10), childSize);

				boolean madeWing = makeTowerWing(pieces, rand, componentType(),
					dest[0], dest[1], dest[2], this.size - 2, childHeight, direction);

				if (madeWing) {
					continue;
				}
				if (direction != 2 && !rand.nextBoolean()) {
					continue;
				}
				makeTowerBalcony(pieces, rand, componentType(), dest[0], dest[1], dest[2], direction);
			}
		} else if (rand.nextInt(4) == 0) {

			int direction = rand.nextInt(4);
			int[] dest = getValidOpening(rand, direction);
			makeTowerBalcony(pieces, rand, componentType(), dest[0], dest[1], dest[2], direction);
		}
	}

	protected int validateChildHeight(int childHeight, int childSize) {
		return childHeight / 4 * 4 + 1;
	}

	@Override
	public void makeARoof(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                      Random rand) {
		int index = componentType();

		ComponentTFDarkTowerRoof roof = switch (rand.nextInt(5)) {
			case 2 -> new ComponentTFDarkTowerRoofCactus(index, this);
			case 3 -> new ComponentTFDarkTowerRoofRings(index, this);
			case 4 -> new ComponentTFDarkTowerRoofFourPost(index, this);
			default -> new ComponentTFDarkTowerRoofAntenna(index, this);
		};

		pieces.add(roof);
		roof.buildComponent(this, pieces, rand);
		this.roofType = roof.getClass();
	}

	@Override
	public void makeABeard(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                       Random rand) {
		ComponentTFDarkTowerBeard beard = new ComponentTFDarkTowerBeard(componentType() + 1, this);
		pieces.add(beard);
		beard.buildComponent(this, pieces, rand);
	}

	@Override
	public boolean makeTowerWing(List<StructureComponentTF> pieces, Random rand, int index,
	                             int x, int y, int z, int wingSize, int wingHeight, int rotation) {
		if (wingHeight < 8) {
			return false;
		}

		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 5, direction);

		if (dx[1] + wingHeight > WING_CEILING) {
			return false;
		}

		ComponentTFDarkTowerBridge bridge = new ComponentTFDarkTowerBridge(
			index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);

		StructureComponentTF hit = findIntersecting(pieces, bridge.boundingBox);
		if (hit != null && hit != this) {
			return false;
		}
		hit = findIntersecting(pieces, bridge.wingBox());
		if (hit != null && hit != this) {
			return false;
		}

		pieces.add(bridge);
		bridge.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	protected boolean makeTowerBalcony(List<StructureComponentTF> pieces, Random rand, int index,
	                                   int x, int y, int z, int rotation) {
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 5, direction);

		ComponentTFDarkTowerBalcony balcony =
			new ComponentTFDarkTowerBalcony(index, dx[0], dx[1], dx[2], direction);

		StructureComponentTF hit = findIntersecting(pieces, balcony.boundingBox);
		if (hit != null && hit != this) {
			return false;
		}

		pieces.add(balcony);
		balcony.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation, EnumDarkTowerDoor.REAPPEARING);
		return true;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		Random decoRNG = new Random(world.getRandomSeed()
			+ (long) (this.boundingBox.minX * 321534781) ^ (long) (this.boundingBox.minZ * 756839));

		makeEncasedWalls(world, rand, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1);

		fillWithBlocks(world, clip, 1, 1, 1,
			this.size - 2, this.height - 2, this.size - 2, 0, 0, false);

		nullifySkyLightForBoundingBox(world);

		if (this.size > 9) {
			addHalfFloors(world, decoRNG, clip, 4, this.height - 1);
		} else if (decoRNG.nextInt(3) == 0) {

			addSmallTimberBeams(world, decoRNG, clip, 4, this.height - 1);
		} else {
			addHalfFloors(world, decoRNG, clip, 4, this.height - 1);
		}

		makeOpenings(world, clip);

		if (decoRNG.nextBoolean() && !isKeyTower() && this.height > 8) {
			int blobs = 1;
			if (this.size > 9 && decoRNG.nextBoolean()) {
				blobs++;
			}
			for (int i = 0; i < blobs; i++) {
				int x = decoRNG.nextInt(this.size);
				int y = decoRNG.nextInt(this.height - 7) + 2;
				int z = decoRNG.nextInt(this.size);
				destroyTower(world, decoRNG, x, y, z, 3, clip);
			}
		}

		return true;
	}

	protected void destroyTower(World world, Random decoRNG, int x, int y, int z, int amount,
	                           BoundingBox clip) {
		int initialRadius = decoRNG.nextInt(amount) + amount;

		drawBlob(world, x, y, z, initialRadius, 0, 0, clip);

		for (int i = 0; i < 3; i++) {
			int dx = x + (initialRadius - 1) * (decoRNG.nextBoolean() ? 1 : -1);
			int dy = y + (initialRadius - 1) * (decoRNG.nextBoolean() ? 1 : -1);
			int dz = z + (initialRadius - 1) * (decoRNG.nextBoolean() ? 1 : -1);

			netherTransformBlob(world, decoRNG, dx, dy, dz, initialRadius - 1, clip);
			drawBlob(world, dx, dy, dz, initialRadius - 2, 0, 0, clip);
		}
	}

	private void netherTransformBlob(World world, Random inRand, int sx, int sy, int sz, int rad,
	                                 BoundingBox clip) {
		Random rand = new Random(inRand.nextLong());

		for (int dx = 0; dx <= rad; dx++) {
			for (int dy = 0; dy <= rad; dy++) {
				for (int dz = 0; dz <= rad; dz++) {
					if (blobDistance(dx, dy, dz) > rad) {
						continue;
					}

					testAndChangeToNetherrack(world, rand, sx + dx, sy + dy, sz + dz, clip);
					testAndChangeToNetherrack(world, rand, sx + dx, sy + dy, sz + dz, clip);
					testAndChangeToNetherrack(world, rand, sx + dx, sy + dy, sz - dz, clip);
					testAndChangeToNetherrack(world, rand, sx - dx, sy + dy, sz + dz, clip);
					testAndChangeToNetherrack(world, rand, sx - dx, sy + dy, sz - dz, clip);
					testAndChangeToNetherrack(world, rand, sx + dx, sy - dy, sz + dz, clip);
					testAndChangeToNetherrack(world, rand, sx + dx, sy - dy, sz - dz, clip);
					testAndChangeToNetherrack(world, rand, sx - dx, sy - dy, sz + dz, clip);
					testAndChangeToNetherrack(world, rand, sx - dx, sy - dy, sz - dz, clip);
				}
			}
		}
	}

	private void testAndChangeToNetherrack(World world, Random rand, int x, int y, int z,
	                                       BoundingBox clip) {
		if (getBlockIdAt(world, x, y, z, clip) > 0) {
			placeBlock(world, NETHERRACK, 0, x, y, z, clip);
			if (getBlockIdAt(world, x, y + 1, z, clip) == 0 && rand.nextBoolean()) {
				placeBlock(world, FIRE, 0, x, y + 1, z, clip);
			}
		}
	}

	public void drawBlob(World world, int sx, int sy, int sz, int rad, int blockValue, int metaValue,
	                     BoundingBox clip) {
		for (int dx = 0; dx <= rad; dx++) {
			for (int dy = 0; dy <= rad; dy++) {
				for (int dz = 0; dz <= rad; dz++) {
					if (blobDistance(dx, dy, dz) > rad) {
						continue;
					}
					placeBlock(world, blockValue, metaValue, sx + dx, sy + dy, sz + dz, clip);
					placeBlock(world, blockValue, metaValue, sx + dx, sy + dy, sz - dz, clip);
					placeBlock(world, blockValue, metaValue, sx - dx, sy + dy, sz + dz, clip);
					placeBlock(world, blockValue, metaValue, sx - dx, sy + dy, sz - dz, clip);
					placeBlock(world, blockValue, metaValue, sx + dx, sy - dy, sz + dz, clip);
					placeBlock(world, blockValue, metaValue, sx + dx, sy - dy, sz - dz, clip);
					placeBlock(world, blockValue, metaValue, sx - dx, sy - dy, sz + dz, clip);
					placeBlock(world, blockValue, metaValue, sx - dx, sy - dy, sz - dz, clip);
				}
			}
		}
	}

	private static int blobDistance(int dx, int dy, int dz) {
		if (dx >= dy && dx >= dz) {
			return dx + (int) (Math.max(dy, dz) * 0.5 + Math.min(dy, dz) * 0.25);
		}
		if (dy >= dx && dy >= dz) {
			return dy + (int) (Math.max(dx, dz) * 0.5 + Math.min(dx, dz) * 0.25);
		}
		return dz + (int) (Math.max(dx, dy) * 0.5 + Math.min(dx, dy) * 0.25);
	}

	protected void addHalfFloors(World world, Random rand, BoundingBox clip, int bottom, int top) {
		int spacing = 4;
		int rotation = (this.boundingBox.minY + bottom) % 3;

		if (bottom == 0) {
			bottom += spacing;
		}

		for (int y = bottom; y < top; y += spacing) {
			rotation += 2;
			rotation %= 4;

			if (y >= top - spacing) {
				makeFullFloor(world, clip, rotation, y, spacing);
				if (isDeadEnd()) {
					decorateTreasureRoom(world, clip, rotation, y, 4, this.deco);
				}
			} else {
				makeHalfFloor(world, clip, rotation, y, spacing);

				switch (rand.nextInt(8)) {
					case 0:
						if (this.size < 11) {
							decorateReappearingFloor(world, rand, clip, rotation, y);
							break;
						}

					case 1:
						decorateSpawner(world, rand, clip, rotation, y);
						break;
					case 2:
						decorateLounge(world, rand, clip, rotation, y);
						break;
					case 3:
						decorateLibrary(world, rand, clip, rotation, y);
						break;
					case 4:
						decorateExperimentPulser(world, rand, clip, rotation, y);
						break;
					case 5:
						decorateExperimentLamp(world, rand, clip, rotation, y);
						break;
					case 6:
						decoratePuzzleChest(world, rand, clip, rotation, y);
						break;
					default:

						break;
				}
			}

			addStairsDown(world, clip, rotation, y, this.size - 2, spacing);
			if (this.size <= 9) {
				continue;
			}

			addStairsDown(world, clip, rotation, y, this.size - 3, spacing);
		}

		rotation += 2;
		rotation %= 4;
		addStairsDown(world, clip, rotation, this.height - 1, this.size - 2, spacing);
	}

	protected void makeHalfFloor(World world, BoundingBox clip, int rotation, int y, int spacing) {
		fillBlocksRotated(world, clip, this.size / 2, y, 1, this.size - 2, y, this.size - 2,
			this.deco.blockID, this.deco.blockMeta, rotation);
		fillBlocksRotated(world, clip, this.size / 2 - 1, y, 1, this.size / 2 - 1, y, this.size - 2,
			this.deco.accentID, this.deco.accentMeta, rotation);
	}

	protected void makeFullFloor(World world, BoundingBox clip, int rotation, int y, int spacing) {
		fillWithBlocks(world, clip, 1, y, 1, this.size - 2, y, this.size - 2,
			this.deco.blockID, this.deco.blockMeta, 0, 0, false);
		fillWithBlocks(world, clip, this.size / 2, y, 1, this.size / 2, y, this.size - 2,
			this.deco.accentID, this.deco.accentMeta, 0, 0, true);
	}

	protected void decorateTreasureRoom(World world, BoundingBox clip, int rotation, int y,
	                                    int spacing, StructureTFDecorator myDeco) {
		int x = this.size / 2;
		int z = this.size / 2;

		for (int dy = 1; dy < spacing; dy++) {
			placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y + dy, z - 1, rotation, clip);
			placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y + dy, z - 1, rotation, clip);
			placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x - 1, y + dy, z + 1, rotation, clip);
			placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta, x + 1, y + dy, z + 1, rotation, clip);
		}

		placeBlockRotated(world, myDeco.stairID, getStairMeta(1 + rotation), x, y + 1, z - 1, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(rotation), x - 1, y + 1, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(2 + rotation), x + 1, y + 1, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(3 + rotation), x, y + 1, z + 1, rotation, clip);

		for (int dy = 2; dy < spacing - 1; dy++) {
			placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x, y + dy, z - 1, rotation, clip);
			placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x - 1, y + dy, z, rotation, clip);
			placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x + 1, y + dy, z, rotation, clip);
			placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta, x, y + dy, z + 1, rotation, clip);
		}

		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;
		placeBlockRotated(world, myDeco.stairID, getStairMeta(1 + rotation) | flip, x, y + spacing - 1, z - 1, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(rotation) | flip, x - 1, y + spacing - 1, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(2 + rotation) | flip, x + 1, y + spacing - 1, z, rotation, clip);
		placeBlockRotated(world, myDeco.stairID, getStairMeta(3 + rotation) | flip, x, y + spacing - 1, z + 1, rotation, clip);

		placeBlockRotated(world, myDeco.platformID, myDeco.platformMeta, x, y + 1, z, rotation, clip);

		placeTreasure(world, world.rand, x, y + 2, z,
			isKeyTower() ? TFTreasure.DARKTOWER_KEY : TFTreasure.DARKTOWER_CACHE, clip);

		if (isKeyTower()) {
			putItemInTreasure(world, x, y + 2, z, new ItemStack(TFItems.TOWER_KEY), clip);
		}
	}

	private void decorateSpawner(World world, Random rand, BoundingBox clip, int rotation, int y) {
		int x = this.size > 9 ? 4 : 3;
		int z = this.size > 9 ? 5 : 4;

		String mobId = this.size > 9
			? (rand.nextBoolean() ? TOWER_GOLEM : REDSCALE_BROODLING)
			: REDSCALE_BROODLING;

		makePillarFrame(world, clip, this.deco, rotation, x, y, z, true);
		placeSpawnerRotated(world, x + 1, y + 2, z + 1, rotation, mobId, clip);
	}

	private void decorateLounge(World world, Random rand, BoundingBox clip, int rotation, int y) {
		int cx = this.size > 9 ? 9 : 7;
		int cz = this.size > 9 ? 4 : 3;

		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation), cx, y + 1, cz, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation), cx, y + 1, cz + 1, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(1 + rotation), cx, y + 1, cz + 2, rotation, clip);

		cx = this.size > 9 ? 5 : 3;
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;
		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation) | flip, cx, y + 1, cz, rotation, clip);
		placeBlockRotated(world, Blocks.SLAB_PLANKS_OAK.id(), SLAB_UPPER, cx, y + 1, cz + 1, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(1 + rotation) | flip, cx, y + 1, cz + 2, rotation, clip);
	}

	private void decorateReappearingFloor(World world, Random rand, BoundingBox clip, int rotation,
	                                      int y) {
		fillBlocksRotated(world, clip, 4, y, 3, 7, y, 5,
			TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.META_REAPPEARING_INACTIVE, rotation);
		fillBlocksRotated(world, clip, 4, y + 1, 2, 7, y + 1, 2, PRESSURE_PLATE, 0, rotation);
		fillBlocksRotated(world, clip, 4, y + 1, 6, 7, y + 1, 6, PRESSURE_PLATE, 0, rotation);
	}

	private void decorateExperimentLamp(World world, Random rand, BoundingBox clip, int rotation,
	                                    int y) {
		int cx = this.size > 9 ? 5 : 3;
		int cz = this.size > 9 ? 5 : 4;

		placeBlockRotated(world, PISTON_STICKY, 1, cx, y + 1, cz, rotation, clip);
		placeBlockRotated(world, LAMP, 0, cx, y + 2, cz, rotation, clip);

		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, cx, y + 1, cz + 1, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 3), cx, y + 1, cz + 2, rotation, clip);

		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, cx, y + 3, cz - 1, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 2) | BlockLogicLever.MASK_POWERED,
			cx, y + 3, cz - 2, rotation, clip);
	}

	private void decorateExperimentPulser(World world, Random rand, BoundingBox clip, int rotation,
	                                      int y) {
		int cx = this.size > 9 ? 6 : 5;
		int cz = this.size > 9 ? 4 : 3;

		placeBlockRotated(world, PISTON_STICKY, getPistonMeta(3 + rotation), cx, y + 1, cz + 1, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, cx, y + 1, cz, rotation, clip);
		placeBlockRotated(world, REDSTONE_WIRE, 0, cx + 1, y + 1, cz, rotation, clip);
		placeBlockRotated(world, PRESSURE_PLATE, 0, cx + 2, y + 1, cz, rotation, clip);
		placeBlockRotated(world, REPEATER, (rotation + 1) % 4 + 4, cx - 1, y + 1, cz, rotation, clip);
		placeBlockRotated(world, REDSTONE_WIRE, 0, cx - 2, y + 1, cz, rotation, clip);
		placeBlockRotated(world, REDSTONE_WIRE, 0, cx - 2, y + 1, cz + 1, rotation, clip);
		placeBlockRotated(world, REDSTONE_WIRE, 0, cx - 1, y + 1, cz + 1, rotation, clip);
	}

	private void decorateLibrary(World world, Random rand, BoundingBox clip, int rotation, int y) {
		int bx = this.size > 9 ? 4 : 3;
		int bz = this.size > 9 ? 3 : 2;
		makeSmallBookshelf(world, clip, rotation, y, bx, bz);

		bx = this.size > 9 ? 9 : 7;
		bz = this.size > 9 ? 3 : 2;
		makeSmallBookshelf(world, clip, rotation, y, bx, bz);
	}

	protected void makeSmallBookshelf(World world, BoundingBox clip, int rotation, int y,
	                                  int bx, int bz) {
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;

		placeBlockRotated(world, this.deco.stairID, getStairMeta(1 + rotation), bx, y + 1, bz, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(1 + rotation) | flip, bx, y + 2, bz, rotation, clip);

		placeBlockRotated(world, BOOKSHELF, 0, bx, y + 1, bz + 1, rotation, clip);
		placeBlockRotated(world, BOOKSHELF, 0, bx, y + 2, bz + 1, rotation, clip);
		placeBlockRotated(world, BOOKSHELF, 0, bx, y + 1, bz + 2, rotation, clip);
		placeBlockRotated(world, BOOKSHELF, 0, bx, y + 2, bz + 2, rotation, clip);

		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation), bx, y + 1, bz + 3, rotation, clip);
		placeBlockRotated(world, this.deco.stairID, getStairMeta(3 + rotation) | flip, bx, y + 2, bz + 3, rotation, clip);
	}

	private void decoratePuzzleChest(World world, Random rand, BoundingBox clip, int rotation,
	                                 int y) {
		int x = this.size > 9 ? 4 : 3;
		int z = this.size > 9 ? 5 : 4;

		makePillarFrame(world, clip, this.deco, rotation, x, y, z, true);

		placeBlockRotated(world, this.deco.platformID, this.deco.platformMeta, x + 1, y + 1, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 2, y + 1, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x, y + 1, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 1, z + 2, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 1, z, rotation, clip);

		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 2, y + 3, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x, y + 3, z + 1, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 3, z + 2, rotation, clip);
		placeBlockRotated(world, 0, 0, x + 1, y + 3, z, rotation, clip);
		placeBlockRotated(world, this.deco.blockID, this.deco.blockMeta, x + 1, y + 3, z + 1, rotation, clip);

		placeBlockRotated(world, PISTON, getPistonMeta(1 + rotation), x + 1, y + 3, z - 1, rotation, clip);
		placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, x + 1, y + 3, z - 2, rotation, clip);
		placeBlockRotated(world, LEVER, getLeverMeta(rotation, 5), x + 2, y + 3, z - 2, rotation, clip);

		placeTreasureRotated(world, world.rand, x + 1, y + 2, z + 1, rotation,
			TFTreasure.DARKTOWER_CACHE, clip);
	}

	protected void makePillarFrame(World world, BoundingBox clip, StructureTFDecorator myDeco,
	                               int rotation, int x, int y, int z, boolean fenced) {
		makePillarFrame(world, clip, myDeco, rotation, x, y, z, 3, 3, 3, fenced);
	}

	protected void makePillarFrame(World world, BoundingBox clip, StructureTFDecorator myDeco,
	                               int rotation, int x, int y, int z,
	                               int width, int height, int length, boolean fenced) {
		int flip = BlockLogicStairs.MASK_ROTATION_VERTICAL;

		for (int dx = 0; dx < width; dx++) {
			for (int dz = 0; dz < length; dz++) {
				boolean pillarX = dx % 3 == 0 || dx == width - 1;
				boolean pillarZ = dz % 3 == 0 || dz == length - 1;

				if (pillarX && pillarZ) {
					for (int py = 1; py <= height; py++) {
						placeBlockRotated(world, myDeco.pillarID, myDeco.pillarMeta,
							x + dx, y + py, z + dz, rotation, clip);
					}
					continue;
				}

				if (dx == 0) {
					placeBlockRotated(world, myDeco.stairID, getStairMeta(rotation), x + dx, y + 1, z + dz, rotation, clip);
					placeBlockRotated(world, myDeco.stairID, getStairMeta(rotation) | flip, x + dx, y + height, z + dz, rotation, clip);
				} else if (dx == width - 1) {
					placeBlockRotated(world, myDeco.stairID, getStairMeta(2 + rotation), x + dx, y + 1, z + dz, rotation, clip);
					placeBlockRotated(world, myDeco.stairID, getStairMeta(2 + rotation) | flip, x + dx, y + height, z + dz, rotation, clip);
				} else if (dz == 0) {
					placeBlockRotated(world, myDeco.stairID, getStairMeta(1 + rotation), x + dx, y + 1, z + dz, rotation, clip);
					placeBlockRotated(world, myDeco.stairID, getStairMeta(1 + rotation) | flip, x + dx, y + height, z + dz, rotation, clip);
				} else if (dz == length - 1) {
					placeBlockRotated(world, myDeco.stairID, getStairMeta(3 + rotation), x + dx, y + 1, z + dz, rotation, clip);
					placeBlockRotated(world, myDeco.stairID, getStairMeta(3 + rotation) | flip, x + dx, y + height, z + dz, rotation, clip);
				}

				if (!fenced || (dx != 0 && dx != width - 1 && dz != 0 && dz != length - 1)) {
					continue;
				}
				for (int fy = 2; fy <= height - 1; fy++) {
					placeBlockRotated(world, myDeco.fenceID, myDeco.fenceMeta,
						x + dx, y + fy, z + dz, rotation, clip);
				}
			}
		}
	}

	protected void putItemInTreasure(World world, int x, int y, int z, ItemStack itemToAdd,
	                                 BoundingBox clip) {
		int dx = getXWithOffset(x, z);
		int dy = getYWithOffset(y);
		int dz = getZWithOffset(x, z);

		if (!clip.contains(dx, dy, dz)) {
			return;
		}

		TileEntity tileEntity = world.getTileEntity(new TilePos(dx, dy, dz));
		if (!(tileEntity instanceof Container inventory)) {
			return;
		}

		boolean alreadyPresent = false;
		int emptySlots = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack inSlot = inventory.getItem(i);
			if (inSlot == null) {
				emptySlots++;
			} else if (ItemStack.areItemStacksEqual(inSlot, itemToAdd)) {
				alreadyPresent = true;
				break;
			}
		}

		if (alreadyPresent || emptySlots == 0) {
			return;
		}

		int slotsUntilPlaced = world.rand.nextInt(emptySlots);
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (inventory.getItem(i) != null) {
				continue;
			}
			if (slotsUntilPlaced == 0) {
				inventory.setItem(i, itemToAdd);
				break;
			}
			slotsUntilPlaced--;
		}
	}

	protected void addStairsDown(World world, BoundingBox clip, int rotation, int y, int sz,
	                             int spacing) {
		for (int i = 0; i < spacing; i++) {
			int sx = this.size - 3 - i;

			placeBlockRotated(world, this.deco.stairID, getStairMeta(rotation), sx, y - i, sz, rotation, clip);
			placeBlockRotated(world, this.deco.accentID, this.deco.accentMeta, sx, y - 1 - i, sz, rotation, clip);

			placeBlockRotated(world, 0, 0, sx, y + 1 - i, sz, rotation, clip);
			placeBlockRotated(world, 0, 0, sx, y + 2 - i, sz, rotation, clip);
			placeBlockRotated(world, 0, 0, sx - 1, y + 2 - i, sz, rotation, clip);
			placeBlockRotated(world, 0, 0, sx, y + 3 - i, sz, rotation, clip);
			placeBlockRotated(world, 0, 0, sx - 1, y + 3 - i, sz, rotation, clip);
		}
	}

	protected void addSmallTimberBeams(World world, Random rand, BoundingBox clip, int bottom,
	                                   int top) {
		int spacing = 4;
		int rotation = 0;

		if (bottom == 0) {
			bottom += spacing;
		}

		for (int y = bottom; y < top; y += spacing) {
			rotation++;
			rotation %= 4;

			if (y >= top - spacing && isDeadEnd()) {
				makeTimberFloor(world, rand, clip, rotation, y, spacing);

				StructureDecoratorDarkTower logDeco = new StructureDecoratorDarkTower();
				logDeco.pillarID = beamBlock();
				logDeco.pillarMeta = LOG_AXIS_Y;
				logDeco.platformID = beamBlock();
				logDeco.pillarMeta = LOG_AXIS_Y;

				decorateTreasureRoom(world, clip, rotation, y, 4, logDeco);
				continue;
			}

			makeSmallTimberBeams(world, rand, clip, rotation, y,
				y == bottom && bottom != spacing, y >= top - spacing);
		}
	}

	protected void makeTimberFloor(World world, Random rand, BoundingBox clip, int rotation, int y,
	                               int spacing) {
		int beamId = beamBlock();
		int beamMetaNS = (this.coordBaseMode + rotation) % 2 == 0 ? LOG_AXIS_X : LOG_AXIS_Z;
		int beamMetaEW = beamMetaNS == LOG_AXIS_X ? LOG_AXIS_Z : LOG_AXIS_X;

		for (int z = 1; z < this.size - 1; z++) {
			for (int x = 1; x < this.size - 1; x++) {
				placeBlockRotated(world, beamId, x < z ? beamMetaNS : beamMetaEW,
					x, y, z, rotation, clip);
			}
		}

		for (int by = 1; by < 4; by++) {
			placeBlockRotated(world, beamId, LOG_AXIS_Y, 2, y - by, 2, rotation, clip);
			placeBlockRotated(world, LADDER, getLadderMeta(2 + rotation), 3, y - by, 2, rotation, clip);
			placeBlockRotated(world, beamId, LOG_AXIS_Y, 6, y - by, 6, rotation, clip);
			placeBlockRotated(world, LADDER, getLadderMeta(4 + rotation), 5, y - by, 6, rotation, clip);
		}

		placeBlockRotated(world, 0, 0, 3, y, 2, rotation, clip);
		placeBlockRotated(world, 0, 0, 5, y, 6, rotation, clip);
	}

	protected void makeSmallTimberBeams(World world, Random rand, BoundingBox clip, int rotation,
	                                    int y, boolean bottom, boolean top) {
		int beamId = beamBlock();
		int beamMetaNS = (this.coordBaseMode + rotation) % 2 == 0 ? LOG_AXIS_X : LOG_AXIS_Z;
		int beamMetaEW = beamMetaNS == LOG_AXIS_X ? LOG_AXIS_Z : LOG_AXIS_X;

		for (int z = 1; z < this.size - 1; z++) {
			placeBlockRotated(world, beamId, beamMetaEW, 2, y, z, rotation, clip);
			placeBlockRotated(world, beamId, beamMetaEW, 6, y, z, rotation, clip);
		}

		int crossZ = pickBetweenExcluding(3, this.size - 3, rand, 2, 2, 6);
		for (int x = 3; x < 6; x++) {
			placeBlockRotated(world, beamId, beamMetaNS, x, y, crossZ, rotation, clip);
		}

		int x1 = 2;
		int z1 = rand.nextBoolean() ? 2 : 6;
		int x3 = 6;
		int z3 = rand.nextBoolean() ? 2 : 6;

		for (int by = 1; by < 4; by++) {
			if (!bottom || checkPost(world, x1, y - 4, z1, rotation, clip)) {
				placeBlockRotated(world, beamId, LOG_AXIS_Y, x1, y - by, z1, rotation, clip);
				placeBlockRotated(world, LADDER, getLadderMeta(2 + rotation), x1 + 1, y - by, z1, rotation, clip);
			}
			if (bottom && !checkPost(world, x3, y - 4, z3, rotation, clip)) {
				continue;
			}
			placeBlockRotated(world, beamId, LOG_AXIS_Y, x3, y - by, z3, rotation, clip);
			placeBlockRotated(world, LADDER, getLadderMeta(4 + rotation), x3 - 1, y - by, z3, rotation, clip);
		}
	}

	protected int pickBetweenExcluding(int low, int high, Random rand, int k, int l, int m) {
		int result;
		do {
			result = rand.nextInt(high - low) + low;
		} while (result == k || result == l || result == m);
		return result;
	}

	protected int pickFrom(Random rand, int i, int j, int k) {
		return switch (rand.nextInt(3)) {
			case 1 -> j;
			case 2 -> k;
			default -> i;
		};
	}

	protected boolean checkPost(World world, int x, int y, int z, int rotation, BoundingBox clip) {
		int worldX = getXWithOffsetAsIfRotated(x, z, rotation);
		int worldY = getYWithOffset(y);
		int worldZ = getZWithOffsetAsIfRotated(x, z, rotation);

		int blockId = clip.contains(worldX, worldY, worldZ)
			? world.getBlockId(worldX, worldY, worldZ) : 0;

		return blockId != 0
			&& (blockId != this.deco.accentID
			|| world.getBlockMetadata(worldX, worldY, worldZ) != this.deco.accentMeta);
	}

	protected void makeEncasedWalls(World world, Random rand, BoundingBox clip,
	                                int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {

					if (x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ) {
						continue;
					}

					boolean face =
						(x != minY && x != maxX || y != minY && y != maxY && z != minZ && z != maxZ)
							&& (y != minY && y != maxY || x != minY && x != maxX && z != minZ && z != maxZ)
							&& (z != minZ && z != maxZ || x != minY && x != maxX && y != minY && y != maxY);

					if (!face) {
						placeBlock(world, this.deco.accentID, this.deco.accentMeta, x, y, z, clip);
						continue;
					}

					BlockSelector blocker = getTowerWoods();
					blocker.select(rand, x, y, z, true);
					placeBlock(world, blocker.blockId, blocker.meta, x, y, z, clip);
				}
			}
		}

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY + 1, minZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY + 1, maxZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY + 1, minZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY + 1, maxZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY - 1, minZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY - 1, maxZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY - 1, minZ, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY - 1, maxZ, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX, minY + 1, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX, minY + 1, maxZ - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX, minY + 1, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX, minY + 1, maxZ - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX, maxY - 1, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX, maxY - 1, maxZ - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX, maxY - 1, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX, maxY - 1, maxZ - 1, clip);

		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, minY, maxZ - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, minY, maxZ - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, minX + 1, maxY, maxZ - 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY, minZ + 1, clip);
		placeBlock(world, this.deco.accentID, this.deco.accentMeta, maxX - 1, maxY, maxZ - 1, clip);
	}

	protected void fillBlocksDownwards(World world, int blockId, int meta, int x, int y, int z,
	                                   BoundingBox clip) {
		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);

		if (!clip.contains(wx, wy, wz)) {
			return;
		}

		while ((world.isAirBlock(wx, wy, wz) || world.getBlockMaterial(wx, wy, wz).isLiquid())
			&& wy > 1) {
			world.setBlockAndMetadataRaw(wx, wy, wz, blockId, meta);
			wy--;
		}
	}

	@Override
	public int[] getValidOpening(Random rand, int direction) {
		int verticalOffset = this.size == 19 ? 5 : 4;

		if (direction == 0 || direction == 2) {
			int rx = direction == 0 ? this.size - 1 : 0;
			int rz = this.size / 2;
			return new int[]{rx, this.height - verticalOffset, rz};
		}
		if (direction == 1 || direction == 3) {
			int rx = this.size / 2;
			int rz = direction == 1 ? this.size - 1 : 0;
			return new int[]{rx, this.height - verticalOffset, rz};
		}
		return new int[]{0, 0, 0};
	}

	@Override
	public void addOpening(int dx, int dy, int dz, int direction) {
		addOpening(dx, dy, dz, direction, EnumDarkTowerDoor.VANISHING);
	}

	protected void addOpening(int dx, int dy, int dz, int direction, EnumDarkTowerDoor type) {
		super.addOpening(dx, dy, dz, direction);
		this.openingTypes.add(indexOfOpening(dx, dy, dz), type);
	}

	private int indexOfOpening(int dx, int dy, int dz) {
		for (int i = 0; i < this.openings.size(); i++) {
			int[] opening = this.openings.get(i);
			if (opening[0] == dx && opening[1] == dy && opening[2] == dz) {
				return i;
			}
		}

		return this.openingTypes.size();
	}

	@Override
	protected void makeOpenings(World world, BoundingBox clip) {
		for (int i = 0; i < this.openings.size(); i++) {
			int[] door = this.openings.get(i);
			switch (this.openingTypes.get(i)) {
				case REAPPEARING -> makeReappearingDoorOpening(world, door[0], door[1], door[2], clip);
				case LOCKED -> makeLockedDoorOpening(world, door[0], door[1], door[2], clip);
				default -> makeDoorOpening(world, door[0], door[1], door[2], clip);
			}
		}
	}

	@Override
	protected void makeDoorOpening(World world, int dx, int dy, int dz, BoundingBox clip) {
		nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);

		if (dx == 0 || dx == this.size - 1) {
			fillWithBlocks(world, clip, dx, dy - 1, dz - 2, dx, dy + 3, dz + 2,
				this.deco.accentID, this.deco.accentMeta, 0, 0, false);
			fillWithBlocks(world, clip, dx, dy, dz - 1, dx, dy + 2, dz + 1,
				TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.META_VANISH_INACTIVE, 0, 0, false);
		}
		if (dz == 0 || dz == this.size - 1) {
			fillWithBlocks(world, clip, dx - 2, dy - 1, dz, dx + 2, dy + 3, dz,
				this.deco.accentID, this.deco.accentMeta, 0, 0, false);
			fillWithBlocks(world, clip, dx - 1, dy, dz, dx + 1, dy + 2, dz,
				TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.META_VANISH_INACTIVE, 0, 0, false);
		}
	}

	protected void makeReappearingDoorOpening(World world, int dx, int dy, int dz, BoundingBox clip) {
		nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);

		if (dx == 0 || dx == this.size - 1) {
			fillWithBlocks(world, clip, dx, dy - 1, dz - 2, dx, dy + 3, dz + 2,
				this.deco.accentID, this.deco.accentMeta, 0, 0, false);
			fillWithBlocks(world, clip, dx, dy, dz - 1, dx, dy + 2, dz + 1,
				TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.META_REAPPEARING_INACTIVE, 0, 0, false);
		}
		if (dz == 0 || dz == this.size - 1) {
			fillWithBlocks(world, clip, dx - 2, dy - 1, dz, dx + 2, dy + 3, dz,
				this.deco.accentID, this.deco.accentMeta, 0, 0, false);
			fillWithBlocks(world, clip, dx - 1, dy, dz, dx + 1, dy + 2, dz,
				TFBlocks.TOWER_DEVICE.id(), BlockLogicTFTowerDevice.META_REAPPEARING_INACTIVE, 0, 0, false);
		}
	}

	protected void makeLockedDoorOpening(World world, int dx, int dy, int dz, BoundingBox clip) {
		nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);

		int device = TFBlocks.TOWER_DEVICE.id();

		if (dx == 0 || dx == this.size - 1) {
			fillWithBlocks(world, clip, dx, dy - 1, dz - 2, dx, dy + 3, dz + 2,
				this.deco.accentID, this.deco.accentMeta, 0, 0, false);
			fillWithBlocks(world, clip, dx, dy, dz - 1, dx, dy + 2, dz + 1,
				device, BlockLogicTFTowerDevice.META_VANISH_INACTIVE, 0, 0, false);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx, dy, dz + 1, clip);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx, dy, dz - 1, clip);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx, dy + 2, dz + 1, clip);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx, dy + 2, dz - 1, clip);
		}
		if (dz == 0 || dz == this.size - 1) {
			fillWithBlocks(world, clip, dx - 2, dy - 1, dz, dx + 2, dy + 3, dz,
				this.deco.accentID, this.deco.accentMeta, 0, 0, false);
			fillWithBlocks(world, clip, dx - 1, dy, dz, dx + 1, dy + 2, dz,
				device, BlockLogicTFTowerDevice.META_VANISH_INACTIVE, 0, 0, false);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx + 1, dy, dz, clip);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx - 1, dy, dz, clip);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx + 1, dy + 2, dz, clip);
			placeBlock(world, device, BlockLogicTFTowerDevice.META_VANISH_LOCKED, dx - 1, dy + 2, dz, clip);
		}
	}

	@Override
	public boolean isDeadEnd() {
		int nonBalconies = 0;
		for (EnumDarkTowerDoor type : this.openingTypes) {
			if (type != EnumDarkTowerDoor.REAPPEARING) {
				nonBalconies++;
			}
		}
		return nonBalconies <= 1;
	}

	public boolean isKeyTower() {
		return this.keyTower;
	}

	public void setKeyTower(boolean keyTower) {
		this.keyTower = keyTower;
	}

	protected int getLeverMeta(int rotation, int direction) {
		if (direction == 0) {
			return BlockLogicLever.ROTATION_BOTTOM_NS;
		}
		if (direction == 1) {
			return BlockLogicLever.ROTATION_TOP_NS;
		}

		rotation += getCoordBaseMode();
		rotation %= 4;

		return switch (rotation) {
			case 0 -> switch (direction) {
				case 2 -> BlockLogicLever.ROTATION_NORTH;
				case 3 -> BlockLogicLever.ROTATION_SOUTH;
				case 4 -> BlockLogicLever.ROTATION_WEST;
				case 5 -> BlockLogicLever.ROTATION_EAST;
				default -> -1;
			};
			case 1 -> switch (direction) {
				case 2 -> BlockLogicLever.ROTATION_EAST;
				case 3 -> BlockLogicLever.ROTATION_WEST;
				case 4 -> BlockLogicLever.ROTATION_NORTH;
				case 5 -> BlockLogicLever.ROTATION_SOUTH;
				default -> -1;
			};
			case 2 -> switch (direction) {
				case 2 -> BlockLogicLever.ROTATION_SOUTH;
				case 3 -> BlockLogicLever.ROTATION_NORTH;
				case 4 -> BlockLogicLever.ROTATION_EAST;
				case 5 -> BlockLogicLever.ROTATION_WEST;
				default -> -1;
			};
			case 3 -> switch (direction) {
				case 2 -> BlockLogicLever.ROTATION_WEST;
				case 3 -> BlockLogicLever.ROTATION_EAST;
				case 4 -> BlockLogicLever.ROTATION_SOUTH;
				case 5 -> BlockLogicLever.ROTATION_NORTH;
				default -> -1;
			};
			default -> -1;
		};
	}

	protected int getPistonMeta(int dir) {
		return switch ((this.coordBaseMode + dir) % 4) {
			case 0 -> 5;
			case 1 -> 3;
			case 2 -> 4;
			case 3 -> 2;
			default -> -1;
		};
	}

	protected void makeNetherburst(World world, Random inRand, int radius, int iterations, int moves,
	                               int sx, int y, int sz, int rotation, BoundingBox clip) {
		Random rand = new Random(inRand.nextLong());

		for (int i = 0; i < iterations; i++) {
			int lx = sx;
			int ly = y;
			int lz = sz;

			for (int move = 0; move < moves; move++) {
				placeBlockRotated(world, 0, 0, lx, ly, lz, rotation, clip);

				for (int facing = 0; facing < 6; facing++) {

					int fx = lx + OFFSET_X[facing];
					int fy = ly + OFFSET_X[facing];
					int fz = lz + OFFSET_X[facing];

					if (getBlockIdRotated(world, fx, fy, fz, rotation, clip) <= 0
						|| rand.nextInt(4) != 0) {
						continue;
					}
					placeBlockRotated(world, NETHERRACK, 0, fx, fy, fz, rotation, clip);
					if (getBlockIdRotated(world, fx, fy + 1, fz, rotation, clip) != 0
						|| rand.nextInt(2) != 0) {
						continue;
					}
					placeBlockRotated(world, FIRE, 0, fx, fy + 1, fz, rotation, clip);
				}

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
	}

	protected static final int[] OFFSET_X = {0, 0, 0, 0, -1, 1};
	protected static final int[] OFFSET_Y = {-1, 1, 0, 0, 0, 0};
	protected static final int[] OFFSET_Z = {0, 0, -1, 1, 0, 0};
}
