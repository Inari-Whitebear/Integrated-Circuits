package vic.mod.integratedcircuits.client;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TileEntitySemiTransparentRenderer extends TileEntitySpecialRenderer
{
	protected int renderPass;
	
	public int getCurrentRenderPass()
	{
		return renderPass;
	}
	
	protected void addToRenderQueue(int x, int y, int z)
	{
		ClientProxy.stRenderer.addToRenderQueue(x, y, z);
	}
}
