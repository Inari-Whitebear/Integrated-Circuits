package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import moe.nightfall.vic.integratedcircuits.misc.RenderManager;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;

public class PartTransparentLatch extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2i pos, ICircuit parent) {
		if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH))) {
			setProperty(pos, parent, PROP_OUT,
					getInputFromSide(pos, parent,
						toExternal(pos, parent, EnumFacing.WEST)));
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.NORTH || s2 == EnumFacing.EAST)
			return getProperty(pos, parent, PROP_OUT);
		return false;
	}

	@Override
	public void renderPart(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		RenderManager.getInstance().addQuad(x, y, 9 * 16, 16, 16, 16, this.getRotation(pos, parent));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(9, 1);
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}
}
