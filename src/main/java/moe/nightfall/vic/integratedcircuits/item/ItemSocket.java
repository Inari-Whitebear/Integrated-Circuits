package moe.nightfall.vic.integratedcircuits.item;

import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemSocket extends ItemBase {
	public ItemSocket() {
		super("socket");
		setHasIcon(false);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Vec3d vhit = new Vec3d(hitX, hitY, hitZ);
		pos.offset(facing);
		if (place(stack, player, world, pos, facing, vhit)) {
			return EnumActionResult.SUCCESS;
		}
		else
		{
			return EnumActionResult.FAIL;
		}
	}

	private boolean place(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, Vec3d vhit) {
		BlockPos pos2 = new BlockPos(pos);
		pos2.offset(side.getOpposite()); // is side ^ 1 the same as get opposite?
		if (!MiscUtils.canPlaceGateOnSide(world, pos2, side))
			return false;

		if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
			world.setBlockState(pos, Content.blockSocket.getDefaultState());
			TileEntitySocket te = (TileEntitySocket) world.getTileEntity(pos);
			te.getSocket().preparePlacement(player, pos2, side, stack);
			MiscUtils.playPlaceSound(player, world, pos);

			if (!player.capabilities.isCreativeMode)
				stack.stackSize--;
			return true;
		}
		return false;
	}
}
