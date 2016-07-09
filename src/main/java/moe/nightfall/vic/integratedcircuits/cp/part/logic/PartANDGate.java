package moe.nightfall.vic.integratedcircuits.cp.part.logic;

import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.Part3I1O;

public class PartANDGate extends Part3I1O {
	@Override
	protected void calcOutput(Vec2i pos, ICircuit parent) {
		EnumFacing s3 = toExternal(pos, parent, EnumFacing.SOUTH);
		EnumFacing s4 = toExternal(pos, parent, EnumFacing.EAST);
		EnumFacing s5 = s4.getOpposite();

		setOutput(
				pos,
				parent,
				(!canConnectToSide(pos, parent, s3) || getInputFromSide(pos, parent, s3))
						&& (!canConnectToSide(pos, parent, s4) || getInputFromSide(pos, parent, s4))
						&& (!canConnectToSide(pos, parent, s5) || getInputFromSide(pos, parent, s5)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(7, 0);
	}
}