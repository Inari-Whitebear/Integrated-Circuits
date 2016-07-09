package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartCPGate;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import net.minecraft.client.resources.I18n;

public class PartToggleLatch extends PartCPGate {
	public final BooleanProperty PROP_OUT = new BooleanProperty("OUT", stitcher);
	public final BooleanProperty PROP_OLD_NORTH = new BooleanProperty("OLD_NORTH", stitcher);
	public final BooleanProperty PROP_OLD_SOUTH = new BooleanProperty("OLD_SOUTH", stitcher);

	@Override
	public void onClick(Vec2i pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			invertProperty(pos, parent, PROP_OUT);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2i pos, ICircuit parent) {
		EnumFacing north = toExternal(pos, parent, EnumFacing.NORTH);
		boolean northIn = getInputFromSide(pos, parent, north);
		boolean southIn = getInputFromSide(pos, parent, north.getOpposite());
		if (northIn && !getProperty(pos, parent, PROP_OLD_NORTH))
			invertProperty(pos, parent, PROP_OUT);
		if (southIn && !getProperty(pos, parent, PROP_OLD_SOUTH))
			invertProperty(pos, parent, PROP_OUT);
		notifyNeighbours(pos, parent);
		setProperty(pos, parent, PROP_OLD_NORTH, northIn);
		setProperty(pos, parent, PROP_OLD_SOUTH, southIn);
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.EAST)
			return getProperty(pos, parent, PROP_OUT);
		if (s2 == EnumFacing.WEST)
			return !getProperty(pos, parent, PROP_OUT);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(8, 1);
	}

	@Override
	public ArrayList<String> getInformation(Vec2i pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.toggle"));
		return text;
	}

	@Override
	public Category getCategory() {
		return Category.LATCH;
	}
}
