package com.twilightforest.compat;

import com.twilightforest.TwilightForest;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class DecorationWindow {

	private DecorationWindow() {}

	private static final boolean ENABLED =
		!"false".equalsIgnoreCase(System.getProperty("twilightforest.cascadeGuard"));

	private static final int MAX_DEPTH = 8;

	private static final World[] stackWorld = new World[MAX_DEPTH];
	private static final int[] stackChunkX = new int[MAX_DEPTH];
	private static final int[] stackChunkZ = new int[MAX_DEPTH];
	private static int depth;

	@Nullable
	private static World windowWorld;
	private static int minX;
	private static int minZ;
	private static int maxX;
	private static int maxZ;

	private static final int MAX_PENDING_INTS = 2_000_000;

	private static final Map<World, Map<Long, Pending>> pending = new IdentityHashMap<>();
	private static int pendingInts;
	private static boolean loggedOverflow;

	private static long deferred;
	private static long written;
	private static final long LOG_EVERY = 50_000;

	private static final class Pending {
		private int[] data = new int[80];
		private int size;

		void add(int x, int y, int z, int blockId, int metadata) {
			if (this.size + 5 > this.data.length) {
				this.data = Arrays.copyOf(this.data, this.data.length * 2);
			}
			this.data[this.size++] = x;
			this.data[this.size++] = y;
			this.data[this.size++] = z;
			this.data[this.size++] = blockId;
			this.data[this.size++] = metadata;
		}
	}

	private static long key(int chunkX, int chunkZ) {
		return (long) chunkX << 32 | (chunkZ & 0xFFFFFFFFL);
	}

	public static void open(@NotNull World world, int chunkX, int chunkZ) {
		if (!ENABLED) {
			return;
		}
		if (depth < MAX_DEPTH) {
			stackWorld[depth] = world;
			stackChunkX[depth] = chunkX;
			stackChunkZ[depth] = chunkZ;
		}
		depth++;
		apply(world, chunkX, chunkZ);
	}

	public static void close(@NotNull World world, int chunkX, int chunkZ) {
		if (!ENABLED) {
			return;
		}
		depth--;
		if (depth < 0) {
			depth = 0;
		}
		if (depth > 0 && depth <= MAX_DEPTH) {
			apply(stackWorld[depth - 1], stackChunkX[depth - 1], stackChunkZ[depth - 1]);
		} else {
			windowWorld = null;
		}

		flush(world, chunkX, chunkZ);

		if (depth == 0 && !pending.isEmpty()) {
			sweep(world);
		}
	}

	private static void apply(@Nullable World world, int chunkX, int chunkZ) {
		windowWorld = world;
		minX = chunkX * 16;
		minZ = chunkZ * 16;
		maxX = minX + 31;
		maxZ = minZ + 31;
	}

	public static boolean wouldCascade(@NotNull World world, int x, int z) {
		if (windowWorld != world) {
			return false;
		}
		if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
			return false;
		}
		return !world.isChunkLoaded(x >> 4, z >> 4);
	}

	public static boolean deferWrite(@NotNull World world, int x, int y, int z,
	                                 int blockId, int metadata) {
		if (pendingInts >= MAX_PENDING_INTS) {
			if (!loggedOverflow) {
				loggedOverflow = true;
				TwilightForest.LOGGER.warn(
					"Deferred decoration queue full at {} ints; further out-of-chunk writes will "
						+ "generate their neighbour as before. This should not happen in normal play.",
					pendingInts);
			}
			return false;
		}
		pending.computeIfAbsent(world, w -> new HashMap<>())
			.computeIfAbsent(key(x >> 4, z >> 4), k -> new Pending())
			.add(x, y, z, blockId, metadata);
		pendingInts += 5;

		deferred++;
		if (deferred == 1 || deferred % LOG_EVERY == 0) {
			TwilightForest.LOGGER.info(
				"Out-of-chunk decoration deferred rather than generating the neighbour: {} block(s) "
					+ "queued, {} written back so far, {} still queued.",
				deferred, written, pendingInts / 5);
		}
		return true;
	}

	private static void flush(@NotNull World world, int chunkX, int chunkZ) {
		Map<Long, Pending> forWorld = pending.get(world);
		if (forWorld == null) {
			return;
		}
		Pending queued = forWorld.remove(key(chunkX, chunkZ));
		if (queued == null) {
			return;
		}
		if (forWorld.isEmpty()) {
			pending.remove(world);
		}
		pendingInts -= queued.size;

		int placed = 0;
		for (int i = 0; i < queued.size; i += 5) {
			int x = queued.data[i];
			int y = queued.data[i + 1];
			int z = queued.data[i + 2];

			if (world.getBlockId(x, y, z) == 0
				&& world.setBlockAndMetadataRaw(x, y, z, queued.data[i + 3], queued.data[i + 4])) {
				placed++;
			}
		}
		written += placed;

		if (placed > 0) {
			int baseX = chunkX * 16;
			int baseZ = chunkZ * 16;
			int minY = world.getWorldType().getMinY(world);
			int maxY = world.getWorldType().getMaxY(world);

			world.scheduleLightingUpdate(LightLayer.Sky,
				baseX, minY, baseZ, baseX + 15, maxY, baseZ + 15);

			world.markBlocksDirty(
				new TilePos(baseX, minY, baseZ),
				new TilePos(baseX + 15, maxY, baseZ + 15));
		}
	}

	private static void sweep(@NotNull World world) {
		Map<Long, Pending> forWorld = pending.get(world);
		if (forWorld == null) {
			return;
		}

		List<Long> ready = null;
		for (Long k : forWorld.keySet()) {
			int chunkX = (int) (k >> 32);
			int chunkZ = k.intValue();
			if (world.isChunkLoaded(chunkX, chunkZ)) {
				if (ready == null) {
					ready = new ArrayList<>();
				}
				ready.add(k);
			}
		}
		if (ready == null) {
			return;
		}

		for (Long k : ready) {
			flush(world, (int) (k >> 32), k.intValue());
		}
	}
}
