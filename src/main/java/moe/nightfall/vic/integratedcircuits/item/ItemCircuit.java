package moe.nightfall.vic.integratedcircuits.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

public class ItemCircuit extends ItemGate {
	public ItemCircuit() {
		super("circuit");
		setMaxStackSize(1);
		setCreativeTab(null);
		setHasIcon(false);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.getTagCompound() == null)
			return I18n.translateToLocalFormatted(getUnlocalizedName() + ".name", "INVALID!");
		String name = stack.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name");
		return I18n.translateToLocalFormatted(getUnlocalizedName() + ".name", name);
	}
/*
	@Override
	public String getGateID(ItemStack stack, EntityPlayer player, BlockPos pos) {
		return "circuit";
	}
	
	@Override
	public Boolean usedUpOnPlace(EntityPlayer player) {
		return true;
	}*/
}
