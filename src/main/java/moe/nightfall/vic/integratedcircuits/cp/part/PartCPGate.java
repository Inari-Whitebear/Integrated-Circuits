package moe.nightfall.vic.integratedcircuits.cp.part;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.misc.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;

import com.google.common.collect.Lists;

/** Rotateable Part **/
public abstract class PartCPGate extends CircuitPart {
	public final IntProperty PROP_ROTATION = new IntProperty("ROTATION", stitcher, 3);

	public final int getRotation(Vec2i pos, ICircuit parent) {
		return getProperty(pos, parent, PROP_ROTATION);
	}

	public final void setRotation(Vec2i pos, ICircuit parent, int rotation) {
		setProperty(pos, parent, PROP_ROTATION, rotation);
		scheduleInputChange(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public Category getCategory() {
		return Category.MISC;
	}

	@Override
	public void onClick(Vec2i pos, ICircuit parent, int button, boolean ctrl) {
		if (button == 0 && !ctrl) {
			cycleProperty(pos, parent, PROP_ROTATION);
			scheduleInputChange(pos, parent);
			notifyNeighbours(pos, parent);
		}
	}

	public EnumFacing toInternal(Vec2i pos, ICircuit parent, EnumFacing dir) {
		return MiscUtils.rotn(dir, -getRotation(pos, parent));
	}

	public EnumFacing toExternal(Vec2i pos, ICircuit parent, EnumFacing dir) {
		return MiscUtils.rotn(dir, getRotation(pos, parent));
	}

	@Override
	public ArrayList<String> getInformation(Vec2i pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = Lists.newArrayList();
		EnumFacing rot = MiscUtils.getDirection(getRotation(pos, parent));
		text.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + MiscUtils.getLocalizedDirection(rot));
		if (edit && !ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.rotate"));
		return text;
	}

	@Override
	public void getCraftingCost(CraftingAmount cost, CircuitData parent, Vec2i pos) {
		cost.add(new ItemAmount(Items.REDSTONE, 0.048));
		cost.add(new ItemAmount(Content.itemSiliconDrop, 0.1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartGate(pos, parent, this, x, y, type);

		Vec2i textureOffset = getTextureOffset(pos, parent, x, y, type);
		RenderManager.getInstance().addQuad(x, y, textureOffset.x * 16, textureOffset.y * 16, 16, 16, this.getRotation(pos, parent));
	}

	@SideOnly(Side.CLIENT)
	public abstract Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type);

}
