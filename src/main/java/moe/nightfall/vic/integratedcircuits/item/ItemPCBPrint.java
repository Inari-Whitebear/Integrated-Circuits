package moe.nightfall.vic.integratedcircuits.item;

import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class ItemPCBPrint extends ItemMap {

	public static ItemStack create(CircuitData cdata) {
		ItemStack stack = new ItemStack(Content.itemPCBPrint);
		stack.setTagCompound(cdata.writeToNBT(new NBTTagCompound()));
		return stack;
	}

	public ItemPCBPrint() {
		ItemBase.register(this, "pcb_print");
		this.setCreativeTab(null);
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return EnumActionResult.SUCCESS;
	}

	@Override
	public MapData getMapData(ItemStack stack, World world) {
		return null;
	}

	@Override
	public void updateMapData(World world, Entity entity, MapData data) {

	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int meta, boolean par5) {

	}

	@Override
	public Packet<?> createMapDataPacket(ItemStack stack, World worldIn, EntityPlayer player) {
		return null;
	}

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {

	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par5) {
		NBTTagCompound comp = stack.getTagCompound();
		if (comp != null) {
			ItemFloppyDisk.addInformation(comp, itemInformation, false);
		} else {
			itemInformation.add(ChatFormatting.RED + "Kill me...");
		}
	}
}
