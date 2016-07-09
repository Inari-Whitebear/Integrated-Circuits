package moe.nightfall.vic.integratedcircuits.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class RayTracer {
	/**
	 * Returns a player's ray.
	 * 
	 * @param player
	 * @param partialTicks
	 * @return Vec3[] 0 = from, 1 = to
	 */
	public static Vec3d[] getPlayerRay(EntityPlayer player, float partialTicks) {
		Vec3d start = getPositionVector(player, partialTicks);
		Vec3d look = player.getLook(partialTicks);
		double reach = getBlockReachDistance(player);
		Vec3d end = start.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
		return new Vec3d[] { start, end };
	}

	public static Vec3d getPositionVector(EntityPlayer player, float partialTicks) {
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * partialTicks + player.getEyeHeight();
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

		if (!(player instanceof EntityPlayerMP))
			d1 -= player.getDefaultEyeHeight();

		return new Vec3d(d0, d1, d2);
	}

	/**
	 * Returns the hit distance of a Player, works both on client and server
	 * side.
	 * 
	 * @param player
	 * @return distance
	 */
	public static double getBlockReachDistance(EntityPlayer player) {
		if (player instanceof EntityPlayerMP)
			return ((EntityPlayerMP)player).interactionManager.getBlockReachDistance();
		return Minecraft.getMinecraft().playerController.getBlockReachDistance();
	}

	/**
	 * Get the nearest intersecting {@link AxisAlignedBB}, the
	 * {@link AxisAlignedBB} selected is returned as
	 * {@link RayTraceResult#hitInfo}. Returns {@code null} if none of the
	 * {@link AxisAlignedBB AxisAligensBBs} intersect.
	 * 
	 * @param player
	 * @param partialTicks
	 * @param alignedAABBs
	 * @return {@link RayTraceResult} or {@code null}
	 */
	public static RayTraceResult rayTraceAABB(EntityPlayer player, float partialTicks,
											  AxisAlignedBB... alignedAABBs) {
		if (alignedAABBs.length == 0)
			return null;
		RayTraceResult nearest = null;

		Vec3d[] playerRay = getPlayerRay(player, partialTicks);
		Vec3d start = playerRay[0];
		Vec3d end = playerRay[1];

		for (AxisAlignedBB aabb : alignedAABBs) {
			if (aabb == null)
				continue;
			RayTraceResult pos = aabb.calculateIntercept(start, end);
			if (pos == null)
				continue;
			if (nearest == null || pos.hitVec.distanceTo(start) < nearest.hitVec.distanceTo(start)) {
				nearest = pos;
				nearest.hitInfo = aabb;
			}
		}
		return nearest;
	}

	public static RayTraceResult rayTrace(EntityPlayer player, float partialTicks) {
		Vec3d[] playerRay = getPlayerRay(player, partialTicks);
		Vec3d start = playerRay[0];
		Vec3d end = playerRay[1];

		RayTraceResult target = player.worldObj.rayTraceBlocks(start, end);
		return target;
	}
}
