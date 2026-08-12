package com.twilightforest.mixin;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.BlockLogicTFPortal;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicPortal;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.DyeColor;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.PortalHandler;
import net.minecraft.core.world.World;
import net.minecraft.core.world.type.WorldType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalHandler.class)
public class PortalHandlerMixin {

	@Inject(method = "generatePortal", at = @At("HEAD"), cancellable = true)
	private void twilightforest$generatePool(World world, Entity entity, @Nullable DyeColor portalColor,
	                                         Dimension oldDim, Dimension newDim,
	                                         CallbackInfoReturnable<Boolean> cir) {

		Block<? extends BlockLogicPortal> target =
			newDim.homeDim == null ? oldDim.portalBlock : newDim.portalBlock;
		if (target == null || !(target.getLogic() instanceof BlockLogicTFPortal portal)) {
			return;
		}

		WorldType oldType = oldDim.getDimensionData(world).getWorldType();
		WorldType newType = newDim.getDimensionData(world).getWorldType();
		double scaled = entity.y - entity.heightOffset;
		scaled = (scaled - oldType.getMinPortalY())
			/ (double) (oldType.getMaxPortalY() - oldType.getMinPortalY());
		scaled = scaled * (newType.getMaxPortalY() - newType.getMinPortalY()) + newType.getMinPortalY();
		scaled = MathHelper.clamp(scaled, newType.getMinPortalY(), newType.getMaxPortalY());

		int px = MathHelper.floor(entity.x);
		int pz = MathHelper.floor(entity.z);

		int surface = world.getHeightValue(px, pz);
		int py = surface > 1 ? surface : MathHelper.floor(scaled);

		portal.makePortalAt(world, px, py, pz);
		TwilightForest.LOGGER.info("Built a Twilight Forest portal at {},{},{}.", px, py, pz);
		cir.setReturnValue(true);
	}
}
