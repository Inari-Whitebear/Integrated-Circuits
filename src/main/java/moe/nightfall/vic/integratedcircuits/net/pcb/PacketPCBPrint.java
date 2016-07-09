package moe.nightfall.vic.integratedcircuits.net.pcb;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPrinter;
import net.minecraft.entity.player.EntityPlayer;

public class PacketPCBPrint extends PacketTileEntity<PacketPCBPrint> {

	public PacketPCBPrint() {
	}

	public PacketPCBPrint(int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD cad = (TileEntityCAD) player.worldObj.getTileEntity(new BlockPos(xCoord, yCoord, zCoord));
		if (cad != null && cad.isPrinterConnected()) {
			BlockPos pl = new BlockPos(cad.getPos()).offset(cad.printerLocation());
			TileEntityPrinter printer = (TileEntityPrinter) player.worldObj.getTileEntity(pl);
			if (printer != null) {
				printer.print(cad.getCircuitData());
			}
		}
	}
}
