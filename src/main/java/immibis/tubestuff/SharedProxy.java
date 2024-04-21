package immibis.tubestuff;

import cpw.mods.fml.client.FMLClientHandler;
import immibis.core.Config;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// TODO: move this stuff into the main mod file

public class SharedProxy {
	static boolean CC_CheckArgs(Object[] objs, Class[] types)
	{
		if(objs.length != types.length)
			return false;
		for(int k = 0; k < objs.length; k++)
			if(objs[k].getClass() != types[k])
				return false;
		return true;
	}
	
	public static final boolean DEBUG_MODE = TubeStuff.class.getName().equals("net.minecraft.src.mod_TubeStuff");
	
	public static boolean enableBHCParticles, enableBHC, enableBHCAnim, enableCraftingIncinerator;
	public static boolean enableCraftingRetrievulator, enableStorageBlocks, enableStorageBlocksVanilla, enableStorageBlockOreDictionary;
	public static boolean enableCraftingBlockBreaker;
	
	private static Class findBCClass(String name)
	{
		try
		{
			return SharedProxy.class.getClassLoader().loadClass("buildcraft."+name);
		}
		catch(ClassNotFoundException e)
		{
			return null;
		}
	}
	
	//public static IConfigReader redpowerConfig = null;
	
	public static void FirstTick() {
		int machineID = -1;
		int woodenPipeID = -1;
		int engineID = -1;
		int logicID = -1;
		int actID = -1;
		
		/*for(Block b : Block.blocksList)
			if(b != null)
				System.err.println(b.blockID+": "+b.getClass().getName()+": "+b);
		for(Item i : Item.itemsList)
			if(i != null && !(i instanceof ItemBlock))
				System.err.println(i.shiftedIndex+": "+i.getClass().getName()+": "+i);*/
		
		Item unused = RedPowerItems.screwdriverItem; // load RedPowerItems
		
		try
		{
			machineID = TubeStuff.findBlockByClass("com.eloraam.redpower.machine.BlockMachine").blockID;
		}
		catch(Exception e)
		{
			System.out.println(TubeStuff.class.getSimpleName()+": RP2 Machine doesn't seem to be installed");
			machineID = -1;
		}

		try
		{
			logicID = RedPowerItems.logicBlock.blockID;
		}
		catch(Exception e)
		{
			System.out.println(TubeStuff.class.getSimpleName()+": RP2 Logic doesn't seem to be installed");
			logicID = -1;
		}
		
		try
		{
			Class bcTransport = findBCClass("BuildCraftTransport");
			if(bcTransport != null)
				woodenPipeID = ((Item)bcTransport.getDeclaredField("pipeItemsWood").get(null)).itemID;
		}
		catch(Exception e)
		{
			System.out.println(TubeStuff.class.getSimpleName()+": BC Transport doesn't seem to be installed");
		}

		try
		{
			Class bcEnergy = findBCClass("BuildCraftEnergy");
			if(bcEnergy != null)
				engineID = ((Block)bcEnergy.getDeclaredField("engineBlock").get(null)).blockID;
		}
		catch(Exception e)
		{
			System.out.println(TubeStuff.class.getSimpleName()+": BC Energy doesn't seem to be installed");
		}
		
		try
		{
			Class bcFactory = findBCClass("BuildCraftFactory");
			if(bcFactory != null)
				actID = ((Block)bcFactory.getDeclaredField("autoWorkbenchBlock").get(null)).blockID;
		}
		catch(Exception e)
		{
			System.out.println(TubeStuff.class.getSimpleName()+": BC Factory doesn't seem to be installed");
		}
		
		ItemStack bufferIS = new ItemStack(TubeStuff.block, 1, 0);
		ItemStack actIS = new ItemStack(TubeStuff.block, 1, 1);
		ItemStack infChestIS = new ItemStack(TubeStuff.block, 1, 2);
		
		boolean addAllRecipes = false; // useful for taking screenshots
		
		if(machineID != -1)
		{
			if(logicID != -1)
				GameRegistry.addShapelessRecipe(bufferIS, new Object[] {
					new ItemStack(machineID, 1, 2), // transposer
					Block.chest,
					new ItemStack(logicID, 1, 0) // timer
				});
			if(logicID == -1 || addAllRecipes)
				GameRegistry.addShapelessRecipe(bufferIS, new Object[] {
					new ItemStack(machineID, 1, 2), // transposer
					Block.chest
				});
			GameRegistry.addRecipe(new ShapedOreRecipe(actIS,
				"GFG",
				"WCW",
				"OcO",
				'F', new ItemStack(machineID, 1, 2), // transposer
				'G', Item.goldNugget,
				'C', Block.workbench,
				'c', Block.chest,
				'W', "plankWood",
				'O', Block.cobblestone
			));
		}
		if(machineID == -1 || addAllRecipes)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(actIS,
				"GGG",
				"WCW",
				"OcO",
				'G', Item.goldNugget,
				'C', Block.workbench,
				'c', Block.chest,
				'W', "plankWood",
				'O', Block.cobblestone
			));
		}
		if(woodenPipeID != -1)
		{
			if(engineID != -1)
				GameRegistry.addShapelessRecipe(bufferIS, new Object[] {
					Item.itemsList[woodenPipeID],
					Block.chest,
					new ItemStack(engineID, 1, 0) // redstone engine
				});
			if(engineID == -1 || addAllRecipes)
				GameRegistry.addShapelessRecipe(bufferIS, new Object[] {
					Item.itemsList[woodenPipeID],
					Block.chest
				});
			
			GameRegistry.addRecipe(new ShapedOreRecipe(actIS,
				"GWG",
				"PCP",
				"OcO",
				'G', Item.goldNugget,
				'C', actID == -1 ? Block.workbench : Block.blocksList[actID],
				'c', Block.chest,
				'W', Item.itemsList[woodenPipeID],
				'P', "plankWood",
				'O', Block.cobblestone
			));
		}
		
		enableBHC = Config.getBoolean("tubestuff.enableBlackHoleChest", false);
		
		if(enableBHC) {
			GameRegistry.addRecipe(infChestIS, new Object[] {
				"ODO",
				"OCO",
				"ODO",
				'O', Block.obsidian,
				'C', Block.chest,
				'D', Block.blockDiamond
			});
		}
		
		enableBHCAnim = Config.getBoolean("tubestuff.enableBHCAnim", true);
		enableBHCParticles = Config.getBoolean("tubestuff.enableBHCParticles", true);
		
		if(enableCraftingIncinerator = Config.getBoolean("tubestuff.enableCraftingIncinerator", true))
		{
			// Incinerator

			GameRegistry.addRecipe(new ItemStack(TubeStuff.block, 1, 3), new Object[] {
				"CCC",
				"CLC",
				"CCC",
				'C', Block.cobblestone,
				'L', Item.bucketLava
			});
		}
		
		enableCraftingRetrievulator = Config.getBoolean("tubestuff.enableCraftingRetrievulator", true);
		
		if(machineID != -1 && enableCraftingRetrievulator) {
			// Retrievulator
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TubeStuff.block, 1, 5),
				"WWW",
				"R T",
				"WWW",
				'R', new ItemStack(machineID, 1, 10), // regulator
				'T', new ItemStack(machineID, 1, 2), // transposer
				'W', "plankWood"
			));
		}
		
		if(enableCraftingBlockBreaker = Config.getBoolean("tubestuff.enableCraftingBlockBreaker", true)) {
			GameRegistry.addRecipe(new ItemStack(TubeStuff.block, 4, 6), new Object[] {
				"O O",
				"---",
				"OXO",
				'O', Block.obsidian,
				'-', Item.stick,
				'X', Block.pistonStickyBase
			});
		}
		
		if(Config.getBoolean("tubestuff.enableCraftingLiquidIncinerator", true)) {
			GameRegistry.addShapelessRecipe(new ItemStack(TubeStuff.block, 1, 7),
				new ItemStack(TubeStuff.block, 1, 3),
				Item.bucketEmpty);
		}
		
		if(Config.getBoolean("tubestuff.enableCraftingOnlinePlayerDetector", true)) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TubeStuff.block, 1, BlockTubestuff.META_ONLINE_DETECTOR),
				"/r/",
				"g g",
				"/r/",
				'/', Item.ingotGold,
				'g', "dyeGreen",
				'r', "dyeRed"
			));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TubeStuff.block, 1, BlockTubestuff.META_ONLINE_DETECTOR),
				"/g/",
				"r r",
				"/g/",
				'/', Item.ingotGold,
				'g', "dyeGreen",
				'r', "dyeRed"
			));
		}
		
		
		
		if(enableStorageBlocks) {
			addStorageRecipes(0, RedPowerItems.silverIS, enableStorageBlocks, "ingotSilver");
			addStorageRecipes(1, RedPowerItems.tinIS, enableStorageBlocks, "ingotTin");
			addStorageRecipes(2, RedPowerItems.copperIS, enableStorageBlocks, "ingotCopper");
			addStorageRecipes(3, RedPowerItems.nikoliteIS, enableStorageBlocks, null);
			addStorageRecipes(4, new ItemStack(Item.coal, 1, 0), enableStorageBlocksVanilla, null);
			addStorageRecipes(5, new ItemStack(Item.redstone, 1, 0), enableStorageBlocksVanilla, null);
			addStorageRecipes(6, RedPowerItems.blueAlloyIS, enableStorageBlocks, null);
			addStorageRecipes(7, RedPowerItems.redAlloyIS, enableStorageBlocks, null);
			addStorageRecipes(8, RedPowerItems.brassIS, enableStorageBlocks, "ingotBrass");
			addStorageRecipes(9, new ItemStack(Item.coal, 1, 1), enableStorageBlocksVanilla, null);
		}
	}
	
	private static void addStorageRecipes(int block_meta, ItemStack item, boolean enable, String oreName) {
		
		if(enable) {
			if(item != null)
				GameRegistry.addRecipe(new ItemStack(TubeStuff.blockStorage, 1, block_meta), "###", "###", "###", '#', item);
			if(enableStorageBlockOreDictionary && oreName != null)
				CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(TubeStuff.blockStorage, 1, block_meta), "###", "###", "###", '#', oreName));
		}
		
		// decompressing recipe is always enabled
		ItemStack result;
		if(item != null)
			result = item.copy();
		else if(enableStorageBlockOreDictionary && oreName != null) {
			List<ItemStack> ores = net.minecraftforge.oredict.OreDictionary.getOres(oreName);
			if(ores.isEmpty())
				result = null;
			else
				result = ores.get(0).copy();
		} else
			result = null;
		
		if(result != null) {
			result.stackSize = 9;
			GameRegistry.addRecipe(result, "#", '#', new ItemStack(TubeStuff.blockStorage, 1, block_meta));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean enableBHCAnim() {
		GameSettings gs = FMLClientHandler.instance().getClient().gameSettings;
		return enableBHCAnim && !gs.anaglyph && gs.fancyGraphics;
	}
}
