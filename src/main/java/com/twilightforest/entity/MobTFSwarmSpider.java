package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.monster.MobSpider;
import net.minecraft.core.world.World;

public class MobTFSwarmSpider extends MobSpider {

	private boolean spawnMore;

	public MobTFSwarmSpider(World world) {
		this(world, true);
	}

	public MobTFSwarmSpider(World world, boolean spawnMore) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "swarmspider");
		this.setSize(0.8F, 0.4F);
		this.spawnMore = spawnMore;
	}

	@Override
	public int getMaxHealth() {
		return 3;
	}

	@Override
	public void tick() {
		if (this.spawnMore && !this.world.isClientSide) {
			int more = 1 + this.random.nextInt(2);
			for (int i = 0; i < more; i++) {
				if (!spawnAnother()) {

					spawnAnother();
				}
			}
			this.spawnMore = false;
		}
		super.tick();
	}

	protected MobTFSwarmSpider createSibling() {
		return new MobTFSwarmSpider(this.world, false);
	}

	private boolean spawnAnother() {
		MobTFSwarmSpider another = createSibling();

		double sx = this.x + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;
		double sy = this.y + this.random.nextInt(3) - 1.0;
		double sz = this.z + (this.random.nextDouble() - this.random.nextDouble()) * 4.0;

		another.moveTo(sx, sy, sz, this.random.nextFloat() * 360.0F, 0.0F);
		this.world.entityJoinedWorld(another);
		return true;
	}
}
