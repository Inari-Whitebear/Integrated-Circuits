package moe.nightfall.vic.integratedcircuits.api.gate;

import net.minecraft.util.math.BlockPos;

public abstract class GateIOProvider {
	public ISocket socket;

	public byte[] calculateBundledInput(int side, int rotation, int abs, BlockPos offset) {
		return null;
	}

	public int calculateRedstoneInput(int side, int rotation, int abs, BlockPos offset) {
		return 0;
	}
}
