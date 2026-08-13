package com.twilightforest.entity;

import com.twilightforest.TwilightForest;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;

public class MobTFMosquitoSwarm extends MobMonster {

	private static final int BITE_DAMAGE = 1;

	private static final int BITE_INTERVAL = 120;

	public MobTFMosquitoSwarm(World world) {
		super(world);
		this.setTextureIdentifier(TwilightForest.MOD_ID, "mosquitoswarm");
		this.setSize(0.7F, 1.9F);

		this.moveSpeed = 0.7F;

		this.attackStrength = BITE_DAMAGE;
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if (this.attackTime <= 0 && distance < 2.0F
			&& target.bb.maxY > this.bb.minY && target.bb.minY < this.bb.maxY) {
			this.attackTime = BITE_INTERVAL;
			target.hurt(this, BITE_DAMAGE, DamageType.COMBAT);
		}
	}

	@Override
	public int getMaxHealth() {
		return 12;
	}

	@Override
	public String getLivingSound() {
		return TwilightForest.MOD_ID + ":mob.tf.mosquito.mosquito";
	}
}
