package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;

public class PartPulseFormer extends PartDelayedAction {
	public BooleanProperty PROP_OLD_IN = new BooleanProperty("OLD_IN", stitcher);

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing f2 = toInternal(pos, parent, side);
		if (f2 != EnumFacing.NORTH)
			return false;
		return isDelayActive(pos, parent);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(6, 1);
	}

	@Override
	public boolean canConnectToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing f2 = toInternal(pos, parent, side);
		return f2 == EnumFacing.NORTH || f2 == EnumFacing.SOUTH;
	}

	@Override
	public void onScheduledTick(Vec2i pos, ICircuit parent) {
		super.onScheduledTick(pos, parent);
		boolean newIn = getInputFromSide(pos, parent,
				toExternal(pos, parent, EnumFacing.SOUTH));
		if (newIn && !getProperty(pos, parent, PROP_OLD_IN)) {
			setDelay(pos, parent, true);
			notifyNeighbours(pos, parent);
		}
		setProperty(pos, parent, PROP_OLD_IN, newIn);
	}

	@Override
	protected int getDelay(Vec2i pos, ICircuit parent) {
		return 2;
	}

	@Override
	public void onDelay(Vec2i pos, ICircuit parent) {
		notifyNeighbours(pos, parent);
	}
}
