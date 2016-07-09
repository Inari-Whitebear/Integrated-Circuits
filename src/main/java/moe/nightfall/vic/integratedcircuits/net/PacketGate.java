package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class PacketGate<T extends AbstractPacket<T>> extends PacketTileEntity<T> {
	protected EnumFacing facing;

	public PacketGate() {
	}

	public PacketGate(ISocketBase part) {
		BlockPos pos = part.getPos();
		this.xCoord = pos.getX();
		this.yCoord = pos.getY();
		this.zCoord = pos.getZ();
		this.facing = part.getSide();
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		facing = EnumFacing.getFront(buffer.readInt());
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeInt(facing.getIndex());
	}

	protected IGate getPart(World world) {
		return IntegratedCircuitsAPI.getSocketAt(world, new BlockPos(xCoord, yCoord, zCoord), facing).getGate();
	}
}
