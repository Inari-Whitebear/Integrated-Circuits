package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.misc.RenderManager;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.Tessellator;

public class PartIOBit extends CircuitPart {
	public final IntProperty PROP_ROTATION = new IntProperty("ROTATION", stitcher, 3);
	public final IntProperty PROP_FREQUENCY = new IntProperty("FREQUENCY", stitcher, 15);

	@SideOnly(Side.CLIENT)
	@Override
	public void renderPart(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		int freq = this.getFrequency(pos, parent);
		int rot = this.getRotation(pos, parent);
		RenderManager rm = new RenderManager();

		if (type == CircuitPartRenderer.EnumRenderType.WORLD_16x) {
			rm.setColor(188, 167, 60, 255);
			rm.addQuad(x, y, 6 * 16, 3 * 16, 16, 16, rot);
		} else {
			rm.setColor(1F, 1F, 1F, 1F);
			rm.addQuad(x, y, 2 * 16, 2 * 16, 16, 16, rot);
			if (this.isPowered(pos, parent) && type == CircuitPartRenderer.EnumRenderType.GUI)
				RenderUtils.applyColorIRGBA(rm, Config.colorGreen);
			else
				RenderUtils.applyColorIRGBA(rm, Config.colorGreen, 0.4F);
			rm.addQuad(x, y, 4 * 16, 2 * 16, 16, 16, rot);
			if (type == CircuitPartRenderer.EnumRenderType.GUI) {
				if (parent.getCircuitData().getProperties().getModeAtSide(getRotation(pos, parent)).isAnalog())
					rm.setColorRGBA_I((getFrequency(pos, parent) * 17) << 20, 255);
				else rm.setColorRGBA_I(MapColor.getMapColorForBlockColored(freq).colorValue, 255);
				rm.addQuad(x, y, 3 * 16, 2 * 16, 16, 16, rot);
			}
		}
	}

	public final EnumFacing getRotation(Vec2i pos, ICircuit parent) {
		return EnumFacing.getFront(getProperty(pos, parent, PROP_ROTATION));
	}

	public final void setRotation(Vec2i pos, ICircuit parent, EnumFacing rotation) {
		setProperty(pos, parent, PROP_ROTATION, rotation.getIndex());
	}

	public final int getFrequency(Vec2i pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_FREQUENCY);
	}

	public final void setFrequency(Vec2i pos, ICircuit parent, int frequency) {
		setProperty(pos, parent, PROP_FREQUENCY, frequency);
	}

	public final void updateExternalOutput(Vec2i pos, ICircuit parent) {
		EnumFacing dir = getRotation(pos, parent);
		parent.setOutputToSide(dir, getFrequency(pos, parent),
				getInputFromSide(pos, parent, dir.getOpposite()));
	}

	@Override
	public boolean canConnectToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing dir = getRotation(pos, parent);
		return side == dir.getOpposite();
	}

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		scheduleTick(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public void onScheduledTick(Vec2i pos, ICircuit parent) {
		updateExternalOutput(pos, parent);
		notifyNeighbours(pos, parent); // Implicit updateExternalInput
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing dir = getRotation(pos, parent);
		if (side == dir.getOpposite())
			return parent.getInputFromSide(dir, getFrequency(pos, parent));
		else
			return false;
	}

	public boolean isPowered(Vec2i pos, ICircuit parent) {
		EnumFacing dir = getRotation(pos, parent).getOpposite();
		return getOutputToSide(pos, parent, dir)
				|| getNeighbourOnSide(pos, parent, dir).getOutputToSide(pos.offset(dir), parent, dir.getOpposite());
	}
}
