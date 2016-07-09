package moe.nightfall.vic.integratedcircuits;

//import mcmultipart.item.ItemMultiPart;
//import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import moe.nightfall.vic.integratedcircuits.item.recipe.RecipeCircuit;
import moe.nightfall.vic.integratedcircuits.item.recipe.RecipeDyeable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
//import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
//import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class Recipes {
	public static void loadRecipes() {
		/*
		// Oredict
		Item bpSilicon = GameData.getItemRegistry().getObject("bluepower:silicon_wafer");
		if (bpSilicon != null)
			OreDictionary.registerOre("silicon", bpSilicon);

		Item prCorePart = GameData.getItemRegistry().getObject("ProjRed|Core:projectred.core.part");
		if (prCorePart != null)
			OreDictionary.registerOre("silicon", new ItemStack(prCorePart, 1, 12));
			*/

		//if (!(IntegratedCircuits.isPRLoaded || IntegratedCircuits.isBPLoaded)) {
			OreDictionary.registerOre("silicon", Content.itemSilicon);

			GameRegistry.addRecipe(new ItemStack(Content.itemCoalCompound),
					"###",
					"#c#",
					"###",

					'#', Blocks.SAND,
					'c', Items.COAL);

			GameRegistry.addRecipe(new ItemStack(Content.item7Segment),
					"srs",
					"r#r",
					"sps",

					'r', Items.REDSTONE,
					's', Blocks.STONE,
					'#', Blocks.GLASS_PANE,
					'p', Content.itemPCBChip);

			GameRegistry.addSmelting(Content.itemCoalCompound, new ItemStack(Content.itemSilicon,
					8), 0.5F);
		/*} else {
			
			Item bpStoneWafer = GameData.getItemRegistry().getObject("bluepower:stone_tile");
			if (bpStoneWafer != null)
				OreDictionary.registerOre("stoneWafer", bpStoneWafer);

			Item bpStoneWire = GameData.getItemRegistry().getObject("bluepower:redstone_wire_tile");
			if (bpStoneWire != null)
				OreDictionary.registerOre("stoneWire", bpStoneWire);
			

			
			if (prCorePart != null) {
				OreDictionary.registerOre("stoneWafer", new ItemStack(prCorePart, 1, 0));
				OreDictionary.registerOre("stoneWire", new ItemStack(prCorePart, 1, 2));
			}
			

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Content.item7Segment),
					"srs",
					"r#r",
					"sps",

					'r', "stoneWire",
					's', "stoneWafer",
					'#', Blocks.GLASS_PANE,
					'p', Content.itemPCBChip));
		}*/

		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Content.itemSiliconDrop, 8), "silicon"));

		GameRegistry.addRecipe(new ItemStack(Content.itemPCBChip),
				"iri",
				"r#r",
				"iri",

				'i', Content.itemSiliconDrop,
				'r', Items.REDSTONE,
				'#', Content.itemPCB);

		GameRegistry.addRecipe(new ItemStack(Content.itemPCBChip),
				"rir",
				"i#i",
				"rir",

				'i', Content.itemSiliconDrop,
				'r', Items.REDSTONE,
				'#', Content.itemPCB);

		GameRegistry.addRecipe(new ItemStack(Content.blockPCBLayout),
				"iii",
				"i#i",
				"sps",

				'i', Items.IRON_INGOT,
				'#', Blocks.GLASS_PANE,
				's', Blocks.STONE,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.blockAssembler),
				"###",
				"#d#",
				"sps",

				'd', Items.DIAMOND,
				'#', Blocks.GLASS_PANE,
				's', Blocks.STONE,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.blockPrinter), "iii", "#d#", "sps",

		'i', Items.IRON_INGOT, '#', Blocks.PISTON, 's', Blocks.STONE, 'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.itemFloppyDisk),
				"iii",
				"i#i",
				"iii",

				'i', Content.itemSiliconDrop,
				'#', Items.REDSTONE);

		GameRegistry.addRecipe(new ItemStack(Content.itemPCB),
				"iii",
				"iii",
				"iii",

				'i', Content.itemSiliconDrop);

		GameRegistry.addRecipe(new ItemStack(Content.itemScrewdriver),
				"i  ",
				" id",
				" di",

				'i', Items.IRON_INGOT,
				'd', Content.itemSiliconDrop);

		GameRegistry.addRecipe(new ItemStack(Content.itemSolderingIron),
				"i  ",
				" ir",
				" ri",

				'i', Items.IRON_INGOT,
				'r', Items.REDSTONE);

		GameRegistry.addRecipe(new ItemStack(Content.itemLaser),
				"oii",
				"p##",
				"oii",

				'i', Items.REDSTONE,
				'#', Items.DIAMOND,
				'o', Blocks.OBSIDIAN,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.itemLaser),
				"i#i",
				"i#i",
				"opo",

				'i', Items.REDSTONE,
				'#', Items.DIAMOND,
				'o', Blocks.OBSIDIAN,
				'p', Content.itemPCBChip);

		GameRegistry.addRecipe(new ItemStack(Content.itemSocket),
				"iri",
				"###",

				'i', Content.itemSiliconDrop,
				'r', Items.REDSTONE,
				'#', Blocks.STONE_SLAB);

		/*
		if (IntegratedCircuits.isMCMPLoaded) {

			GameRegistry.addRecipe(new ItemStack(Content.itemSocketFMP),
					"iri",
					"###",

					'i', Content.itemSiliconDrop,
					'r', Items.REDSTONE,
					'#', ItemMicroPart.create(1, "tile.stone"));
		}*/

		// TODO NEI integration? Rewrite using multiple recipes?
		GameRegistry.addRecipe(new RecipeDyeable());
		GameRegistry.addRecipe(new RecipeCircuit());
	}
}
