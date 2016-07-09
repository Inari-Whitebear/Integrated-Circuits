package moe.nightfall.vic.integratedcircuits.misc;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderManager {

    private float[] setColor = new float[] {0, 0, 0, 0};
    private boolean started = false;
    private VertexFormat currentFormat;
    private int currentMode;

    private RenderManager() {

    }

    private static RenderManager instance = new RenderManager();

    public static RenderManager getInstance() {
        return instance;
    }

    public void setColor(float r, float g, float b, float a) {
        setColor[0] = r;
        setColor[1] = g;
        setColor[2] = b;
        setColor[3] = a;
    }

    public void setColor(float[] color) {
        setColor(color[0], color[1], color[2], color[3]);
    }

    public void startDraw(int mode, VertexFormat vertexFormat) {
        started = true;
        currentFormat = vertexFormat;
        currentMode = mode;
        Tessellator.getInstance().getBuffer().begin(mode, vertexFormat);
    }

    public void startDrawQuads(VertexFormat vertexFormat) {
        startDraw(GL11.GL_QUADS, vertexFormat);
    }

    public void addQuad(double x, double y, double u, double v, double w, double h) {
        addQuad(x, y, u, v, w, h, 0);
    }

    public void addQuad(double x, double y, double u, double v, double w, double h, float r, float g, float b, float a) {
        addQuad(x, y, u, v, w, h, 0, r, g, b, a);
    }


    public void addQuad(double x, double y, double u, double v, double w, double h, double rotation) {
        addQuad(x, y, u, v, w, h, w, h, 256, 256, rotation, setColor[0], setColor[1], setColor[2], setColor[3]);
    }

    public void addQuad(double x, double y, double u, double v, double w, double h, double rotation, float r, float g, float b, float a) {
        addQuad(x, y, u, v, w, h, w, h, 256, 256, rotation, r, g, b, a);
    }

    public void addQuad(double x, double y, double u, double v, double w, double h, double w2, double h2,
                        double tw, double th, double rotation) {
        addQuad(x, y, u, v, w, h, w2, h2, tw, th, rotation, setColor[0], setColor[1], setColor[2], setColor[3]);
    }

    public void addQuad(double x, double y, double u, double v, double w, double h, double w2, double h2,
                               double tw, double th, double rotation, float r, float g, float b, float a) {
        double d1, d2, d3, d4;
        double scalew = 1 / tw;
        double scaleh = 1 / th;
        Tessellator tes = Tessellator.getInstance();
        VertexBuffer buffer = tes.getBuffer();

        d1 = u + 0;
        d2 = u + w2;

        if (rotation == 1) {
            d3 = v + h2;
            d4 = v + 0;

            buffer.pos(x + w, y + h, 0).tex(d2 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + 0, 0).tex(d1 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + 0, y + 0, 0).tex(d1 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + 0, y + h, 0).tex(d2 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
        } else if (rotation == 2) {
            d3 = v + h2;
            d4 = v + 0;

            buffer.pos(x + 0, y + h, 0).tex(d2 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, 0).tex(d1 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + 0, 0).tex(d1 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + 0, y + 0, 0).tex(d2 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
        } else if (rotation == 3) {
            d3 = v + 0;
            d4 = v + h2;

            buffer.pos(x + w, y + h, 0).tex(d1 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + 0, 0).tex(d2 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + 0, y + 0, 0).tex(d2 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + 0, y + h, 0).tex(d1 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
        } else {
            d3 = v + 0;
            d4 = v + h2;

            buffer.pos(x + 0, y + h, 0).tex(d1 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + h, 0).tex(d2 * scalew, d4 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + w, y + 0, 0).tex(d2 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
            buffer.pos(x + 0, y + 0, 0).tex(d1 * scalew, d3 * scaleh).color(r, g, b, a).endVertex();
        }
    }

    public void addVertex(double x, double y, double z) {
        Tessellator.getInstance().getBuffer().pos(x, y, z).endVertex();
    }

    public void draw() {
        Tessellator.getInstance().draw();
        started = false;
        currentMode = -1;
        currentFormat = null;
    }

}
