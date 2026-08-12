package com.twilightforest.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.particle.Particle;
import net.minecraft.client.render.particle.ParticleTextureCache;
import net.minecraft.core.world.World;

@Environment(EnvType.CLIENT)
public class ParticleTFFirefly extends Particle {

	private final float baseScale;

	private final int halfLife;

	public ParticleTFFirefly(World world, double x, double y, double z,
	                         float red, float green, float blue) {
		this(world, x, y, z, 1.0F, red, green, blue);
	}

	public ParticleTFFirefly(World world, double x, double y, double z, float scale,
	                         float red, float green, float blue) {
		super(world, x, y, z, 0.0, 0.0, 0.0);

		this.xd *= 2.100000001490116;
		this.yd *= 2.100000001490116;
		this.zd *= 2.100000001490116;

		if (green == 0.0F) {
			green = 1.0F;
		}

		this.rCol = this.gCol = 1.0F * scale;
		this.rCol *= 0.9F;
		this.bCol = 0.0F;

		this.size *= scale;
		this.baseScale = this.size;

		int life = (int) (32.0 / (Math.random() * 0.8 + 0.2));
		this.lifetime = (int) (life * scale);
		this.halfLife = this.lifetime / 2;

		this.noPhysics = true;
		this.gravity = 0.0F;
		this.tex = ParticleTextureCache.ICON_FIREFLY;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}

		float ramp = this.halfLife <= 0 ? 1.0F
			: this.age < this.halfLife
				? (float) this.age / this.halfLife
				: 1.0F - (float) (this.age - this.halfLife) / this.halfLife;
		if (ramp < 0.0F) {
			ramp = 0.0F;
		}
		this.rCol = 0.9F * ramp;
		this.gCol = ramp;
		this.bCol = 0.0F;
		this.size = this.baseScale * ramp;

		this.move(this.xd, this.yd, this.zd);

		if (this.y == this.yo) {
			this.xd *= 1.1;
			this.zd *= 1.1;
		}

		this.xd *= 0.96F;
		this.yd *= 0.96F;
		this.zd *= 0.96F;

		if (this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
		}
	}
}
