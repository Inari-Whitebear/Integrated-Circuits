package moe.nightfall.vic.integratedcircuits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import mcmultipart.multipart.*;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketProvider;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.compat.BPRedstoneProvider;
import moe.nightfall.vic.integratedcircuits.compat.gateio.GateIO;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.gate.Gate7Segment;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.BlockSocket;
import moe.nightfall.vic.integratedcircuits.tile.TileEntitySocket;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = "integratedcircuits", dependencies = "required-after:CodeChickenCore; after:ComputerCraft; after:ForgeMultipart@[1.1.2.332,)", guiFactory = "moe.nightfall.vic.integratedcircuits.client.gui.IntegratedCircuitsGuiFactory")
public class IntegratedCircuits {

	// TODO Some of those might be obsolete
	// TODO Move to some helper class called Compat
	public static boolean isPRLoaded = false;
	public static boolean isAWLoaded = false;
	public static boolean isBPLoaded = false;
	public static boolean isMCMPLoaded = false;
	public static boolean isRLLoaded = false;
	public static boolean isMFRLoaded = false;
	public static boolean isOCLoaded = false;
	//public static boolean isCCLoaded = false;
	//public static boolean isNEILoaded = false;
	public static boolean isBCLoaded = false;

	// TODO BETTER NAME?
	public static boolean isBCToolsAPIThere = false;
	public static boolean isBPAPIThere = false;

	public static boolean developmentEnvironment;
	public static Logger logger;

	public static CreativeTabs creativeTab;

	public static final API API = new API();

	@Instance(Constants.MOD_ID)
	public static IntegratedCircuits instance;

	@SidedProxy(clientSide = "moe.nightfall.vic.integratedcircuits.proxy.ClientProxy", serverSide = "moe.nightfall.vic.integratedcircuits.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		developmentEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

		// Initialize API
		Field apiField = IntegratedCircuitsAPI.class.getDeclaredField("instance");
		apiField.setAccessible(true);
		apiField.set(null, API);

		logger = event.getModLog();
		logger.info("Loading Integrated Circutis " + Constants.MOD_VERSION);

		Config.preInitialize(event.getSuggestedConfigurationFile());
		CircuitPart.registerParts();
		Config.postInitialize();

		// Compatibility
		logger.info("Searching for compatible mods");
		logger.info("ProjRed|Transmission: " + (isPRLoaded = Loader.isModLoaded("ProjRed|Transmission")));
		logger.info("armourersWorkshop: " + (isAWLoaded = Loader.isModLoaded("armourersWorkshop")));
		logger.info("BluePower: " + (isBPLoaded = Loader.isModLoaded("bluepower")));
		logger.info("MCMultiPart: " + (isMCMPLoaded = Loader.isModLoaded("MCMultiPart")));
		logger.info("RedLogic: " + (isRLLoaded = Loader.isModLoaded("RedLogic")));
		logger.info("MineFactoryReloaded: " + (isMFRLoaded = Loader.isModLoaded("MineFactoryReloaded")));
		logger.info("Open Computers: " + (isOCLoaded = Loader.isModLoaded("OpenComputers")));
		//logger.info("Computer Craft: " + (isCCLoaded = Loader.isModLoaded("ComputerCraft")));
		//logger.info("Not Enough Items: " + (isNEILoaded = Loader.isModLoaded("NotEnoughItems")));
		logger.info("BuildCraft: " + (isBCLoaded = Loader.isModLoaded("BuildCraft|Core")));
		logger.info("Searching for compatible APIs");
		logger.info("BuildCraft Tools API: " + (isBCToolsAPIThere = ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools")));
		logger.info("BluePower API: " + (isBPAPIThere = ModAPIManager.INSTANCE.hasAPI("bluepowerAPI")));

		if (isMCMPLoaded)
			logger.info("Minecraft Multi Part installation found! MCMP Compatible gates will be added.");

		proxy.preInitialize();

		creativeTab = new CreativeTabs(Constants.MOD_ID + ".ctab") {

			private ItemStack iconStack;

			@Override
			public ItemStack getIconItemStack() {
				if (iconStack == null)
					iconStack = new ItemStack(Content.itemCircuit, 1, Integer.MAX_VALUE);
				return iconStack;
			}

			@Override
			public Item getTabIconItem() {
				return null;
			}
		};

		IntegratedCircuitsAPI.getGateRegistry().registerGate("circuit", GateCircuit.class);
		IntegratedCircuitsAPI.getGateRegistry().registerGate("7segment", Gate7Segment.class);

		// Initialize content
		Content.init();

		// Register socket provider
		if (isMCMPLoaded) {
			IntegratedCircuitsAPI.registerSocketProvider(new ISocketProvider() {
				@Override
				public ISocket getSocketAt(World world, BlockPos pos, EnumFacing facing) {
					//TileEntity te = world.getTileEntity(pos);
					IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
					if (container != null) {

						ISlottedPart multipart = container.getPartInSlot(PartSlot.getFaceSlot(facing));
						if (multipart instanceof ISocketWrapper)
							return ((ISocketWrapper) multipart).getSocket();
					}
					return null;
				}
			});
		}

		IntegratedCircuitsAPI.registerSocketProvider(new ISocketProvider() {
			@Override
			public ISocket getSocketAt(World world, BlockPos pos, EnumFacing facing) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof ISocketWrapper) {
					ISocketWrapper wrapper = (ISocketWrapper) te;
					if (wrapper.getSocket().getSide() == facing)
						return wrapper.getSocket();
				}
				return null;
			}
		});

		// Need to wait for BC
		// if (isBCLoaded)
		// BCAddon.preInit();

		GateIO.initialize();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) throws Exception {
		// Initialize with reflection so that the transformer doesn't run upon
		// constructing this class.
		Content.blockSocket = BlockSocket.class.newInstance();

		GameRegistry.register(Content.blockSocket);
		GameRegistry.registerTileEntity(TileEntitySocket.class, Constants.MOD_ID + ".socket");

		/*
		if (isCCLoaded) {
			ComputerCraftAPI.registerBundledRedstoneProvider((IBundledRedstoneProvider) Content.blockSocket);
			ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) Content.blockSocket);
		}
		*/

		proxy.initialize();

		FMLInterModComms.sendMessage("Waila", "register", "moe.nightfall.vic.integratedcircuits.compat.WailaAddon.registerAddon");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Recipes.loadRecipes();

		if (Config.enableTracker) {
			new Thread() {
				@Override
				public void run() {
					// I would have done it with commons, but it doesn't let me.
					// So this is pretty much copied from AW
					// https://github.com/RiskyKen/Armourers-Workshop
					try {
						String location = "http://bit.ly/1GIaUA6";
						HttpURLConnection conn = null;
						while (location != null && !location.isEmpty()) {
							URL url = new URL(location);
							if (conn != null)
								conn.disconnect();

							conn = (HttpURLConnection) url.openConnection();
							conn.setRequestProperty("User-Agent",
									"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
							conn.setRequestProperty("Referer", "http://" + Constants.MOD_VERSION);
							conn.connect();
							location = conn.getHeaderField("Location");
						}

						if (conn == null)
							throw new NullPointerException();
						String newestVersion = new BufferedReader(new InputStreamReader(conn.getInputStream(),
								Charset.forName("UTF-8"))).readLine();
						// TODO version checker? I don't really like them but we
						// have the information now...
						logger.info("Your version: {}, Newest version: {}", Constants.MOD_VERSION, newestVersion);
						conn.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.run();
		}

		// Register provider for bluepower
		if (isBPLoaded)
			new BPRedstoneProvider();

		logger.info("Done! This is an extremely early alpha version so please report any bugs occurring to https://github.com/Victorious3/Integrated-Circuits");
	}
}
