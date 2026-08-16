package com.twilightforest.entity.ai.harness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.twilightforest.entity.ai.TFPathfinder;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import org.joml.primitives.AABBd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class FakeWorld {

	static {
		bootstrap();
	}

	public static void bootstrap() {
		net.minecraft.core.block.Blocks.init();
	}

	public final World world;

	private final Map<Long, Integer> blocks = new HashMap<>();
	private final Set<Long> unloadedChunks = new HashSet<>();

	private final List<Entity> entities = new ArrayList<>();

	public FakeWorld() {
		this(new Random(1234L));
	}

	public FakeWorld(Random rand) {

		this.world = mock(World.class, withSettings().stubOnly().lenient());
		Reflect.set(this.world, "rand", rand);

		when(this.world.getBlockId(anyInt(), anyInt(), anyInt())).thenAnswer(inv ->
			this.blocks.getOrDefault(key(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)), 0));
		when(this.world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenReturn(0);
		when(this.world.getTileEntity(anyInt(), anyInt(), anyInt())).thenReturn(null);
		when(this.world.isChunkLoaded(anyInt(), anyInt())).thenAnswer(inv ->
			!this.unloadedChunks.contains(chunkKey(inv.getArgument(0), inv.getArgument(1))));

		org.mockito.Mockito.doAnswer(inv -> {
			Class<?> type = inv.getArgument(0);
			org.joml.primitives.AABBdc box = inv.getArgument(1);
			List<Entity> hits = new ArrayList<>();
			for (Entity e : this.entities) {
				boolean inside = e.x >= box.minX() && e.x <= box.maxX()
					&& e.y >= box.minY() && e.y <= box.maxY()
					&& e.z >= box.minZ() && e.z <= box.maxZ();
				if (type.isInstance(e) && inside) {
					hits.add(e);
				}
			}
			return hits;
		}).when(this.world).getEntitiesWithinAABB(any(), any());
		when(this.world.getClosestPlayer(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
			.thenAnswer(inv -> {
				double x = inv.getArgument(0), y = inv.getArgument(1), z = inv.getArgument(2);
				double range = inv.getArgument(3);
				net.minecraft.core.entity.player.Player best = null;
				double bestSq = range < 0.0 ? Double.MAX_VALUE : range * range;
				for (Entity e : this.entities) {
					if (!(e instanceof net.minecraft.core.entity.player.Player)) continue;
					double dsq = sq(e.x - x) + sq(e.y - y) + sq(e.z - z);
					if (dsq <= bestSq) {
						bestSq = dsq;
						best = (net.minecraft.core.entity.player.Player) e;
					}
				}
				return best;
			});

		when(this.world.getEntityPathToTilePos(any(), any(), anyFloat())).thenAnswer(inv -> {
			Entity mob = inv.getArgument(0);
			TilePos to = inv.getArgument(1);
			return TFPathfinder.findPath(this.world, mob, to.x, to.y, to.z, 1.0, null);
		});
		when(this.world.getEntityPathToXYZ(any(), anyInt(), anyInt(), anyInt(), anyFloat()))
			.thenAnswer(inv -> TFPathfinder.findPath(this.world, inv.getArgument(0),
				inv.getArgument(1), inv.getArgument(2), inv.getArgument(3), 1.0, null));

		org.mockito.Mockito.doAnswer(inv -> {
			net.minecraft.core.data.gamerule.GameRule<?> rule = inv.getArgument(0);
			return rule.getDefaultValue();
		}).when(this.world).getGameRuleValue(any());
	}

	private static double sq(double d) {
		return d * d;
	}

	private static long key(int x, int y, int z) {
		return TFPathfinder.packKey(x, y, z);
	}

	private static long chunkKey(int cx, int cz) {
		return ((long) cx << 32) ^ (cz & 0xFFFFFFFFL);
	}

	public FakeWorld set(int x, int y, int z, Block<?> block) {
		if (block == null) {
			this.blocks.remove(key(x, y, z));
		} else {
			this.blocks.put(key(x, y, z), block.id());
		}
		return this;
	}

	public FakeWorld fill(int x0, int y0, int z0, int x1, int y1, int z1, Block<?> block) {
		for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
			for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
				for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
					set(x, y, z, block);
				}
			}
		}
		return this;
	}

	public FakeWorld floor(int y, int r, Block<?> block) {
		return fill(-r, y, -r, r, y, r, block);
	}

	public FakeWorld unloadChunkAt(int x, int z) {
		this.unloadedChunks.add(chunkKey(x >> 4, z >> 4));
		return this;
	}

	public MobPathfinder mob() {
		return inhabit(mock(MobPathfinder.class, withSettings().stubOnly().lenient()));
	}

	public net.minecraft.core.entity.player.Player player() {
		return player(true);
	}

	public net.minecraft.core.entity.player.Player player(boolean frightening) {
		net.minecraft.core.entity.player.Player p = mock(net.minecraft.core.entity.player.Player.class,
			withSettings().stubOnly().lenient());
		net.minecraft.core.player.gamemode.Gamemode mode =
			mock(net.minecraft.core.player.gamemode.Gamemode.class, withSettings().stubOnly().lenient());
		when(mode.hasHostileMobs()).thenReturn(frightening);
		when(p.getGamemode()).thenReturn(mode);
		return inhabit(p);
	}

	public net.minecraft.core.entity.EntityItem item(net.minecraft.core.item.ItemStack stack,
	                                                 double x, double y, double z) {
		net.minecraft.core.entity.EntityItem entity =
			new net.minecraft.core.entity.EntityItem(this.world, x, y, z, stack);
		this.entities.add(entity);
		return place(entity, x, y, z);
	}

	private <T extends Entity> T inhabit(T entity) {
		entity.world = this.world;
		entity.bbWidth = 0.6F;
		entity.bbHeight = 1.8F;
		Reflect.set(entity, "bb", new AABBd());
		Reflect.set(entity, "random", new Random(4321L));
		when(entity.distanceToSqr(anyDouble(), anyDouble(), anyDouble())).thenAnswer(inv -> {
			double dx = (double) inv.getArgument(0) - entity.x;
			double dy = (double) inv.getArgument(1) - entity.y;
			double dz = (double) inv.getArgument(2) - entity.z;
			return dx * dx + dy * dy + dz * dz;
		});
		when(entity.distanceToSqr(any(Entity.class))).thenAnswer(inv -> {
			Entity o = inv.getArgument(0);
			return sq(o.x - entity.x) + sq(o.y - entity.y) + sq(o.z - entity.z);
		});
		when(entity.isAlive()).thenReturn(true);
		when(entity.isOnFire()).thenReturn(false);
		if (entity instanceof net.minecraft.core.entity.Mob) {

			when(((net.minecraft.core.entity.Mob) entity).canEntityBeSeen(any())).thenReturn(true);
		}
		this.entities.add(entity);
		return entity;
	}

	public <T extends Entity> T place(T entity, double x, double y, double z) {
		entity.x = x;
		entity.y = y;
		entity.z = z;
		double hw = entity.bbWidth / 2.0;
		entity.bb.setMin(x - hw, y, z - hw).setMax(x + hw, y + entity.bbHeight, z + hw);
		return entity;
	}

	public void remove(Entity entity) {
		this.entities.remove(entity);
	}
}
