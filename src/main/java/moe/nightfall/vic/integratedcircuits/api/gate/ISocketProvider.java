package moe.nightfall.vic.integratedcircuits.api.gate;

import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISocketProvider {
	public ISocket getSocketAt(World world, BlockPos pos, EnumFacing facing);
}
