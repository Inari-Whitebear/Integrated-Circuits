package moe.nightfall.vic.integratedcircuits.cp.part.timed;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.ItemAmount;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;

public class PartStateCell extends PartDelayedAction implements IConfigurableDelay {
	public final IntProperty PROP_DELAY = new IntProperty("DELAY", stitcher, 255);
	private final BooleanProperty PROP_OUT_WEST = new BooleanProperty("OUT_WEST", stitcher);
	private final BooleanProperty PROP_OUT_NORTH = new BooleanProperty("OUT_NORTH", stitcher);

	@Override
	public int getConfigurableDelay(Vec2i pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_DELAY);
	}

	@Override
	public void setConfigurableDelay(Vec2i pos, ICircuit parent, int delay) {
		setProperty(pos, parent, PROP_DELAY, delay);
	}

	@Override
	protected int getDelay(Vec2i pos, ICircuit parent) {
		if (getProperty(pos, parent, PROP_OUT_NORTH))
			return 2;
		return getConfigurableDelay(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.WEST && getProperty(pos, parent, PROP_OUT_WEST))
			return true;
		if (s2 == EnumFacing.NORTH && getProperty(pos, parent, PROP_OUT_NORTH))
			return true;
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(4, 1);
	}

	@Override
	public void onDelay(Vec2i pos, ICircuit parent) {
		if (getProperty(pos, parent, PROP_OUT_NORTH))
			setProperty(pos, parent, PROP_OUT_NORTH, false);
		else {
			setProperty(pos, parent, PROP_OUT_WEST, false);
			setProperty(pos, parent, PROP_OUT_NORTH, true);
			setDelay(pos, parent, true);
		}
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onPlaced(Vec2i pos, ICircuit parent) {
		setProperty(pos, parent, PROP_DELAY, 20);
		super.onPlaced(pos, parent);
	}

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		scheduleTick(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2i pos, ICircuit parent) {
		super.onScheduledTick(pos, parent);
		if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.SOUTH))) {
			setProperty(pos, parent, PROP_OUT_WEST, true);
			setProperty(pos, parent, PROP_OUT_NORTH, false);
			setDelay(pos, parent, false);
			notifyNeighbours(pos, parent);
		} else if (getProperty(pos, parent, PROP_OUT_WEST)) {
			if (getInputFromSide(pos, parent, toExternal(pos, parent, EnumFacing.EAST)))
				setDelay(pos, parent, false);
			else if (!isDelayActive(pos, parent))
				setDelay(pos, parent, true);
		}
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2i pos) {
		cost.add(new ItemAmount(Items.REDSTONE, 0.15));
		cost.add(new ItemAmount(Items.GLOWSTONE_DUST, 0.1));
	}

	@Override
	public ArrayList<String> getInformation(Vec2i pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.delay"));
		return text;
	}
}
