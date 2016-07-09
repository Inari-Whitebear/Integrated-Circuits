package moe.nightfall.vic.integratedcircuits.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Interface;

@InterfaceList({
	@Interface(iface = "mrtjp.projectred.api.IScrewdriver", modid = "ProjRed|Core"),
	@Interface(iface = "com.bluepowermod.api.misc.IScrewdriver", modid = "bluepowerAPI"),
	@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|tools")
})
public class ItemScrewdriver extends ItemBase /*implements mrtjp.projectred.api.IScrewdriver,
                                                         com.bluepowermod.api.misc.IScrewdriver,
                                                         buildcraft.api.tools.IToolWrench*/ {
	public ItemScrewdriver() {
		super("screwdriver");
		setMaxStackSize(1);
		setMaxDamage(128);
		setNoRepair();
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		Block blockClicked = world.getBlockState(pos).getBlock();

		boolean rotate = !player.isSneaking();

		if (rotate) {
			if (blockClicked.rotateBlock(world, pos, side)) {
				return EnumActionResult.SUCCESS;
			}
		}

		return super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
	}
/*
	@Override
	public void damageScrewdriver(EntityPlayer player, ItemStack stack) {
		stack.damageItem(1, player);
	*/

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
/*
	@Override
	public boolean damage(ItemStack stack, int damage, EntityPlayer player, boolean simulated) {
		if (player != null)
			if (player.capabilities.isCreativeMode)
				return true;

		if (!simulated) stack.damageItem(1, player);
		return true;
	}*/
/*
	@Override
	public boolean canUse(EntityPlayer player, ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return false;
	}
	
	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
		player.swingItem();
	}*/
}
