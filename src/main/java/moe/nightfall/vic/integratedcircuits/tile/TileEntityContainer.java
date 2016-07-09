package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

public abstract class TileEntityContainer extends TileEntity implements IInventory {
	public EnumFacing rotation;
	public int playersUsing;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		rotation = EnumFacing.values()[compound.getInteger("rotation")];
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("rotation", rotation.getIndex());
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		return new SPacketUpdateTileEntity(pos, 0, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound compound = pkt.getNbtCompound();
		readFromNBT(compound);
	}

	@Override
	public boolean receiveClientEvent(int id, int par) {
		if (id == 0) {
			playersUsing = par;
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			return true;
		}
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (!worldObj.isRemote)
			worldObj.addBlockEvent(pos, getBlockType(), 0, ++playersUsing);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		if (!worldObj.isRemote)
			worldObj.addBlockEvent(pos, getBlockType(), 0, --playersUsing);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return pos.distanceSqToCenter(player.posX, player.posY, player.posZ) < 64; //FIXME is this right?
	}

	public void onSlotChange(int id) {

	}

	// IInventory defaults

	@Override
	public ItemStack decrStackSize(int id, int size) {
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int id, ItemStack stack) {
		return true;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	public boolean rotate() {

		this.rotation = MiscUtils.rot(rotation);
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); // FIXME flags, states
		return true;
	}
}
