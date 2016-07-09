package moe.nightfall.vic.integratedcircuits.api.gate;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISocketBridge {

	public void markRender();

	public MCDataOutput getWriteStream(int disc);

	public World getWorld();

	public void notifyBlocksAndChanges();

	public void notifyPartChange();

	public BlockPos getPos();

	public void destroy();

	public void updateInput();

	public int updateRedstoneInput(EnumFacing side);

	public byte[] updateBundledInput(EnumFacing side);

	public void scheduleTick(int delay);

	public void sendDescription();

	public static interface ISocketBase extends ISocketBridge {

		public void setGate(IGate gate);

		public void setGate(ItemStack stack, EntityPlayer player);

		public IGate getGate();

		public ISocketWrapper getWrapper();

		public byte getOrientation();

		public EnumFacing getSide();

		public EnumFacing getSideRel(EnumFacing side);

		public void setSide(EnumFacing side);

		public EnumFacing getRotation();

		public EnumFacing getRotationAbs(EnumFacing rel);

		public EnumFacing getRotationRel(EnumFacing abs);

		public void setRotation(EnumFacing rot);

		@SideOnly(Side.CLIENT)
		public byte getRedstoneIO();

		public byte[][] getInput();

		public byte[][] getOutput();

		public void setInput(byte[][] input);

		public void setOutput(byte[][] output);

		public byte getRedstoneInput(EnumFacing side);

		public byte getBundledInput(EnumFacing side, int frequency);

		public byte getRedstoneOutput(EnumFacing side);

		public byte getBundledOutput(EnumFacing side, int frequency);

		public void setInput(EnumFacing side, int frequency, byte input);

		public void setOutput(EnumFacing side, int frequency, byte output);

		public void resetInput();

		public void resetOutput();

		public EnumConnectionType getConnectionTypeAtSide(EnumFacing side);

		/**
		 * Use this to store additional data on this socket, meant for use with
		 * {@link GateIOProvider}.
		 * 
		 * @param key
		 * @return T
		 */
		public <T> T get(String key);

		public void put(String key, Object o);

		public void updateInputPre();

		public void updateInputPost();
	}
}
