package com.twilightforest.mixin;

import com.twilightforest.TwilightForest;
import com.twilightforest.achievement.TFAchievements;
import com.twilightforest.block.BlockLogicTFPortal;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.TFDimension;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityItem.class)
public abstract class EntityItemPortalMixin extends Entity {

	private EntityItemPortalMixin(World world) {
		super(world);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void twilightforest$diamondOpensPortal(CallbackInfo ci) {
		EntityItem self = (EntityItem) (Object) this;

		if (this.world == null || !this.isInWater()) {
			return;
		}

		ItemStack stack = self.item;
		if (stack == null || stack.itemID != Items.DIAMOND.id) {
			return;
		}

		for (int i = 0; i < 2; i++) {
			double xd = this.world.rand.nextGaussian() * 0.02;
			double yd = this.world.rand.nextGaussian() * 0.02;
			double zd = this.world.rand.nextGaussian() * 0.02;
			this.world.spawnParticle("portal", this.x, this.y + 0.2, this.z, xd, yd, zd, 0, false);
		}

		if (this.world.isClientSide || !TFDimension.isRegistered()) {
			return;
		}

		if (TFBlocks.PORTAL_TWILIGHT == null) {
			return;
		}
		BlockLogic logic = TFBlocks.PORTAL_TWILIGHT.getLogic();
		if (!(logic instanceof BlockLogicTFPortal portal)) {
			return;
		}

		int bx = MathHelper.floor(this.x);
		int by = MathHelper.floor(this.y);
		int bz = MathHelper.floor(this.z);

		if (portal.tryToCreatePortal(this.world, bx, by, bz)) {
			twilightforest$lastFailure = null;

			Player nearby = this.world.getClosestPlayerToEntity(this, 8.0D);
			TFAchievements.award(nearby, TFAchievements.PORTAL);

			if (--stack.stackSize <= 0) {
				this.remove();
			}
			return;
		}

		String reason = portal.describePortalFailure(this.world, bx, by, bz);
		if (reason != null && !reason.equals(twilightforest$lastFailure)) {
			twilightforest$lastFailure = reason;
			TwilightForest.LOGGER.info("Diamond at {},{},{} did not open a portal: {}",
				bx, by, bz, reason);
		}
	}

	@Unique
	private String twilightforest$lastFailure;
}
