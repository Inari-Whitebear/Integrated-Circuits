package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCircuitStorage extends Block {
	protected BlockCircuitStorage() {
		super(Material.IRON);
		setUnlocalizedName(Constants.MOD_ID + ".circuitstorage");
		setRegistryName("circuitstorage");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return false;
	}
}
