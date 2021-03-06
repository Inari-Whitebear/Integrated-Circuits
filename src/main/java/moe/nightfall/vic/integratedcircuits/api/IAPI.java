package moe.nightfall.vic.integratedcircuits.api;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IAPI {
	public ISocket getSocketAt(World world, BlockPos pos, EnumFacing side);

	public void registerSocketProvider(ISocketProvider provider);

	public ByteBuf getWriteStream(World world, BlockPos pos, EnumFacing side);

	public IGateRegistry getGateRegistry();

	public int updateRedstoneInput(ISocket socket, EnumFacing side);

	public byte[] updateBundledInput(ISocket socket, EnumFacing side);
}
