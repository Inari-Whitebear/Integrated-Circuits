package moe.nightfall.vic.integratedcircuits.gate;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

public abstract class Gate implements IGate {
	protected ISocketBase provider;

	@Override
	public ISocketBase getProvider() {
		return provider;
	}

	@Override
	public void setProvider(ISocketBase provider) {
		this.provider = provider;
	}

	@Override
	public void preparePlacement(EntityPlayer player, ItemStack stack) {
	}

	@Override
	public void load(NBTTagCompound tag) {
	}

	@Override
	public void save(NBTTagCompound tag) {
	}

	@Override
	public void readDesc(NBTTagCompound tag) {
	}

	@Override
	public void writeDesc(NBTTagCompound tag) {
	}

	@Override
	public void read(byte discr, ByteBuf packet) {
	}

	@Override
	public boolean activate(EntityPlayer player, RayTraceResult hit, ItemStack item) {
		return false;
	}

	@Override
	public void onActivatedWithScrewdriver(EntityPlayer player, RayTraceResult hit, ItemStack item) {
	}

	@Override
	public void onRotated() {
	}

	@Override
	public void onAdded() {
		notifyChanges();
	}

	@Override
	public void onRemoved() {
		provider.notifyBlocksAndChanges();
	}

	@Override
	public void onMoved() {
		notifyChanges();
	}

	private void notifyChanges() {
		if (!provider.getWorld().isRemote)
			provider.updateInput();
		provider.notifyBlocksAndChanges();
	}

	@Override
	public ItemStack pickItem(RayTraceResult hit) {
		return getItemStack();
	}

	@Override
	public void onNeighborChanged() {
	}

	@Override
	public void update() {
	}

	@Override
	public void scheduledTick() {
	}

	@Override
	public void updateInputPre() {
	}

	@Override
	public void updateInputPost() {
	}

	@Override
	public boolean hasComparatorInputAtSide(EnumFacing side) {
		return false;
	}
}
