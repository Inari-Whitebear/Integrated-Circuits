package moe.nightfall.vic.integratedcircuits.client.gui.cad;

import moe.nightfall.vic.integratedcircuits.misc.RenderManager;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.cp.part.PartTunnel;
import moe.nightfall.vic.integratedcircuits.cp.part.timed.IConfigurableDelay;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBChangePart;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class EditHandler extends CADHandler {

	@Override
	public void renderCADCursor(GuiCAD parent, double mouseX, double mouseY, int gridX, int gridY, CircuitData cdata) {
		if (parent.drag) {
			GL11.glColor4f(0F, 0F, 1F, 1F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			RenderManager rm = RenderManager.getInstance();
			//rm.startDrawQuads(DefaultVertexFormats.POSITION); FIXME is this needed? addline calls it
			if (cdata.getPart(new Vec2i(parent.endX, parent.endY)) instanceof PartTunnel) {
				RenderUtils.addLine(parent.startX + 0.5, parent.startY + 0.5, parent.endX + 0.5, parent.endY + 0.5, 0.25);
			} else {
				RenderUtils.addLine(parent.startX + 0.5, parent.startY + 0.5, mouseX, mouseY, 0.25);
			}
			rm.draw();

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(0.6F, 0.6F, 0.6F, 0.7F);
		}
	}

	@Override
	public void onMouseDown(GuiCAD parent, int mx, int my, int button) {
		int gridX = (int) parent.boardAbs2RelX(mx);
		int gridY = (int) parent.boardAbs2RelY(my);
		int w = parent.getBoardSize();

		if (gridX > 0 && gridY > 0 && gridX < w - 1 && gridY < w - 1 && !GuiScreen.isShiftKeyDown()) {
			Vec2i pos = new Vec2i(gridX, gridY);
			CircuitPart cp = parent.getCircuitData().getPart(pos);
			if (cp instanceof IConfigurableDelay && parent.isCtrlKeyDown()) {
				parent.timedPart = new CircuitRenderWrapper(parent.tileentity.getCircuitData(), cp, pos);
				parent.labelTimed.setText(String.format("Current delay: %s ticks",
						((IConfigurableDelay) cp).getConfigurableDelay(pos, parent.tileentity)));
				parent.callbackTimed.display();
			} else if (cp instanceof PartTunnel) {
				parent.startX = gridX;
				parent.startY = gridY;
				parent.drag = true;
			} else {
				CommonProxy.networkWrapper.sendToServer(new PacketPCBChangePart(gridX, gridY, button, parent.isCtrlKeyDown(), parent.tileentity.getPos().getX(),
						parent.tileentity.getPos().getY(), parent.tileentity.getPos().getZ()));
			}
		}
	}

	@Override
	public void onMouseUp(GuiCAD parent, int mx, int my, int button) {
		if (parent.drag) {
			int w = parent.getCircuitData().getSize();

			if (parent.endX > 0 && parent.endY > 0 && parent.endX < w - 1 && parent.endY < w - 1) {

				Vec2i first = new Vec2i(parent.startX, parent.startY);
				Vec2i second = new Vec2i(parent.endX, parent.endY);

				if (parent.getCircuitData().getPart(second) instanceof PartTunnel && !first.equals(second)) {

					PartTunnel pt = CircuitPart.getPart(PartTunnel.class);
					PacketPCBChangePart packet = new PacketPCBChangePart(true, parent.tileentity.getPos().getX(), parent.tileentity.getPos().getY(), parent.tileentity.getPos().getZ());
					
					if (pt.isConnected(pt.getConnectedPos(first, parent.tileentity))) {	
						Vec2i part = pt.getConnectedPos(first, parent.tileentity);
						packet.add(part, CircuitPart.getId(pt), pt.setConnectedPos(parent.getCircuitData().getMeta(part), new Vec2i(255, 255)));
					}

					if (pt.isConnected(pt.getConnectedPos(second, parent.tileentity))) {
						Vec2i part = pt.getConnectedPos(second, parent.tileentity);
						packet.add(part, CircuitPart.getId(pt), pt.setConnectedPos(parent.getCircuitData().getMeta(part), new Vec2i(255, 255)));
					}

					packet.add(first, CircuitPart.getId(pt), pt.setConnectedPos(parent.getCircuitData().getMeta(first), second));
					packet.add(second, CircuitPart.getId(pt), pt.setConnectedPos(parent.getCircuitData().getMeta(second), first));

					CommonProxy.networkWrapper.sendToServer(packet);
				}
			}
		}
	}

	@Override
	public void remove(GuiCAD parent) {
		super.remove(parent);
	}
}
