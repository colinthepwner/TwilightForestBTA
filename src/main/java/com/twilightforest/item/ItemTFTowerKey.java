package com.twilightforest.item;

import com.twilightforest.block.BlockLogicTFTowerDevice;
import com.twilightforest.block.TFBlocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;

public class ItemTFTowerKey extends Item {

	public ItemTFTowerKey(String name, String texture, int id) {
		super(name, texture, id);
	}

	@Override
	public boolean onUseOnBlock(ItemStack itemStack, World world, Player player,
	                            TilePosc pos, Side side, double xHit, double yHit) {
		if (world.isClientSide) {
			return false;
		}
		if (world.getBlockId(pos.x(), pos.y(), pos.z()) != TFBlocks.TOWER_DEVICE.id()
			|| world.getBlockMetadata(pos.x(), pos.y(), pos.z())
				!= BlockLogicTFTowerDevice.META_VANISH_LOCKED) {
			return false;
		}

		BlockLogicTFTowerDevice.unlockBlock(world, pos.x(), pos.y(), pos.z());
		itemStack.stackSize--;
		return true;
	}
}
