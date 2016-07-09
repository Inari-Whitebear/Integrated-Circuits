package moe.nightfall.vic.integratedcircuits.tile;

import java.util.List;
import java.util.Random;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.compat.gateio.GateIO;
import moe.nightfall.vic.integratedcircuits.gate.Socket;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSocket extends Block {
	public BlockSocket() {
		super(Material.CIRCUITS);
		setUnlocalizedName(Constants.MOD_ID + ".gate");
		setRegistryName("gate");
		setHardness(1);

		//Socket.box.setBlockBounds(this);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Content.itemSocket;
	}

	@Override
	public ItemStack getPickBlock(IBlockState blockState, RayTraceResult target, World world, BlockPos blockPos, EntityPlayer player) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);
		if (te == null)
			return null;
		ItemStack stack = te.getSocket().pickItem(target);
		return stack != null ? stack : new ItemStack(Content.itemSocket);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos blockPos, EffectRenderer effectRenderer) {
		return true;
	}


	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing axis) {
		return !world.isRemote &&
			((TileEntitySocket) world.getTileEntity(blockPos)).getSocket().rotate();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState blockState, World world, RayTraceResult target, EffectRenderer effectRenderer) {
		return true;
	}

	public void setBlockBoundsBasedOnState(IBlockAccess world, IBlockState blockState, BlockPos blockPos) { //FIXME reimplement this
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);
		if (te == null)
			return;
		//Cuboid6 bounds = Socket.box.copy().apply(Socket.getRotationTransformation(te.getSocket()));
		//bounds.setBlockBounds(this);
	}

	public AxisAlignedBB getStatelessBoundingBox(World world, BlockPos blockPos) {
		return null;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World world, BlockPos blockPos) {
		if (world.getBlockState(blockPos) != this) { // BUGFIX: mojang randomly calls this method for blocks not in the world!
			return getStatelessBoundingBox(world, blockPos); // see: net.minecraft.item.ItemBlock.func_150936_a (1.7.10 srg)
		}

		setBlockBoundsBasedOnState(world, blockState, blockPos);
		return super.getCollisionBoundingBox(blockState, world, blockPos);
	}

	@Override
	public void updateTick(World world, BlockPos blockPos, IBlockState blocSstate, Random random) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);
		te.getSocket().scheduledTick();
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos blockPos, IBlockState blockState, Block neighborBlock) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);
		if (!te.isDestroyed)
			te.getSocket().onNeighborChanged();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);

		ItemStack heldStack = player.getHeldItem();
		if (heldStack != null) {
			Item item = heldStack.getItem();
			if (Socket.checkItemIsTool(item) && !player.isSneaking()) {
				// return before activate is called if the item right clicked causes a rotation anyway // FIXME reimplement
				//if ((IntegratedCircuits.isBPAPIThere && item instanceof com.bluepowermod.api.misc.IScrewdriver)
//				        || (IntegratedCircuits.isBCToolsAPIThere && item instanceof buildcraft.api.tools.IToolWrench)) {
//					return false;
		//		}
			}
		}

		return te.getSocket().activate(player,
				new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, blockPos),
				heldStack);
	}

	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState state) {
		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);
		List<ItemStack> drops = Lists.newArrayList();
		te.getSocket().addDrops(drops);
		for (ItemStack stack : drops)
			MiscUtils.dropItem(world, stack, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		te.getSocket().onRemoved();
	}


/*
	@Override
	public boolean renderAsNormalBlock(IBlockState blockState) {
		return false;
	}*/

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}
/*
	@Override
	public int getRenderType() {
		return Constants.GATE_RENDER_ID;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {
	}*/

	@Override
	public TileEntity createTileEntity(World world, IBlockState blockState) {
		return new TileEntitySocket();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean canProvidePower(IBlockState blockState) {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState blockState, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		side = GateIO.vanillaToSide(side);

		TileEntitySocket te = (TileEntitySocket) world.getTileEntity(blockPos);
		ISocket socket = te.getSocket();

		if ((side & 6) == (socket.getSide() & 6))
			return false;
		int rel = socket.getSideRel(side);

		return socket.getConnectionTypeAtSide(rel).isRedstone();
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing side) {
		return getStrongPower(blockState, blockAccess, blockPos, side);
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing side) {
		side ^= 1;

		TileEntitySocket te = (TileEntitySocket) blockAccess.getTileEntity(x, y, z);
		ISocket socket = te.getSocket();

		if ((side & 6) == (socket.getSide() & 6))
			return 0;
		int rot = socket.getSideRel(side);
		if (!socket.getConnectionTypeAtSide(side).isRedstone())
			return 0;

		return socket.getRedstoneOutput(rot);
	}
}
