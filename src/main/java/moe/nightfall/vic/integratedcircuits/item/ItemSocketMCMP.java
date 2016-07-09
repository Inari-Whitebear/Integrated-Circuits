package moe.nightfall.vic.integratedcircuits.item;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemSocketMCMP extends ItemMultiPart {
    public ItemSocketMCMP() {
        setRegistryName("socket_mcmp");
        setUnlocalizedName(Constants.MOD_ID + ".socket_mcmp");
        setCreativeTab(IntegratedCircuits.creativeTab);
        GameRegistry.register(this);
    }

    @Override
    public IMultipart createPart(World world, BlockPos pos, EnumFacing side, Vec3d hit, ItemStack stack, EntityPlayer player) {
        return null; //TODO implement
    }
}
