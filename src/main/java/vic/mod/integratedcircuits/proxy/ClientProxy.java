package vic.mod.integratedcircuits.proxy;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.world.WorldEvent;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.DiskDrive;
import vic.mod.integratedcircuits.DiskDrive.IDiskDrive;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.ItemLaserRenderer;
import vic.mod.integratedcircuits.client.Part7SegmentRenderer;
import vic.mod.integratedcircuits.client.PartCircuitRenderer;
import vic.mod.integratedcircuits.client.Resources;
import vic.mod.integratedcircuits.client.SemiTransparentRenderer;
import vic.mod.integratedcircuits.client.ShaderHelper;
import vic.mod.integratedcircuits.client.TileEntityAssemblerRenderer;
import vic.mod.integratedcircuits.client.TileEntityGateRenderer;
import vic.mod.integratedcircuits.client.TileEntityPCBLayoutRenderer;
import vic.mod.integratedcircuits.client.gui.Gui7Segment;
import vic.mod.integratedcircuits.gate.Part7Segment;
import vic.mod.integratedcircuits.misc.RenderUtils;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static SemiTransparentRenderer stRenderer;
	public static Resources resources;
	
	public static int clientTicks;
	public static PartCircuitRenderer circuitRenderer;
	public static Part7SegmentRenderer segmentRenderer;
	
	@Override
	public void initialize() 
	{
		super.initialize();
		stRenderer = new SemiTransparentRenderer();
		TileEntityAssemblerRenderer.fboArray = new LinkedList<Framebuffer>();
		ShaderHelper.loadShaders();
		
		Constants.GATE_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
		
		TileEntityGateRenderer gateRenderer = new TileEntityGateRenderer();
		RenderingRegistry.registerBlockHandler(Constants.GATE_RENDER_ID, gateRenderer);
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPCBLayout.class, new TileEntityPCBLayoutRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssembler.class, new TileEntityAssemblerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGate.class, gateRenderer);
		
		MinecraftForgeClient.registerItemRenderer(IntegratedCircuits.itemLaser, new ItemLaserRenderer());
	}
	
	@Override
	public void preInitialize() 
	{
		super.preInitialize();
		
		circuitRenderer = new PartCircuitRenderer();
		segmentRenderer = new Part7SegmentRenderer();
		resources = new Resources();
	}
	
	@SubscribeEvent
	public void onBlockHighlight(DrawBlockHighlightEvent event)
	{
		World world = event.player.worldObj;
		if((event.target == null) || (event.target.typeOfHit != MovingObjectType.BLOCK)) return;
		int x = event.target.blockX;
		int y = event.target.blockY;
		int z = event.target.blockZ;
		
		AxisAlignedBB box = null;
		Block block = world.getBlock(x, y, z);
		TileEntity tileEntity = world.getTileEntity(x, y, z);
				
		if(tileEntity instanceof IDiskDrive)
			box = DiskDrive.getDiskDriveBoundingBox((IDiskDrive)tileEntity, x, y, z, event.target.hitVec);
		if(tileEntity instanceof TileEntityAssembler && box == null)
			box = getLaserBoundingBox((TileEntityAssembler)tileEntity, x, y, z, event.player, event.partialTicks).getLeft();
		if(box == null) return;
		
		double xOff = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * event.partialTicks;
		double yOff = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * event.partialTicks;
		double zOff = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * event.partialTicks;
		box = box.offset(-xOff, -yOff, -zOff).expand(0.002, 0.002, 0.002);
        
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);

		RenderGlobal.drawOutlinedBoundingBox(box, -1);

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		
		event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		try {
			for(Framebuffer buf : TileEntityAssemblerRenderer.fboArray)
				buf.deleteFramebuffer();
		} catch (RuntimeException e) {}
		TileEntityAssemblerRenderer.fboArray.clear();
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui == null || !gui.doesGuiPauseGame()) clientTicks++;
		}
	}
	
	Framebuffer fbo;
	Framebuffer fbo2;
	
	//Don't even look at what's coming now. Not related at all
	
	public void renderPlayer(float partial, RenderGlobal context)
	{
		//Cirno
		try {
			Minecraft mc = Minecraft.getMinecraft();
			Framebuffer mcfbo = mc.getFramebuffer();
			
			if(OpenGlHelper.isFramebufferEnabled())
			{
				if(fbo == null || fbo2 == null)
				{
					fbo = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
					fbo.setFramebufferColor(0, 0, 0, 1);
					fbo.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
					fbo.unbindFramebuffer();
					fbo2 = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
					fbo2.setFramebufferColor(0, 0, 0, 1);
					fbo2.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
					fbo2.unbindFramebuffer();
					mcfbo.bindFramebuffer(false);
				}
				else if(mc.displayWidth != fbo.framebufferWidth || mc.displayHeight != fbo.framebufferHeight)
				{
					fbo.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
					fbo.unbindFramebuffer();
					fbo2.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
					fbo2.unbindFramebuffer();
					mcfbo.bindFramebuffer(false);
				}
				
				OpenGlHelper.func_153188_a(OpenGlHelper.field_153198_e, OpenGlHelper.field_153200_g, 3553, fbo.framebufferTexture, 0);
				
				GL11.glClearColor(0, 0, 0, 1);
			}
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);

			WorldClient world = mc.theWorld;
			EntityLivingBase entityLiving = mc.renderViewEntity;
	
			double x = entityLiving.prevPosX + (entityLiving.posX - entityLiving.prevPosX) * partial;
			double y = entityLiving.prevPosY + (entityLiving.posY - entityLiving.prevPosY) * partial;
			double z = entityLiving.prevPosZ + (entityLiving.posZ - entityLiving.prevPosZ) * partial;
	
			Frustrum frustrum = new Frustrum();
			frustrum.setPosition(x, y, z);
	
			boolean found = false;
			List<Entity> list = world.loadedEntityList;
			for(Entity entity : list) 
			{
				if(!(entity instanceof EntityPlayer)) continue;
				EntityPlayer player = (EntityPlayer) entity;
				if(player.isInvisible()) return;
				if(!entity.getCommandSenderName().equalsIgnoreCase("victorious3")) continue;
				found = true;
				
				boolean flag = entity.isInRangeToRender3d(x, y, z) && (entity.ignoreFrustumCheck || frustrum.isBoundingBoxInFrustum(entity.boundingBox) || entity.riddenByEntity == mc.thePlayer);
	
				if(!flag && entity instanceof EntityLiving) 
				{
					EntityLiving entityliving = (EntityLiving) entity;
					if(entityliving.getLeashed() && entityliving.getLeashedToEntity() != null) 
					{
						Entity entity1 = entityliving.getLeashedToEntity();
						flag = frustrum.isBoundingBoxInFrustum(entity1.boundingBox);
					}
				}
				if(flag && (entity != mc.renderViewEntity || mc.gameSettings.thirdPersonView != 0 || mc.renderViewEntity.isPlayerSleeping()) && world.blockExists(MathHelper.floor_double(entity.posX), 0, MathHelper.floor_double(entity.posZ)))
				{
					GL11.glPushMatrix();
					double scale = 1.2 + (Math.sin((player.ticksExisted + partial) / 20D) + 1) * 0.02;
					GL11.glScaled(scale, scale, scale);
					GL11.glColor3f(0, (float)((Math.sin((player.ticksExisted + partial) / 20D) + 1) * 0.2), 1);
					
					RenderManager rm = RenderManager.instance;
					
					double x2 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - rm.renderPosX;
					double y2 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - rm.renderPosY;
					double z2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - rm.renderPosZ;
	
					float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partial;
					
					int i = entity.getBrightnessForRender(partial);
	
					int j = i % 65536;
					int k = i / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);

					RenderPlayer render = (RenderPlayer)RenderManager.instance.getEntityRenderObject(entity);
					if(render != null && rm.renderEngine != null && !render.isStaticEntity())
					{	
						ItemStack itemstack = player.inventory.getCurrentItem();
			            render.modelBipedMain.heldItemRight = itemstack != null ? 1 : 0;

			            if (itemstack != null && player.getItemInUseCount() > 0)
			            {
			                EnumAction enumaction = itemstack.getItemUseAction();

			                if (enumaction == EnumAction.block) render.modelBipedMain.heldItemRight = 3;
			                else if (enumaction == EnumAction.bow) render.modelBipedMain.aimedBow = true;
			            }

			            render.modelBipedMain.isSneak = player.isSneaking();
			            if (player.isSneaking() && !(player instanceof EntityPlayerSP)) y2 -= 0.125D;
						
						float f2 = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partial;
			            float f3 = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partial;
			            float f4;
	
			            if (player.isRiding() && player.ridingEntity instanceof EntityLivingBase)
			            {
			                EntityLivingBase entitylivingbase1 = (EntityLivingBase)player.ridingEntity;
			                f2 = player.prevRenderYawOffset + (entitylivingbase1.renderYawOffset - entitylivingbase1.prevRenderYawOffset) * partial;
			                f4 = MathHelper.wrapAngleTo180_float(f3 - f2);
			                f4 = MathHelper.clamp_float(f4, -85, 85);
			                f2 = f3 - f4;
			                if (f4 * f4 > 2500.0F) f2 += f4 * 0.2F;
			            }
	
			            float f13 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partial;
			            f4 = player.ticksExisted + partial;
			            
			            ReflectionHelper.findMethod(RenderPlayer.class, render, new String[]{"renderLivingAt", "func_77039_a"}, EntityLivingBase.class, double.class, double.class, double.class).invoke(render, entity, x2, y2, z2);
			            ReflectionHelper.findMethod(RenderPlayer.class, render, new String[]{"rotateCorpse", "func_77043_a"}, EntityLivingBase.class, float.class, float.class, float.class).invoke(render, entity, f4, f2, partial);
			            			            
			            float titlt = 0.0625F;
			            float limbSwing = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partial;
			            float limbSwing2 = player.limbSwing - player.limbSwingAmount * (1.0F - partial);
	
			            if (player.isChild()) limbSwing2 *= 3.0F;
			            if (limbSwing > 1.0F) limbSwing = 1.0F;
			            
			            GL11.glRotatef(180, 1, 0, 0);
			            GL11.glRotatef(180, 0, 1, 0);
			            GL11.glTranslatef(0, player.eyeHeight, 0);
			            
			            render.modelBipedMain.setRotationAngles(limbSwing2, limbSwing, f4, f3 - f2, f13, titlt, entity);
			            
			            render.modelBipedMain.bipedBody.render(titlt);
			            
			            render.modelBipedMain.bipedRightArm.offsetX = 1 / 16F;
			            render.modelBipedMain.bipedRightArm.render(titlt);
			            
			            render.modelBipedMain.bipedLeftArm.offsetX = -1 / 16F;
			            render.modelBipedMain.bipedLeftArm.render(titlt);
			            
			            render.modelBipedMain.bipedRightLeg.offsetY = render.modelBipedMain.bipedLeftLeg.offsetY = -2 / 16F;
			            if(player.isSneaking()) 
			            	render.modelBipedMain.bipedRightLeg.offsetZ = render.modelBipedMain.bipedLeftLeg.offsetZ = -1 / 16F;
			            render.modelBipedMain.bipedRightLeg.render(titlt);
			            render.modelBipedMain.bipedLeftLeg.render(titlt);
			            
			            render.modelBipedMain.bipedHeadwear.offsetY = 1 / 16F;
			            render.modelBipedMain.bipedHeadwear.render(titlt);
			            
			            render.modelBipedMain.bipedRightArm.offsetX = render.modelBipedMain.bipedLeftArm.offsetX = 0;
			            render.modelBipedMain.bipedRightLeg.offsetY = render.modelBipedMain.bipedLeftLeg.offsetY = 0;
			            render.modelBipedMain.bipedRightLeg.offsetZ = render.modelBipedMain.bipedLeftLeg.offsetZ = 0;
			            render.modelBipedMain.bipedHeadwear.offsetY = 0;
			        }
					
					GL11.glColor3f(1, 1, 1);
					GL11.glPopMatrix();
				}
			}
			if(!found) return;

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);
			
			if(OpenGlHelper.isFramebufferEnabled())
			{
				OpenGlHelper.func_153188_a(OpenGlHelper.field_153198_e, OpenGlHelper.field_153200_g, 3553, mcfbo.framebufferTexture, 0);
				
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				
				fbo2.bindFramebuffer(false);
				fbo.bindFramebufferTexture();
				
				ShaderHelper.bindShader(ShaderHelper.SHADER_BLUR);
				ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(ShaderHelper.SHADER_BLUR, "uShift"), 2F / fbo.framebufferWidth, 0);
				for(int i = 0; i < 6; i++)
				{
					if(i == 1) fbo2.bindFramebufferTexture();
					if(i == 3) ARBShaderObjects.glUniform2fARB(ARBShaderObjects.glGetUniformLocationARB(ShaderHelper.SHADER_BLUR, "uShift"), 0, 2F / fbo.framebufferHeight);
					Tessellator tes = Tessellator.instance;
					tes.startDrawingQuads();
					tes.addVertexWithUV(-1, -1, 0, 0, 0);
					tes.addVertexWithUV(1, -1, 0, 1, 0);
					tes.addVertexWithUV(1, 1, 0, 1, 1);
					tes.addVertexWithUV(-1, 1, 0, 0, 1);
					tes.draw();
				}
				ShaderHelper.releaseShader();
				fbo.framebufferClear();
				
				mcfbo.bindFramebuffer(false);
				fbo2.bindFramebufferTexture();
				
				GL11.glShadeModel(GL11.GL_SMOOTH);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
				
				Tessellator tes = Tessellator.instance;
				tes.startDrawingQuads();
				tes.addVertexWithUV(-1, -1, 0, 0, 0);
				tes.addVertexWithUV(1, -1, 0, 1, 0);
				tes.addVertexWithUV(1, 1, 0, 1, 1);
				tes.addVertexWithUV(-1, 1, 0, 0, 1);
				tes.draw();
				
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glShadeModel(GL11.GL_FLAT);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Specials.Post event)
	{
		EntityPlayer player = event.entityPlayer;
		String name = player.getCommandSenderName();
		Minecraft mc = Minecraft.getMinecraft();
		
		int renderType = 0;
		if(name.equalsIgnoreCase("victorious3")) renderType = 1;
		else if(name.equalsIgnoreCase("thog")) renderType = 2;
		else if(name.equalsIgnoreCase("rx14")) renderType = 3;
		else if(name.equalsIgnoreCase("riskyken")) renderType = 4;
		else if(name.equalsIgnoreCase("skyem")) renderType = 5;
		if(renderType == 0) return;	
		
		boolean hideArmor = player.inventory.armorItemInSlot(3) != null;
		
		//Test if AW is hiding the headgear
		if(IntegratedCircuits.isAWLoaded)
		{
			try {
				Object epRenderCache = Class.forName("riskyken.armourersWorkshop.client.handler.PlayerSkinHandler").getDeclaredField("INSTANCE").get(null);
				Field f = epRenderCache.getClass().getDeclaredField("skinMap");
				f.setAccessible(true);
				Map skinMap = (Map)f.get(epRenderCache);
				if(skinMap.containsKey(player.getPersistentID()))
				{
					Object skinInfo = skinMap.get(player.getPersistentID());
					Object nakedInfo = skinInfo.getClass().getMethod("getNakedInfo").invoke(skinInfo);
					BitSet armourOverride = (BitSet)nakedInfo.getClass().getDeclaredField("armourOverride").get(nakedInfo);
					if(armourOverride.get(0)) hideArmor = false;
				}	
			} catch (Exception e) {}
		}
		
		if(renderType != 2 && hideArmor) return;
		
		if(renderType == 5) 
		{
			long time = System.currentTimeMillis();
			
			//Nano Shinonome
			NanoProperties properties = (NanoProperties) event.entityPlayer.getExtendedProperties("nano");	
			if(properties == null)
				event.entityPlayer.registerExtendedProperties("nano", properties = new NanoProperties());
			boolean isJumping = player.motionY > player.jumpMovementFactor;
			if(isJumping && !properties.isJumping) properties.lastJumpStart = System.currentTimeMillis();
			else if(!isJumping && properties.isJumping)
			{
				int jumpTime = (int)(time - properties.lastJumpStart);
				if(jumpTime > 150 && jumpTime < 450)
				{
					if(!properties.isSpinning && time - properties.lastJump < 1200 && Math.random() < 0.5 && time - properties.lastSpin > 10000)
					{
						properties.lastSpin = time;
						properties.isSpinning = true;
						System.out.println(properties.isSpinning);
					}
					properties.lastJump = time;
				}
			}
			properties.isJumping = isJumping;
			
			GL11.glPushMatrix();
			GL11.glScalef(0.6F, 0.6F, 0.6F);
			GL11.glRotatef(-90, 0, 1, 0);
			GL11.glRotatef((float)Math.toDegrees(-event.renderer.modelBipedMain.bipedBody.rotateAngleX), 0, 0, 1);
			GL11.glTranslatef(3 / 16F, 0, 1 / 16F);
			GL11.glTranslatef(0, 1 / 2F, -1 / 16F);
			if(properties.isSpinning)
			{
				double t = time - properties.lastSpin;
				float angle = (float)(Math.sin(t / 50D) * 20D + 0.2 * t);
				GL11.glRotatef(angle, 1, 0, 0);
				if(angle >= 360F) properties.isSpinning = false;
			}	
			GL11.glTranslatef(0, -1 / 2F, 1 / 16F);
			mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_NANO);
			ItemRenderer.renderItemIn2D(Tessellator.instance, 0, 0, 1, 1, 32, 32, 2 / 16F);
			GL11.glPopMatrix();
			return;
		}
		
		float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.partialRenderTick;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.partialRenderTick;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.partialRenderTick;
		float pitchZ = (float)Math.toDegrees(event.renderer.modelBipedMain.bipedHead.rotateAngleZ);
		
		GL11.glPushMatrix();
		
		GL11.glColor3f(1F, 1F, 1F);
		GL11.glRotatef(pitchZ, 0, 0, 1);
		GL11.glRotatef(yawOffset, 0, -1, 0);
		GL11.glRotatef(yaw - 270, 0, 1, 0);
		GL11.glRotatef(pitch, 0, 0, 1);
		
		GL11.glTranslated(0, (player.isSneaking() ? 0.0625 : 0), 0);
		Tessellator tes = Tessellator.instance;
		
		if(renderType == 2)
		{
			//Jibril
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			RenderUtils.setBrightness(240, 240);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_HALO);
			
			GL11.glRotated(30, 1, 0, -1);
			GL11.glTranslatef(-0.1F, -0.62F, -0.1F);
			GL11.glRotatef(player.ticksExisted + event.partialRenderTick, 0, 1, 0);
			
			tes.startDrawingQuads();	
			tes.addVertexWithUV(-0.5, 0, -0.5, 0, 0);
			tes.addVertexWithUV(-0.5, 0, 0.5, 0, 1);
			tes.addVertexWithUV(0.5, 0, 0.5, 1, 1);
			tes.addVertexWithUV(0.5, 0, -0.5, 1, 0);	
			tes.draw();
			
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
			RenderUtils.resetBrightness();
		}
		else if(renderType == 1)
		{
			//Shiro Nai
			GL11.glPushMatrix();
			float scale = 1 / 64F;
			
			GL11.glTranslated(15 * scale, -0.78, 15 * scale);			
			float f1 = (float)(7 * Math.sin(Math.toRadians(45)) + 7 / 2F) * scale;
			GL11.glTranslatef(-f1, 0, -f1);
			GL11.glRotated(-25, 1, 0, -1);
			GL11.glTranslatef(f1, 0, f1);
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			ModelCrown.instance.render(scale);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glPopMatrix();
		}
		else if(renderType == 3)
		{
			//Stephanie Dola
			mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_EARS);
			ModelDogEars.instance.render(pitch, player.rotationYawHead - player.prevRotationYawHead);
			GameData.getBlockRegistry().getObject(name);
		}
		else if(renderType == 4) 
		{	
			//Mami Tomoe
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			renderCurl();
			GL11.glScalef(1, 1, -1);
			renderCurl();
			GL11.glScalef(1, 1, -1);
			renderHat();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_FLOWER);
			GL11.glPushMatrix();
			GL11.glTranslatef(0, -9 / 16F, 0);
			GL11.glTranslatef(2 / 16F, 0, -3.3F / 16F);
			GL11.glRotatef(85, 1, 0, 0);
			GL11.glRotatef(30, 0, 0, 1);
			tes.startDrawingQuads();
			tes.addVertexWithUV(-2 / 16F, 0, -2 / 16F, 0, 0);
			tes.addVertexWithUV(-2 / 16F, 0, 2 / 16F, 0, 1);
			tes.addVertexWithUV(2 / 16F, 0, 2 / 16F, 1, 1);
			tes.addVertexWithUV(2 / 16F, 0, -2 / 16F, 1, 0);
			tes.draw();
			GL11.glPopMatrix();
			
			mc.renderEngine.bindTexture(Resources.RESOURCE_MISC_FLUFF);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPushMatrix();
			GL11.glTranslatef(-1 / 16F, -8 / 16F, -5F / 16F);
			GL11.glRotatef(0, 1, 0, 0);
			tes.startDrawingQuads();
			tes.addVertexWithUV(0, -3 / 16F, -3 / 16F, 0, 0);
			tes.addVertexWithUV(0, 3 / 16F, -3 / 16F, 0, 1);
			tes.addVertexWithUV(0, 3 / 16F, 3 / 16F, 1, 1);
			tes.addVertexWithUV(0, -3 / 16F, 3 / 16F, 1, 0);
			tes.draw();
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		GL11.glPopMatrix();
	}
	
	/** Needed because of reflection. */
	public static void open7SegmentGUI(Part7Segment part) 
	{
		Minecraft.getMinecraft().displayGuiScreen(new Gui7Segment(part));
	}

	public static void renderCurl()
	{
		GL11.glPushMatrix();
		GL11.glRotatef(40, 1, 0, 0);
		GL11.glTranslatef(3 / 16F, 1.5F / 16F, 3.5F / 16F);
		
		Tessellator tes = Tessellator.instance;
		tes.startDrawing(GL11.GL_QUAD_STRIP);
		tes.setColorRGBA_I(0xF9DE85, 255);
		float x = 0, y = 0, z = 0, angle;
		float distance = 0.4F;
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		for(angle = 0.5F; angle <= (Math.PI * 2.16F * 2); angle += distance) 
		{
			float pos = 1 - (float)(angle / (Math.PI * 2.16F * 2)) * 0.7F;
			x = (float) Math.sin(angle) * 0.1F * pos;
			z = (float) Math.cos(angle) * 0.1F * pos;
			Vec3 normals = Vec3.createVectorHelper(x, 0, z).normalize();
			tes.setNormal((float)normals.xCoord, (float)normals.yCoord, (float)normals.zCoord);
			tes.addVertex(x - 0.025, y - 0.025, z - 0.025);
			tes.addVertex(x + 0.025, y + 0.025, z + 0.025);
			y += 0.01;
		}
		tes.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}
	
	public static void renderHat()
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(0, -10 / 16F, 0);
		
		float radius = 4 / 16F, height = 2 / 16F;
		float res = 0.7F;
		Tessellator tes = Tessellator.instance;
		
		tes.startDrawing(GL11.GL_TRIANGLE_FAN);
		tes.setColorRGBA_I(0x57424F, 255);
		tes.setNormal(0, -1, 0);
		tes.addVertex(0, 0, 0);
		for(float i = 0; i <= 2 * Math.PI; i += res)
			tes.addVertex(radius * Math.cos(i), 0, radius * Math.sin(i));
		tes.addVertex(radius, 0, 0);
		tes.draw();
		
		Vec3 center = Vec3.createVectorHelper(-radius, -height / 2, -radius);
		tes.startDrawing(GL11.GL_QUAD_STRIP);
		tes.setColorRGBA_I(0x57424F, 255);
		for(float i = 0; i <= 2 * Math.PI; i += res)
		{
			float x = (float)(radius * Math.cos(i));
			float z = (float)(radius * Math.sin(i));
			Vec3 v1 = Vec3.createVectorHelper(x, 0, z).subtract(center).normalize();
			tes.setNormal((float)v1.xCoord, (float)v1.yCoord, (float)v1.zCoord);
			tes.addVertex(x, 0, z);
			Vec3 v2 = Vec3.createVectorHelper(x, height, z).subtract(center).normalize();
			tes.setNormal((float)v2.xCoord, (float)v2.yCoord, (float)v2.zCoord);
			tes.addVertex(x, height, z);
		}
		tes.addVertex(radius, 0, 0);
		tes.addVertex(radius, height, 0);
		tes.draw();
		
		GL11.glPopMatrix();
	}
	
	public static class ModelCrown extends ModelBase
	{
		public static ModelCrown instance = new ModelCrown();
		
		public ModelRenderer crown1;
		public ModelRenderer crown2;
		
		public ModelCrown()
		{
			int i1 = 7;
			int i2 = 18;	
			this.textureWidth = i1 * 2;
			this.textureHeight = i2;		
			float f1 = -(i1 / 2F);	
			float f2 = (float)(i1 * Math.sin(Math.toRadians(45)) + i1 / 2F);	
			
			crown1 = new ModelRenderer(this);
			crown1.setTextureOffset(0, 0);
			crown1.addBox(f1, 0, -f2, i1, i2, 0);
			crown1.setTextureOffset(7, 0);
			crown1.addBox(f1, 0, f2, i1, i2, 0);
			crown1.setTextureOffset(0, -7);
			crown1.addBox(-f2, 0, f1, 0, i2, i1);
			crown1.setTextureOffset(7, -7);
			crown1.addBox(f2, 0, f1, 0, i2, i1);
			crown1.rotateAngleY = (float) Math.toRadians(30);
			
			crown2 = new ModelRenderer(this);
			crown2.setTextureOffset(7, 0);
			crown2.addBox(f1, 0, -f2, i1, i2, 0);
			crown2.setTextureOffset(0, 0);
			crown2.addBox(f1, 0, f2, i1, i2, 0);
			crown2.setTextureOffset(7, -7);
			crown2.addBox(-f2, 0, f1, 0, i2, i1);
			crown2.setTextureOffset(0, -7);
			crown2.addBox(f2, 0, f1, 0, i2, i1);
			crown2.rotateAngleY = (float) Math.toRadians(75);
		}
		
		public void render(float scale)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(Resources.RESOURCE_MISC_CROWN);
			crown1.render(scale);
			crown2.render(scale);
		}
	}
	
	public static class ModelDogEars extends ModelBase
	{
		public static ModelDogEars instance = new ModelDogEars();
		
		public ModelRenderer ear;
		
		public ModelDogEars()
		{
			this.textureWidth = 16;
			this.textureHeight = 16;
			ear = new ModelRenderer(this);
			ear.addBox(0, 0, 0, 3, 9, 1);
		}
		
		public void render(float pitch, float off)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(0, -6 / 16F, -5 / 16F);
			GL11.glTranslatef(1.5F / 16F, 0, 0.5F / 16F);
			GL11.glRotatef(-pitch, 0, 0, 1);
			GL11.glRotatef(-5, 1, 0, 0);
			if(off < 0) GL11.glRotatef(off, 1, 0, 0);
			GL11.glTranslatef(-1.5F / 16F, 0, -0.5F / 16F);
			ear.render(1 / 16F);
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
			GL11.glTranslatef(0, -6 / 16F, 4 / 16F);
			GL11.glTranslatef(1.5F / 16F, 0, 0.5F / 16F);
			GL11.glRotatef(-pitch, 0, 0, 1);
			GL11.glRotatef(5, 1, 0, 0);
			if(off > 0) GL11.glRotatef(off, 1, 0, 0);
			GL11.glTranslatef(-1.5F / 16F, 0, -0.5F / 16F);
			ear.render(1 / 16F);
			GL11.glPopMatrix();
		}
	}
	
	private static class NanoProperties implements IExtendedEntityProperties {
		
		private boolean isJumping;
		private long lastJumpStart;
		private long lastJump;
		private long lastSpin;
		private boolean isSpinning;

		@Override public void saveNBTData(NBTTagCompound compound) {}
		@Override public void loadNBTData(NBTTagCompound compound) {}
		@Override public void init(Entity entity, World world) {}	
	}
}
