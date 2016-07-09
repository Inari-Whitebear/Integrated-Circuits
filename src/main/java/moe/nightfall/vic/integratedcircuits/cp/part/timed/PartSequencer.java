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
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;

public class PartSequencer extends PartDelayedAction implements IConfigurableDelay {
	public final IntProperty PROP_OUTPUT_SIDE = new IntProperty("OUTPUT_SIDE", stitcher, 3);
	public final IntProperty PROP_DELAY = new IntProperty("DELAY", stitcher, 255);

	@Override
	protected int getDelay(Vec2i pos, ICircuit parent) {
		return getConfigurableDelay(pos, parent);
	}

	@Override
	public int getConfigurableDelay(Vec2i pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_DELAY);
	}

	@Override
	public void setConfigurableDelay(Vec2i pos, ICircuit parent, int delay) {
		setProperty(pos, parent, PROP_DELAY, delay);
	}

	@Override
	public void onPlaced(Vec2i pos, ICircuit parent) {
		setConfigurableDelay(pos, parent, 10);
		setDelay(pos, parent, true);
		super.onPlaced(pos, parent);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(3, 1);
	}

	@Override
	public void onDelay(Vec2i pos, ICircuit parent) {
		cycleProperty(pos, parent, PROP_OUTPUT_SIDE);
		setDelay(pos, parent, true);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		return MiscUtils.getDirection(getProperty(pos, parent, PROP_OUTPUT_SIDE))
				== toInternal(pos, parent, side);
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
