package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPrinter extends Block {

	public BlockPrinter() {
		super(Material.IRON);
		setUnlocalizedName(Constants.MOD_ID + ".pcbprinter");
		setRegistryName("pcbprinter");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
		//setBlockBounds(0, 0, 0, 1F, 0.5F, 1F);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState blockState) {
		return new TileEntityPrinter();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	/*
	@Override
	public IIcon getIcon(int par1, int par2) {
		return Resources.ICON_ASSEMBLER_BOTTOM;
	}*/


	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(blockPos);
		if (te != null)
			te.rotation = rotation;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(blockPos);
		if (!world.isRemote) {
			if (te != null) {
				if (heldItem != null) {
					boolean b = te.addInk(heldItem);
					if (!b)
						b = te.addPaper(heldItem);
					if (b) {
						world.notifyBlockUpdate(blockPos, blockState, blockState, 0); // FIXME blockstates // FIXME flags
					}
					if (heldItem.stackSize < 1) {
						player.setHeldItem(hand, null);
					}
				} else {
					if (te.paperCount() > 0 && player.isSneaking()) {
						player.setHeldItem(hand, te.getStackInSlot(0));
						te.setInventorySlotContents(0, null);
						world.notifyBlockUpdate(blockPos, blockState, blockState, 0); // FIXME blockstates // FIXME flags
					}
				}
			}
		}
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
		TileEntityPrinter te = (TileEntityPrinter) world.getTileEntity(blockPos);
		if (te != null)
			MiscUtils.dropItem(world, te.getStackInSlot(0), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing side) {
		return ((TileEntityContainer) world.getTileEntity(blockPos)).rotate();
	}

/*	@Override
	public int getRenderType() {
		return Constants.PRINTER_RENDER_ID;
	}*/

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}
}
