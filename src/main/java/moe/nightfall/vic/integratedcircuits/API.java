package moe.nightfall.vic.integratedcircuits;

import java.util.List;
import java.util.Set;

import moe.nightfall.vic.integratedcircuits.api.IAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.GateIOProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import moe.nightfall.vic.integratedcircuits.gate.GateRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

public class API implements IAPI {
	private List<ISocketProvider> providerList = Lists.newArrayList();
	private GateRegistry gateRegistry = new GateRegistry();

	@Override
	public ISocket getSocketAt(World world, BlockPos pos, EnumFacing side) {
		ISocket socket;
		for (ISocketProvider provider : providerList) {
			socket = provider.getSocketAt(world, pos, side);
			if (socket != null)
				return socket;
		}
		return null;
	}

	@Override
	public void registerSocketProvider(ISocketProvider provider) {
		providerList.add(provider);
	}

	@Override
	public GateRegistry getGateRegistry() {
		return gateRegistry;
	}

	@Override
	public MCDataOutput getWriteStream(World world, BlockPos pos, int side) {
		return IntegratedCircuits.proxy.addStream(world, pos, side);
	}

	@Override
	public int updateRedstoneInput(ISocket socket, EnumFacing side) {
		EnumFacing rotation = socket.getRotationAbs(side);
		EnumFacing face = socket.getSide();
		EnumFacing abs = face.rotateAround(rotation.getAxis());
		BlockPos pos = socket.getPos().offset(abs);

		int input = 0;

		// Vanilla input
		input = socket.getWorld().getRedstonePower(pos, abs);
		//input = socket.getWorld().getIndirectPowerLevelTo(pos.x, pos.y, pos.z, abs);

		//Comparator input
		if(socket.getConnectionTypeAtSide(side).isRedstone() && hasComparatorInput(socket, pos))
			input = updateComparatorInput(socket, pos, rotation);
		if (input != 0)
			return input;

		// Compatibility to Redstone
		if (input < 15 && socket.getWorld().getBlockState(pos).getBlock() == Blocks.REDSTONE_WIRE)
			input = Math.max(input, socket.getWorld().getBlockState(pos));
		if (input != 0)
			return input;

		Set<GateIOProvider> providerList = gateRegistry.getIOProviderList(socket.getWrapper().getClass());
		for (GateIOProvider provider : providerList) {
			provider.socket = socket;
			input = provider.calculateRedstoneInput(side, rotation, abs, pos);
			if (input != 0)
				return input;
		}
		return input;
	}

	public boolean hasComparatorInput(ISocket socket, BlockPos pos)
	{
		IBlockState b = socket.getWorld().getBlockState(pos);
		return b.hasComparatorInputOverride();
	}

	public int updateComparatorInput(ISocket socket, BlockPos pos)
	{
		IBlockState b = socket.getWorld().getBlockState(pos);
		if(b != null && b.hasComparatorInputOverride())
			return b.getComparatorInputOverride(socket.getWorld(), pos);
		return 0;
	}

	@Override
	public byte[] updateBundledInput(ISocket socket, EnumFacing side) {
		EnumFacing rotation = socket.getRotationAbs(side);
		EnumFacing face = socket.getSide();
		EnumFacing abs = face.rotateAround(rotation.getAxis());
		BlockPos pos = socket.getPos().offset(abs);

		byte[] input = updateBundledInputNative(socket, rotation, side, pos);
		if (input != null)
			return input;

		Set<GateIOProvider> providerList = gateRegistry.getIOProviderList(socket.getWrapper().getClass());
		for (GateIOProvider provider : providerList) {
			provider.socket = socket;
			input = provider.calculateBundledInput(side, rotation, abs, pos);
			if (input != null)
				return input;
		}
		return input == null ? new byte[16] : input;
	}

	public byte[] updateBundledInputNative(ISocket socket, EnumFacing rotation, EnumFacing side, BlockPos pos) {
		ISocket neighbour = getSocketAt(socket.getWorld(), pos, socket.getSide());
		if (neighbour != null)
			return neighbour.getOutput()[side.getOpposite().getHorizontalIndex()];
		return null;
	}
}
