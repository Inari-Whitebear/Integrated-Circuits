package moe.nightfall.vic.integratedcircuits.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.DiskDrive;
import moe.nightfall.vic.integratedcircuits.DiskDrive.IDiskDrive;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


public class BlockCAD extends Block {
	public BlockCAD() {
		super(Material.IRON);
		setRegistryName("pcblayoutcad");
		setUnlocalizedName(Constants.MOD_ID + ".pcblayoutcad");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileEntityCAD te = (TileEntityCAD) world.getTileEntity(blockPos);
			int rotation = te.rotation;
			boolean canInteract = rotation == 3 && side.ordinal() == 4 || rotation == 0 && side.ordinal() == 2 || rotation == 1
					&& side.ordinal() == 5 || rotation == 2 && side.ordinal() == 3;
			if (canInteract)
				player.openGui(IntegratedCircuits.instance, 0, world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
			return canInteract;
		}
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos blockPos, IBlockState blockState, Block neighborBlock) {
		TileEntityCAD te = (TileEntityCAD) world.getTileEntity(blockPos);
		if (te != null) {
			te.onNeighborBlockChange();
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityCAD te = (TileEntityCAD) world.getTileEntity(blockPos);
		if (te != null) {
			te.rotation = rotation;
		}
	}

	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
		DiskDrive.dropFloppy((IDiskDrive) world.getTileEntity(blockPos), world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	/*
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return getIcon(null, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int s) {
		return getIcon((TileEntityCAD) world.getTileEntity(x, y, z), s);
	}

	@SideOnly(Side.CLIENT)
	private IIcon getIcon(TileEntityCAD te, int s) {
		boolean on = false;
		int rotation = 2;
		if (te != null && te.playersUsing > 0)
			on = true;
		if (te != null)
			rotation = te.rotation;

		if (rotation == 0 && s == 2 || rotation == 1 && s == 5 || rotation == 2 && s == 3 || rotation == 3 && s == 4)
			return on ? Resources.ICON_CAD_FRONT_ON : Resources.ICON_CAD_FRONT_OFF;
		if (rotation == 0 && s == 3 || rotation == 1 && s == 4 || rotation == 2 && s == 2 || rotation == 3 && s == 5)
			return on ? Resources.ICON_CAD_BACK_ON : Resources.ICON_CAD_BACK_OFF;

		return Resources.ICON_CAD_SIDE;
	}*/

	@Override
	public TileEntity createTileEntity(World world, IBlockState blockState) {
		TileEntityCAD te = new TileEntityCAD();
		te.setup(32);
		return te;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	/*
	@Override
	public void registerBlockIcons(IIconRegister ir) {
	}*/

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing side) {
		return ((TileEntityContainer) world.getTileEntity(blockPos)).rotate();
	}
}
