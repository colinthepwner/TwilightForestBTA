package com.twilightforest.world.structure;

import com.twilightforest.TwilightForest;
import com.twilightforest.world.feature.WorldFeatureTFBigMushroom;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.block.BlockLogicSaplingBase;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComponentTFTowerWing extends StructureComponentTF {

	protected static final int STONE_BRICK = Blocks.BRICK_STONE_POLISHED.id();
	protected static final int STONE_BRICK_MOSSY = Blocks.BRICK_STONE_POLISHED_MOSSY.id();

	protected static final int SLAB_WOOD = Blocks.SLAB_PLANKS_OAK.id();
	protected static final int SLAB_STONE_BRICK = Blocks.SLAB_BRICK_STONE_POLISHED.id();
	protected static final int SLAB_STONE = Blocks.SLAB_STONE_POLISHED.id();

	protected static final int SLAB_LOWER = 0;
	protected static final int SLAB_DOUBLE = 1;

	protected static final int STAIRS_WOOD = Blocks.STAIRS_PLANKS_OAK.id();
	protected static final int PLANKS = Blocks.PLANKS_OAK.id();
	protected static final int LADDER = Blocks.LADDER_OAK.id();
	protected static final int TORCH = Blocks.TORCH_COAL.id();
	protected static final int FENCE = Blocks.FENCE_PLANKS_OAK.id();
	protected static final int BARS = Blocks.FENCE_STEEL.id();
	protected static final int GLASS = Blocks.GLASS.id();
	protected static final int WEB = Blocks.COBWEB.id();
	protected static final int BOOKSHELF = Blocks.BOOKSHELF_PLANKS_OAK.id();
	protected static final int STONE = Blocks.STONE.id();
	protected static final int MOSSY_COBBLE = Blocks.COBBLE_STONE_MOSSY.id();

	protected static final int[] STAIR_META = {1, 0, 3, 2};

	private static final String HEDGE_SPIDER = TwilightForest.MOD_ID + ":hedgespider";
	private static final String SWARM_SPIDER = TwilightForest.MOD_ID + ":swarmspider";
	private static final String CAVE_SPIDER_STANDIN = "minecraft:scorpion";

	public int size;

	public int height;

	protected Class<?> roofType;

	protected final List<int[]> openings = new ArrayList<>();

	protected int highestOpening;

	protected final boolean[] openingTowards = {false, false, true, false};

	protected ComponentTFTowerWing(int componentType, int x, int y, int z,
	                               int size, int height, int direction) {
		super(componentType);
		this.size = size;
		this.height = height;
		this.coordBaseMode = direction;
		this.highestOpening = 0;
		this.boundingBox = componentBox(x, y, z, 0, 0, 0,
			size - 1, height - 1, size - 1, direction);
	}

	@Override
	public int featureType() {
		return com.twilightforest.world.feature.TFFeature.LICH_TOWER;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		addOpening(0, 1, this.size / 2, 2);
		makeARoof(parent, pieces, rand);
		makeABeard(parent, pieces, rand);

		if (this.size > 4) {
			for (int i = 0; i < 4; i++) {

				if (i == 2) {
					continue;
				}
				int[] dest = getValidOpening(rand, i);
				if (!makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2],
					this.size - 2, this.height - 6, i) && this.size > 8) {
					makeTowerWing(pieces, rand, 1, dest[0], dest[1], dest[2],
						this.size - 6, this.height - 18, i);
				}
			}
		}
	}

	public boolean makeTowerWing(List<StructureComponentTF> pieces, Random rand, int index,
	                             int x, int y, int z, int wingSize, int wingHeight, int rotation) {
		if (wingHeight < 6) {
			return false;
		}

		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, wingSize, direction);

		if (rand.nextInt(6) == 0) {
			return makeBridge(pieces, rand, index, x, y, z, wingSize, wingHeight, rotation);
		}

		ComponentTFTowerWing wing =
			new ComponentTFTowerWing(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
		StructureComponentTF hit = findIntersecting(pieces, wing.boundingBox);
		if (hit != null && hit != this) {
			return false;
		}

		pieces.add(wing);
		wing.buildComponent(this, pieces, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	protected boolean makeBridge(List<StructureComponentTF> pieces, Random rand, int index,
	                            int x, int y, int z, int wingSize, int wingHeight, int rotation) {
		int direction = (this.coordBaseMode + rotation) % 4;
		int[] dx = offsetTowerCoords(x, y, z, 3, direction);

		ComponentTFTowerBridge bridge =
			new ComponentTFTowerBridge(index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);

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

	protected int[] offsetTowerCoords(int x, int y, int z, int towerSize, int direction) {
		int dx = getXWithOffset(x, z);
		int dy = getYWithOffset(y);
		int dz = getZWithOffset(x, z);

		return switch (direction) {
			case 0 -> new int[]{dx + 1, dy - 1, dz - towerSize / 2};
			case 1 -> new int[]{dx + towerSize / 2, dy - 1, dz + 1};
			case 2 -> new int[]{dx - 1, dy - 1, dz + towerSize / 2};
			case 3 -> new int[]{dx - towerSize / 2, dy - 1, dz - 1};
			default -> new int[]{x, y, z};
		};
	}

	public void addOpening(int dx, int dy, int dz, int direction) {
		this.openingTowards[direction] = true;
		if (dy > this.highestOpening) {
			this.highestOpening = dy;
		}
		this.openings.add(new int[]{dx, dy, dz});
	}

	public void makeABeard(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                       Random rand) {
		ComponentTFTowerBeard beard = new ComponentTFTowerBeard(componentType() + 1, this);
		pieces.add(beard);
		beard.buildComponent(this, pieces, rand);
	}

	public void makeARoof(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                      Random rand) {
		if (parent.boundingBox.maxY > this.boundingBox.maxY) {
			makeAttachedRoof(pieces, rand);
		} else {
			makeFreestandingRoof(pieces, rand);
		}
	}

	protected void makeAttachedRoof(List<StructureComponentTF> pieces, Random rand) {
		int index = componentType();

		if (this.roofType == null && rand.nextInt(32) != 0) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofGableForwards(index + 1, this));
		}
		if (this.roofType == null && rand.nextInt(8) != 0) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofSlabForwards(index + 1, this));
		}
		if (this.roofType == null && rand.nextInt(32) != 0) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofAttachedSlab(index + 1, this));
		}
		if (this.roofType == null) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofFence(index + 1, this));
		}
	}

	protected void makeFreestandingRoof(List<StructureComponentTF> pieces, Random rand) {
		int index = componentType();

		if (this.roofType == null && rand.nextInt(8) != 0) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofPointyOverhang(index + 1, this));
		}
		if (this.roofType == null) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofStairsOverhang(index + 1, this));
		}
		if (this.roofType == null) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofStairs(index + 1, this));
		}

		if (this.roofType == null && rand.nextInt(53) != 0) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofSlab(index + 1, this));
		}
		if (this.roofType == null) {
			tryToFitRoof(pieces, rand, new ComponentTFTowerRoofFence(index + 1, this));
		}
	}

	protected void tryToFitRoof(List<StructureComponentTF> pieces, Random rand,
	                            ComponentTFTowerRoof roof) {
		if (roof.fits(this, pieces, rand)) {
			pieces.add(roof);
			roof.buildComponent(this, pieces, rand);
			this.roofType = roof.getClass();
		}
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		fillWithRandomizedBlocks(world, clip, 0, 0, 0,
			this.size - 1, this.height - 1, this.size - 1, false, rand, TOWER_STONE);

		if (this.highestOpening > 1) {
			makeStairs(world, rand, clip);
		}

		decorateThisTower(world, rand, clip);
		makeWindows(world, rand, clip);
		makeOpenings(world, clip);
		return true;
	}

	protected static final BlockSelector TOWER_STONE = new BlockSelector() {
		@Override
		public void select(Random rand, int x, int y, int z, boolean shell) {
			if (!shell) {
				this.blockId = 0;
				this.meta = 0;
				return;
			}

			this.meta = 0;
			float f = rand.nextFloat();
			if (f < 0.2F) {
				this.blockId = STONE_BRICK;
			} else if (f < 0.5F) {
				this.blockId = STONE_BRICK_MOSSY;
			} else if (f < 0.55F) {
				this.blockId = STONE_BRICK;
			} else {
				this.blockId = STONE_BRICK;
			}
		}
	};

	protected void decorateThisTower(World world, Random rand, BoundingBox clip) {
		Random decoRNG = new Random(world.getRandomSeed()
			+ (long) this.boundingBox.minX * 321534781L * this.boundingBox.minZ * 756839L);

		if (this.size > 3) {
			if (isDeadEnd()) {
				decorateDeadEnd(world, decoRNG, clip);
			} else {
				decorateStairTower(world, decoRNG, clip);
			}
		}
	}

	public boolean isDeadEnd() {
		return this.openings.size() == 1;
	}

	public boolean hasExitsOnAllWalls() {
		int exits = 0;
		for (int i = 0; i < 4; i++) {
			exits += this.openingTowards[i] ? 1 : 0;
		}
		return exits == 4;
	}

	public boolean hasStairs() {
		return this.highestOpening > 1;
	}

	protected void decorateDeadEnd(World world, Random rand, BoundingBox clip) {
		int floors = (this.height - 1) / 5;
		int floorHeight = this.height / floors;

		for (int i = 1; i < floors; i++) {
			for (int x = 1; x < this.size - 1; x++) {
				for (int z = 1; z < this.size - 1; z++) {
					placeBlock(world, PLANKS, 0, x, i * floorHeight, z, clip);
				}
			}
		}

		if (floors > 1) {
			int ladderDir = 3;
			int downLadderDir;
			decorateFloor(world, rand, 0, 1, floorHeight, ladderDir, -1, clip);

			for (int i = 1; i < floors - 1; i++) {
				int bottom = 1 + floorHeight * i;
				int top = floorHeight * (i + 1);
				downLadderDir = ladderDir++;
				ladderDir %= 4;
				decorateFloor(world, rand, i, bottom, top, ladderDir, downLadderDir, clip);
			}

			decorateFloor(world, rand, floors, 1 + floorHeight * (floors - 1), this.height - 1,
				-1, ladderDir, clip);
		} else {
			decorateFloor(world, rand, 0, 1, this.height - 1, -1, -1, clip);
		}
	}

	protected void decorateFloor(World world, Random rand, int floor, int bottom, int top,
	                             int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		if (ladderUpDir > -1) {
			int meta = getLadderMeta(ladderUpDir);
			int dx = getLadderX(ladderUpDir);
			int dz = getLadderZ(ladderUpDir);
			for (int dy = bottom; dy < top; dy++) {
				placeBlock(world, LADDER, meta, dx, dy, dz, clip);
			}
		}

		if (ladderDownDir > -1) {
			int meta = getLadderMeta(ladderDownDir);
			int dx = getLadderX(ladderDownDir);
			int dz = getLadderZ(ladderDownDir);

			for (int dy = bottom - 1; dy < bottom + 2; dy++) {
				placeBlock(world, LADDER, meta, dx, dy, dz, clip);
			}
		}

		if (rand.nextInt(7) == 0 && ladderDownDir == -1) {
			decorateWell(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(7) == 0 && ladderDownDir == -1) {
			decorateSkeletonRoom(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(6) == 0 && ladderDownDir == -1) {
			decorateZombieRoom(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(5) == 0 && ladderDownDir == -1) {
			decorateCactusRoom(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(4) == 0 && ladderDownDir > -1) {
			decorateTreasureChest(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(5) == 0) {
			decorateSpiderWebs(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(12) == 0 && ladderDownDir > -1) {
			decorateSolidRock(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else if (rand.nextInt(3) == 0) {
			decorateFullLibrary(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		} else {
			decorateLibrary(world, rand, bottom, top, ladderUpDir, ladderDownDir, clip);
		}
	}

	protected void decorateWell(World world, Random rand, int bottom, int top,
	                            int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		int cx = this.size / 2;
		int fluid = rand.nextInt(4) == 0
			? Blocks.FLUID_LAVA_STILL.id() : Blocks.FLUID_WATER_STILL.id();

		if (this.size > 5) {
			placeBlock(world, STONE_BRICK, 0, cx - 1, bottom, cx - 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx - 1, bottom + 1, cx - 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx, bottom, cx - 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx + 1, bottom, cx - 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx + 1, bottom + 1, cx - 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx - 1, bottom, cx, clip);
			placeBlock(world, fluid, 0, cx, bottom, cx, clip);
			placeBlock(world, STONE_BRICK, 0, cx + 1, bottom, cx, clip);
			placeBlock(world, STONE_BRICK, 0, cx - 1, bottom, cx + 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx - 1, bottom + 1, cx + 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx, bottom, cx + 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx + 1, bottom, cx + 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx + 1, bottom + 1, cx + 1, clip);
		}

		placeBlock(world, fluid, 0, cx, bottom - 1, cx, clip);
	}

	protected void decorateSkeletonRoom(World world, Random rand, int bottom, int top,
	                                    int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		placeSpawner(world, rand, this.size / 2, bottom + 2, this.size / 2,
			"minecraft:skeleton", clip);

		List<int[]> chains = new ArrayList<>();
		chains.add(new int[]{this.size / 2, bottom + 2, this.size / 2});

		for (int i = 0; i < this.size + 2; i++) {
			int[] chain = {2 + rand.nextInt(this.size - 4), this.height - 2,
				2 + rand.nextInt(this.size - 4)};
			if (!chainCollides(chain, chains)) {
				for (int dy = bottom; dy < top; dy++) {
					placeBlock(world, BARS, 0, chain[0], dy, chain[2], clip);
				}
				chains.add(chain);
			}
		}

		for (int dx = 1; dx <= this.size - 2; dx++) {
			for (int dz = 1; dz <= this.size - 2; dz++) {
				if ((dx == 1 || dx == this.size - 2 || dz == 1 || dz == this.size - 2)
					&& !isWindowPos(dx, dz)
					&& !isLadderPos(dx, dz, ladderUpDir, ladderDownDir)) {
					placeBlock(world, WEB, 0, dx, top - 1, dz, clip);
				}
			}
		}
	}

	protected void decorateZombieRoom(World world, Random rand, int bottom, int top,
	                                  int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		placeSpawner(world, rand, this.size / 2, bottom + 2, this.size / 2,
			"minecraft:zombie", clip);

		for (int dx = 1; dx <= this.size - 2; dx++) {
			for (int dz = 1; dz <= this.size - 2; dz++) {
				if (!isWindowPos(dx, dz) && !isLadderPos(dx, dz, ladderUpDir, ladderDownDir)
					&& rand.nextInt(5) == 0) {
					placeBlock(world, Blocks.MUSHROOM_BROWN.id(), 0, dx, bottom, dz, clip);
				}
			}
		}

		List<int[]> slabs = new ArrayList<>();
		slabs.add(new int[]{this.size / 2, bottom + 2, this.size / 2});

		for (int i = 0; i < this.size - 1; i++) {
			int[] slab = {2 + rand.nextInt(this.size - 4), this.height - 2,
				2 + rand.nextInt(this.size - 4)};
			if (!chainCollides(slab, slabs)) {
				placeBlock(world, BARS, 0, slab[0], bottom, slab[2], clip);
				placeBlock(world, SLAB_WOOD, SLAB_LOWER, slab[0], bottom + 1, slab[2], clip);
				placeBlock(world, Blocks.SOULSAND.id(), 0, slab[0], bottom + 2, slab[2], clip);
				slabs.add(slab);
			}
		}
	}

	protected void decorateCactusRoom(World world, Random rand, int bottom, int top,
	                                  int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		for (int dx = 1; dx <= this.size - 2; dx++) {
			for (int dz = 1; dz <= this.size - 2; dz++) {
				placeBlock(world, Blocks.SAND.id(), 0, dx, bottom - 1, dz, clip);
				if (!isWindowPos(dx, dz) && !isLadderPos(dx, dz, ladderUpDir, ladderDownDir)
					&& rand.nextInt(4) == 0) {
					placeBlock(world, Blocks.DEADBUSH.id(), 0, dx, bottom, dz, clip);
				}
			}
		}

		List<int[]> cacti = new ArrayList<>();
		cacti.add(new int[]{this.size / 2, bottom + 2, this.size / 2});

		for (int i = 0; i < this.size + 2; i++) {
			int[] cactus = {2 + rand.nextInt(this.size - 4), this.height - 2,
				2 + rand.nextInt(this.size - 4)};
			if (!chainCollides(cactus, cacti)) {
				for (int dy = bottom; dy < top; dy++) {
					placeBlock(world, Blocks.CACTUS.id(), 0, cactus[0], dy, cactus[2], clip);
				}
				cacti.add(cactus);
			}
		}
	}

	protected void decorateTreasureChest(World world, Random rand, int bottom, int top,
	                                     int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		int cx = this.size / 2;

		rand.nextInt(4);

		placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx - 1, bottom, cx - 1, clip);
		placeBlock(world, STONE_BRICK, 0, cx, bottom, cx - 1, clip);
		placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx + 1, bottom, cx - 1, clip);
		placeBlock(world, STONE_BRICK, 0, cx - 1, bottom, cx, clip);
		placeBlock(world, STONE_BRICK, 0, cx, bottom, cx, clip);
		placeBlock(world, STONE_BRICK, 0, cx + 1, bottom, cx, clip);
		placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx - 1, bottom, cx + 1, clip);
		placeBlock(world, STONE_BRICK, 0, cx, bottom, cx + 1, clip);
		placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx + 1, bottom, cx + 1, clip);

		placeTreasureChest(world, rand, cx, bottom + 1, cx, clip);
	}

	protected void decorateSpiderWebs(World world, Random rand, int bottom, int top,
	                                  int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		for (int dy = bottom; dy < top; dy++) {
			int chance = top - dy + 2;
			for (int dx = 1; dx <= this.size - 2; dx++) {
				for (int dz = 1; dz <= this.size - 2; dz++) {
					if (!isLadderPos(dx, dz, ladderUpDir, ladderDownDir)
						&& rand.nextInt(chance) == 0) {
						placeBlock(world, WEB, 0, dx, dy, dz, clip);
					}
				}
			}
		}

		if (rand.nextInt(5) == 0) {
			String spider = switch (rand.nextInt(4)) {
				case 1 -> HEDGE_SPIDER;
				case 2 -> SWARM_SPIDER;
				case 3 -> CAVE_SPIDER_STANDIN;
				default -> "minecraft:spider";
			};
			placeSpawner(world, rand, this.size / 2, bottom + 2, this.size / 2, spider, clip);
		}
	}

	protected void decorateSolidRock(World world, Random rand, int bottom, int top,
	                                 int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		for (int dy = bottom; dy < top; dy++) {
			for (int dx = 1; dx <= this.size - 2; dx++) {
				for (int dz = 1; dz <= this.size - 2; dz++) {
					if (!isLadderPos(dx, dz, ladderUpDir, ladderDownDir) && rand.nextInt(9) != 0) {
						placeBlock(world, STONE, 0, dx, dy, dz, clip);
					}
				}
			}
		}
	}

	protected void decorateLibrary(World world, Random rand, int bottom, int top,
	                               int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		for (int dx = 1; dx <= this.size - 2; dx++) {
			for (int dz = 1; dz <= this.size - 2; dz++) {
				for (int dy = bottom; dy < top - 1; dy++) {
					if ((dx == 1 || dx == this.size - 2 || dz == 1 || dz == this.size - 2)
						&& !isWindowPos(dx, dz)
						&& !isLadderPos(dx, dz, ladderUpDir, ladderDownDir)) {
						placeBlock(world, BOOKSHELF, 0, dx, dy, dz, clip);
					}
				}
			}
		}
	}

	protected void decorateFullLibrary(World world, Random rand, int bottom, int top,
	                                   int ladderUpDir, int ladderDownDir, BoundingBox clip) {
		for (int dx = 1; dx <= this.size - 2; dx++) {
			for (int dz = 1; dz <= this.size - 2; dz++) {
				for (int dy = bottom; dy < top; dy++) {
					boolean onLattice =
						dx % 2 != 0 && (dz >= dx && dz <= this.size - dx - 1
							|| dz >= this.size - dx - 1 && dz <= dx)
						|| dz % 2 != 0 && (dx >= dz && dx <= this.size - dz - 1
							|| dx >= this.size - dz - 1 && dx <= dz);

					if (onLattice
						&& !isWindowPos(dx, dy, dz)
						&& !isOpeningPos(dx, dy, dz)
						&& !isLadderPos(dx, dz, ladderUpDir, ladderDownDir)) {
						placeBlock(world, BOOKSHELF, 0, dx, dy, dz, clip);
					}
				}
			}
		}
	}

	protected boolean isWindowPos(int x, int z) {
		if (x == 1 && z == this.size / 2) return true;
		if (x == this.size - 2 && z == this.size / 2) return true;
		if (x == this.size / 2 && z == 1) return true;
		return x == this.size / 2 && z == this.size - 2;
	}

	protected boolean isWindowPos(int x, int y, int z) {
		int checkYDir = -1;
		if (x == 1 && z == this.size / 2) {
			checkYDir = 2;
		} else if (x == this.size - 2 && z == this.size / 2) {
			checkYDir = 0;
		} else if (x == this.size / 2 && z == 1) {
			checkYDir = 3;
		} else if (x == this.size / 2 && z == this.size - 2) {
			checkYDir = 1;
		}

		if (checkYDir <= -1) {
			return false;
		}
		return !this.openingTowards[checkYDir]
			&& (y == 2 || y == 3
			|| this.height > 8 && (y == this.height - 3 || y == this.height - 4));
	}

	protected boolean isOpeningPos(int x, int y, int z) {
		for (int[] door : this.openings) {
			int ix = door[0];
			int iz = door[2];
			if (ix == 0) {
				ix++;
			} else if (ix == this.size - 1) {
				ix--;
			} else if (iz == 0) {
				iz++;
			} else if (iz == this.size - 1) {
				iz--;
			}

			if (ix == x && iz == z && (door[1] == y || door[1] + 1 == y)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isLadderPos(int x, int z, int ladderUpDir, int ladderDownDir) {
		if (x == getLadderX(ladderUpDir) && z == getLadderZ(ladderUpDir)) {
			return true;
		}
		return x == getLadderX(ladderDownDir) && z == getLadderZ(ladderDownDir);
	}

	protected int getLadderX(int ladderDir) {
		return switch (ladderDir) {
			case 0 -> this.size - 2;
			case 1 -> this.size / 2 + 1;
			case 2 -> 1;
			case 3 -> this.size / 2 - 1;
			default -> this.size / 2;
		};
	}

	protected int getLadderZ(int ladderDir) {
		return switch (ladderDir) {
			case 0 -> this.size / 2 - 1;
			case 1 -> this.size - 2;
			case 2 -> this.size / 2 + 1;
			case 3 -> 1;
			default -> this.size / 2;
		};
	}

	protected int getLadderMeta(int ladderDir) {
		return switch ((this.coordBaseMode + ladderDir) % 4) {
			case 0 -> 4;
			case 1 -> 2;
			case 2 -> 5;
			case 3 -> 3;
			default -> -1;
		};
	}

	protected void decorateStairTower(World world, Random rand, BoundingBox clip) {
		if (this.height - this.highestOpening > 13) {
			int base = this.highestOpening + 3;
			int floors = (this.height - base) / 5;
			int floorHeight = (this.height - base) / floors;

			for (int i = 0; i < floors; i++) {
				for (int x = 1; x < this.size - 1; x++) {
					for (int z = 1; z < this.size - 1; z++) {
						placeBlock(world, PLANKS, 0, x, i * floorHeight + base, z, clip);
					}
				}
			}

			int ladderDir = 3;
			int downLadderDir;
			int meta = getLadderMeta(ladderDir);
			int dx = getLadderX(ladderDir);
			int dz = getLadderZ(ladderDir);

			for (int dy = 1; dy < 3; dy++) {
				placeBlock(world, LADDER, meta, dx, base - dy, dz, clip);
			}

			for (int i = 0; i < floors - 1; i++) {
				int bottom = base + 1 + floorHeight * i;
				int top = base + floorHeight * (i + 1);
				downLadderDir = ladderDir++;
				ladderDir %= 4;
				decorateFloor(world, rand, i, bottom, top, ladderDir, downLadderDir, clip);
			}

			decorateFloor(world, rand, floors, base + 1 + floorHeight * (floors - 1),
				this.height - 1, -1, ladderDir, clip);
		} else if (this.size > 5) {
			switch (rand.nextInt(5)) {
				case 0 -> decorateChandelier(world, rand, clip);
				case 1 -> decorateHangingChains(world, rand, clip);
				case 2 -> decorateFloatingBooks(world, rand, clip);
				case 3 -> decorateFloatingVines(world, rand, clip);
				default -> {  }
			}
		}

		if (this.size > 5) {
			if (rand.nextInt(4) == 0) {
				decorateStairWell(world, rand, clip);
			} else if (rand.nextInt(3) != 0) {
				decoratePlanter(world, rand, clip);
			}
		}
	}

	protected void decorateHangingChains(World world, Random rand, BoundingBox clip) {
		List<int[]> chains = new ArrayList<>();

		for (int i = 0; i < this.size + 2; i++) {
			int filled = this.size < 15 ? 2 : 4;
			int[] chain = {filled + rand.nextInt(this.size - filled * 2), this.height - 2,
				filled + rand.nextInt(this.size - filled * 2)};
			if (!chainCollides(chain, chains)) {
				int length = 1 + rand.nextInt(this.height - this.highestOpening - 3);
				decorateOneChain(world, rand, chain[0], length, chain[2], clip);
				chains.add(chain);
			}
		}
	}

	protected boolean chainCollides(int[] coords, List<int[]> taken) {
		for (int[] existing : taken) {
			if (coords[2] == existing[2] && Math.abs(coords[0] - existing[0]) <= 1) {
				return true;
			}
			if (coords[0] == existing[0] && Math.abs(coords[2] - existing[2]) <= 1) {
				return true;
			}
		}
		return false;
	}

	protected void decorateOneChain(World world, Random rand, int dx, int length, int dz,
	                                BoundingBox clip) {
		for (int y = 1; y <= length; y++) {
			placeBlock(world, BARS, 0, dx, this.height - y - 1, dz, clip);
		}

		int ballBlock;
		int ballMeta = 0;
		switch (rand.nextInt(10)) {
			case 0 -> ballBlock = Blocks.BLOCK_IRON.id();
			case 1 -> ballBlock = BOOKSHELF;
			case 2 -> ballBlock = Blocks.NETHERRACK.id();
			case 3 -> ballBlock = Blocks.SOULSAND.id();
			case 4 -> ballBlock = GLASS;
			case 5 -> ballBlock = Blocks.BLOCK_LAPIS.id();

			case 6 -> ballBlock = STONE_BRICK;
			default -> ballBlock = Blocks.GLOWSTONE.id();
		}

		placeBlock(world, ballBlock, ballMeta, dx, this.height - length - 2, dz, clip);
	}

	protected void decorateFloatingBooks(World world, Random rand, BoundingBox clip) {
		List<int[]> shelves = new ArrayList<>();

		for (int i = 0; i < this.size + 2; i++) {
			int filled = this.size < 15 ? 2 : 4;
			int[] shelf = {filled + rand.nextInt(this.size - filled * 2), this.height - 2,
				filled + rand.nextInt(this.size - filled * 2)};
			if (!chainCollides(shelf, shelves)) {
				int bottom = 2 + rand.nextInt(this.height - this.highestOpening - 3);
				int top = rand.nextInt(bottom - 1) + 2;
				for (int y = top; y <= bottom; y++) {
					placeBlock(world, BOOKSHELF, 0, shelf[0], this.height - y, shelf[2], clip);
				}
				shelves.add(shelf);
			}
		}
	}

	protected void decorateFloatingVines(World world, Random rand, BoundingBox clip) {
		List<int[]> mosses = new ArrayList<>();

		for (int i = 0; i < this.size + 2; i++) {
			int filled = this.size < 15 ? 2 : 4;
			int[] moss = {filled + rand.nextInt(this.size - filled * 2), this.height - 2,
				filled + rand.nextInt(this.size - filled * 2)};
			if (!chainCollides(moss, mosses)) {
				int bottom = 2 + rand.nextInt(this.height - this.highestOpening - 3);
				int top = rand.nextInt(bottom - 1) + 2;
				for (int y = top; y <= bottom; y++) {
					placeBlock(world, MOSSY_COBBLE, 0, moss[0], this.height - y, moss[2], clip);

				}
				mosses.add(moss);
			}
		}

		for (int y = this.highestOpening + 3; y < this.height - 1; y++) {
			for (int x = 1; x < this.size - 1; x++) {
				rand.nextInt(3);
				rand.nextInt(3);
			}
			for (int z = 1; z < this.size - 1; z++) {
				rand.nextInt(3);
				rand.nextInt(3);
			}
		}
	}

	protected void decoratePlanter(World world, Random rand, BoundingBox clip) {
		int cx = this.size / 2;
		placeBlock(world, SLAB_STONE, SLAB_LOWER, cx, 1, cx + 1, clip);
		placeBlock(world, SLAB_STONE, SLAB_LOWER, cx, 1, cx - 1, clip);
		placeBlock(world, SLAB_STONE, SLAB_LOWER, cx + 1, 1, cx, clip);
		placeBlock(world, SLAB_STONE, SLAB_LOWER, cx - 1, 1, cx, clip);
		placeBlock(world, Blocks.GRASS.id(), 0, cx, 1, cx, clip);

		int planterBlock;
		int planterMeta = 0;
		switch (rand.nextInt(8)) {

			case 0 -> planterBlock = Blocks.DEADBUSH.id();
			case 1 -> planterBlock = Blocks.TALLGRASS.id();
			case 2 -> planterBlock = Blocks.TALLGRASS_FERN.id();

			case 3 -> planterBlock = Blocks.SAPLING_OAK.id();
			case 4 -> planterBlock = Blocks.SAPLING_PINE.id();
			case 5 -> planterBlock = Blocks.SAPLING_BIRCH.id();
			case 6 -> planterBlock = Blocks.MUSHROOM_BROWN.id();
			default -> planterBlock = Blocks.MUSHROOM_RED.id();
		}

		placeBlock(world, planterBlock, planterMeta, cx, 2, cx, clip);

		if (planterBlock == Blocks.SAPLING_OAK.id() || planterBlock == Blocks.SAPLING_PINE.id()
			|| planterBlock == Blocks.SAPLING_BIRCH.id()) {
			int wx = getXWithOffset(cx, cx);
			int wy = getYWithOffset(2);
			int wz = getZWithOffset(cx, cx);
			if (world.getBlockId(wx, wy, wz) == planterBlock
				&& Blocks.getBlock(planterBlock) != null
				&& Blocks.getBlock(planterBlock).getLogic() instanceof BlockLogicSaplingBase sapling) {
				sapling.growTree(world, new TilePos(wx, wy, wz), world.rand);
			}
		}

		if (planterBlock == Blocks.MUSHROOM_BROWN.id() || planterBlock == Blocks.MUSHROOM_RED.id()) {
			int wx = getXWithOffset(cx, cx);
			int wy = getYWithOffset(2);
			int wz = getZWithOffset(cx, cx);
			if (world.getBlockId(wx, wy, wz) == planterBlock) {

				int species = planterBlock == Blocks.MUSHROOM_BROWN.id() ? 0 : 1;

				world.setBlockWithNotify(wx, wy, wz, 0);
				if (!new WorldFeatureTFBigMushroom(species).place(world, world.rand, wx, wy, wz)) {

					world.setBlockAndMetadataWithNotify(wx, wy, wz, planterBlock, planterMeta);
				}
			}
		}
	}

	protected void decorateStairWell(World world, Random rand, BoundingBox clip) {
		int cx = this.size / 2;
		int cy = 1;
		int fluid = rand.nextInt(4) == 0
			? Blocks.FLUID_LAVA_STILL.id() : Blocks.FLUID_WATER_STILL.id();

		if (this.size > 7) {
			placeBlock(world, STONE_BRICK, 0, cx - 1, cy, cx - 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx - 1, cy + 1, cx - 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx, cy, cx - 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx + 1, cy, cx - 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx + 1, cy + 1, cx - 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx - 1, cy, cx, clip);
			placeBlock(world, fluid, 0, cx, cy, cx, clip);
			placeBlock(world, STONE_BRICK, 0, cx + 1, cy, cx, clip);
			placeBlock(world, STONE_BRICK, 0, cx - 1, cy, cx + 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx - 1, cy + 1, cx + 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx, cy, cx + 1, clip);
			placeBlock(world, STONE_BRICK, 0, cx + 1, cy, cx + 1, clip);
			placeBlock(world, SLAB_STONE_BRICK, SLAB_LOWER, cx + 1, cy + 1, cx + 1, clip);
		}

		placeBlock(world, fluid, 0, cx, cy - 1, cx, clip);
	}

	protected void decorateChandelier(World world, Random rand, BoundingBox clip) {
		int cx = this.size / 2;
		int cy = this.highestOpening + 2 + rand.nextInt(this.height - this.highestOpening - 5);
		int cz = this.size / 2;

		placeBlock(world, FENCE, 0, cx, cy, cz, clip);
		placeBlock(world, FENCE, 0, cx - 1, cy, cz, clip);
		placeBlock(world, FENCE, 0, cx + 1, cy, cz, clip);
		placeBlock(world, FENCE, 0, cx, cy, cz - 1, clip);
		placeBlock(world, FENCE, 0, cx, cy, cz + 1, clip);
		placeBlock(world, FENCE, 0, cx, cy + 1, cz, clip);
		placeBlock(world, TORCH, 0, cx - 1, cy + 1, cz, clip);
		placeBlock(world, TORCH, 0, cx + 1, cy + 1, cz, clip);
		placeBlock(world, TORCH, 0, cx, cy + 1, cz - 1, clip);
		placeBlock(world, TORCH, 0, cx, cy + 1, cz + 1, clip);

		for (int y = cy; y < this.height - 1; y++) {
			placeBlock(world, FENCE, 0, cx, y, cz, clip);
		}
	}

	protected void makeOpenings(World world, BoundingBox clip) {
		for (int[] door : this.openings) {
			makeDoorOpening(world, door[0], door[1], door[2], clip);
		}
	}

	protected void makeDoorOpening(World world, int dx, int dy, int dz, BoundingBox clip) {
		placeBlock(world, 0, 0, dx, dy, dz, clip);
		placeBlock(world, 0, 0, dx, dy + 1, dz, clip);
		if (getBlockIdAt(world, dx, dy + 2, dz, clip) != 0) {
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, dx, dy + 2, dz, clip);
		}
	}

	protected void makeWindows(World world, Random rand, BoundingBox clip) {
		for (int i = 0; i < 4; i++) {
			if (!this.openingTowards[i]) {
				makeWindowBlock(world, this.size - 1, 2, this.size / 2, i, clip);
				makeWindowBlock(world, this.size - 1, 3, this.size / 2, i, clip);
				if (this.height > 8) {
					makeWindowBlock(world, this.size - 1, this.height - 3, this.size / 2, i, clip);
					makeWindowBlock(world, this.size - 1, this.height - 4, this.size / 2, i, clip);
				}
			}
		}
	}

	protected void makeWindowBlock(World world, int x, int y, int z, int rotation,
	                               BoundingBox clip) {
		int temp = this.coordBaseMode;
		this.coordBaseMode = (this.coordBaseMode + rotation) % 4;

		int outside = getBlockIdAt(world, x + 1, y, z, clip);
		int inside = getBlockIdAt(world, x - 1, y, z, clip);
		if (inside == 0 && outside == 0) {
			placeBlock(world, GLASS, 0, x, y, z, clip);
		}

		this.coordBaseMode = temp;
	}

	public int[] getValidOpening(Random rand, int direction) {
		int wLength = this.size - 2;
		int offset = 1;
		if (this.size == 15) {
			wLength = 11;
			offset = 2;
		}

		if (direction == 0 || direction == 2) {
			int rx = direction == 0 ? this.size - 1 : 0;
			int rz = offset + rand.nextInt(wLength);
			return new int[]{rx, getYByStairs(rz, rand, direction), rz};
		}
		if (direction == 1 || direction == 3) {
			int rx = offset + rand.nextInt(wLength);
			int rz = direction == 1 ? this.size - 1 : 0;
			return new int[]{rx, getYByStairs(rx, rand, direction), rz};
		}
		return new int[]{0, 0, 0};
	}

	private int getYByStairs(int rx, Random rand, int direction) {
		int rise = 1;
		int base = 0;

		if (this.size == 15) {
			rise = 10;
			base = direction != 0 && direction != 2 ? 28 : 23;
		}
		if (this.size == 9) {
			rise = 6;
			base = direction != 0 && direction != 2 ? 5 : 2;
		}
		if (this.size == 7) {
			rise = 4;
			base = direction != 0 && direction != 2 ? 4 : 2;
		}
		if (this.size == 5) {
			rise = 4;
			base = switch (direction) {
				case 0 -> 3;
				case 1 -> 2;
				case 2 -> 5;
				case 3 -> 4;
				default -> 0;
			};
		}

		int flights = (this.height - 6 - base) / rise + 1;
		if (base <= 0 || flights <= 0) {
			return 0;
		}

		int dy = rand.nextInt(flights) * rise + base;
		if (this.size == 15) {
			dy -= direction != 0 && direction != 3 ? (this.size - rx - 3) / 2 : (rx - 2) / 2;
		} else {
			dy -= direction != 0 && direction != 3 ? (this.size - rx - 2) / 2 : (rx - 1) / 2;
		}
		return Math.max(dy, 1);
	}

	protected boolean makeStairs(World world, Random rand, BoundingBox clip) {
		return switch (this.size) {
			case 15 -> makeStairs15(world, rand, clip);
			case 9 -> makeStairs9(world, rand, clip);
			case 7 -> makeStairs7(world, rand, clip);
			case 5 -> makeStairs5(world, rand, clip);
			default -> false;
		};
	}

	private void rotated(int rotation, Runnable body) {
		int temp = this.coordBaseMode;
		this.coordBaseMode = (this.coordBaseMode + rotation) % 4;
		body.run();
		this.coordBaseMode = temp;
	}

	protected boolean makeStairs5(World world, Random rand, BoundingBox clip) {
		int rise = 1;
		int flights = this.highestOpening / rise;
		for (int i = 0; i < flights; i++) {
			int height = i * rise;
			rotated(i * 3, () -> {
				placeBlock(world, SLAB_WOOD, SLAB_LOWER, 2, 1 + height, 3, clip);
				placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 3, 1 + height, 3, clip);
			});
		}
		return true;
	}

	protected boolean makeStairs7(World world, Random rand, BoundingBox clip) {
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 1, 1, 4, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 1, 1, 5, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 5, 1, 2, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 5, 1, 1, clip);

		int rise = 2;
		int flights = this.highestOpening / rise;
		for (int i = 0; i < flights; i++) {
			int height = 1 + i * rise;
			rotated(i * 3, () -> stairs7flight(world, clip, height));
			rotated(2 + i * 3, () -> stairs7flight(world, clip, height));
		}
		return true;
	}

	private void stairs7flight(World world, BoundingBox clip, int height) {
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 2, 1 + height, 5, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 3, 1 + height, 5, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 4, 2 + height, 5, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 5, 2 + height, 5, clip);
	}

	protected boolean makeStairs9(World world, Random rand, BoundingBox clip) {
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 1, 1, 6, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 1, 1, 7, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 7, 1, 2, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 7, 1, 1, clip);

		int rise = 3;
		int flights = this.highestOpening / rise;
		for (int i = 0; i < flights; i++) {
			int height = 1 + i * rise;
			rotated(i * 3, () -> stairs9flight(world, clip, height));
			rotated(2 + i * 3, () -> stairs9flight(world, clip, height));
		}
		return true;
	}

	private void stairs9flight(World world, BoundingBox clip, int height) {
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 2, 1 + height, 7, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 3, 1 + height, 7, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 4, 2 + height, 7, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 5, 2 + height, 7, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 6, 3 + height, 7, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 7, 3 + height, 7, clip);
	}

	protected boolean makeStairs15(World world, Random rand, BoundingBox clip) {
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 1, 1, 9, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 2, 1, 9, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 1, 1, 10, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 2, 1, 10, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 1, 2, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 2, 2, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 1, 2, 12, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 2, 2, 12, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 1, 2, 13, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 2, 2, 13, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 3, 2, 11, clip);
		placeBlock(world, FENCE, 0, 3, 3, 11, clip);
		placeBlock(world, FENCE, 0, 3, 4, 11, clip);
		placeBlock(world, TORCH, 0, 3, 5, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 3, 1, 10, clip);
		placeBlock(world, FENCE, 0, 3, 2, 10, clip);
		placeBlock(world, FENCE, 0, 3, 3, 10, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 3, 1, 9, clip);
		placeBlock(world, FENCE, 0, 3, 2, 9, clip);

		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 13, 1, 5, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 12, 1, 5, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 13, 1, 4, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 12, 1, 4, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 13, 2, 3, clip);
		placeBlock(world, SLAB_WOOD, SLAB_LOWER, 12, 2, 3, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 13, 2, 2, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 12, 2, 2, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 13, 2, 1, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 12, 2, 1, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 11, 2, 3, clip);
		placeBlock(world, FENCE, 0, 11, 3, 3, clip);
		placeBlock(world, FENCE, 0, 11, 4, 3, clip);
		placeBlock(world, TORCH, 0, 11, 5, 3, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 11, 1, 4, clip);
		placeBlock(world, FENCE, 0, 11, 2, 4, clip);
		placeBlock(world, FENCE, 0, 11, 3, 4, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 11, 1, 5, clip);
		placeBlock(world, FENCE, 0, 11, 2, 5, clip);

		int rise = 5;
		int flights = this.highestOpening / rise;
		for (int i = 0; i < flights; i++) {
			int height = 2 + i * rise;
			rotated(i * 3, () -> stairs15flight(world, clip, height));
			rotated(2 + i * 3, () -> stairs15flight(world, clip, height));
		}
		return true;
	}

	private void stairs15flight(World world, BoundingBox clip, int height) {

		for (int z = 12; z <= 13; z++) {
			placeBlock(world, SLAB_WOOD, SLAB_LOWER, 3, 1 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 4, 1 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_LOWER, 5, 2 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 6, 2 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_LOWER, 7, 3 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 8, 3 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_LOWER, 9, 4 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 10, 4 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_LOWER, 11, 5 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 12, 5 + height, z, clip);
			placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 13, 5 + height, z, clip);
		}

		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 4, 1 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 5, 2 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 6, 2 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 7, 3 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 8, 3 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 9, 4 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 10, 4 + height, 11, clip);
		placeBlock(world, SLAB_WOOD, SLAB_DOUBLE, 11, 5 + height, 11, clip);

		placeBlock(world, FENCE, 0, 4, 2 + height, 11, clip);
		placeBlock(world, FENCE, 0, 5, 3 + height, 11, clip);
		placeBlock(world, FENCE, 0, 6, 3 + height, 11, clip);
		placeBlock(world, FENCE, 0, 7, 4 + height, 11, clip);
		placeBlock(world, FENCE, 0, 8, 4 + height, 11, clip);
		placeBlock(world, FENCE, 0, 9, 5 + height, 11, clip);
		placeBlock(world, FENCE, 0, 10, 5 + height, 11, clip);
		placeBlock(world, FENCE, 0, 11, 6 + height, 11, clip);
		placeBlock(world, FENCE, 0, 4, 3 + height, 11, clip);
		placeBlock(world, FENCE, 0, 6, 4 + height, 11, clip);
		placeBlock(world, FENCE, 0, 8, 5 + height, 11, clip);
		placeBlock(world, FENCE, 0, 10, 6 + height, 11, clip);
		placeBlock(world, FENCE, 0, 11, 7 + height, 11, clip);
		placeBlock(world, TORCH, 0, 11, 8 + height, 11, clip);
	}

	protected void placeTreasureChest(World world, Random rand, int x, int y, int z, BoundingBox clip) {
		if (getBlockIdAt(world, x, y, z, clip) == Blocks.CHEST_PLANKS_OAK.id()) {
			return;
		}

		int wx = getXWithOffset(x, z);
		int wy = getYWithOffset(y);
		int wz = getZWithOffset(x, z);
		if (!clip.contains(wx, wy, wz)) {
			return;
		}
		TFTreasure.place(world, rand, wx, wy, wz, TFTreasure.TOWER_ROOM);
	}

	protected void makeOpeningMarkers(World world, Random rand, int numMarkers, BoundingBox clip) {
		if (this.size <= 4) {
			return;
		}
		for (int side = 0; side < 4; side++) {
			for (int i = 0; i < numMarkers; i++) {
				int[] spot = getValidOpening(rand, side);
				placeBlock(world, Blocks.WOOL.id(), side, spot[0], spot[1], spot[2], clip);
			}
		}
	}
}
