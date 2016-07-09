package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;

public class PartMultiplexer extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.MISC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(0, 1);
	}

	@Override
	protected void calcOutput(Vec2i pos, ICircuit parent) {
		if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH)))
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.WEST)));
		else
			setOutput(pos, parent, getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.EAST)));
	}

	@Override
	protected boolean hasOutputToSide(Vec2i pos, ICircuit parent, EnumFacing fd) {
		return fd == EnumFacing.NORTH;
	}
}
