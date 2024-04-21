package immibis.tubestuff;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RedPowerItems {
	public static Block logicBlock;
	public static Item logicPartItem;
	public static ItemStack stoneWaferIS, stoneWireIS, stoneAnodeIS, stoneCathodeIS, stonePointerIS, stoneRedwireIS, plateAssemblyIS, siliconChipIS, taintedChipIS, stoneBundleIS;
	public static ItemStack timerIS, sequencerIS, stateCellIS, rsLatchIS, norIS, orIS, nandIS, andIS, xnorIS, xorIS, pulseFormerIS, notIS, bufferGateIS, multiplexerIS, repeaterIS, synchronizerIS, transLatchIS, counterIS;
	public static ItemStack toggleLatchIS, randomizerIS, lightSensorIS, nullCellIS, invertCellIS, nonInvertCellIS, busTransceiverIS;
	public static Item screwdriverItem, sonicScrewdriverItem;
	public static Item alloyItem, resourceItem;
	public static ItemStack rubyIS, emeraldIS, sapphireIS, silverIS, tinIS, copperIS, nikoliteIS;
	public static ItemStack redAlloyIS, blueAlloyIS, brassIS;
	public static Item indigoDyeItem;
	public static ItemStack indigoDyeIS;
	
	static {
		for(Block b : TubeStuff.findBlocksByClass("com.eloraam.redpower.logic.BlockLogic")) {
			String name0 = Item.itemsList[b.blockID].getItemNameIS(new ItemStack(b, 1, 0));
			if(name0.equals("tile.irtimer"))
				logicBlock = b;
		}
		screwdriverItem = TubeStuff.findItemByClass("com.eloraam.redpower.base.ItemScrewdriver");
		sonicScrewdriverItem = TubeStuff.findItemByClass("com.eloraam.redpower.machine.ItemSonicDriver");
		
		for(Item i : TubeStuff.findItemsByClass("com.eloraam.redpower.core.ItemParts")) {
			String name0 = i.getItemNameIS(new ItemStack(i, 1, 0));
			if(name0.equals("item.ruby"))
				resourceItem = i;
			else if(name0.equals("item.ingotRed"))
				alloyItem = i;
			else if(name0.equals("item.irwafer"))
				logicPartItem = i;
		}
		
		indigoDyeItem = TubeStuff.findItemByClass("com.eloraam.redpower.base.ItemDyeIndigo");
		
		if(resourceItem != null) {
		
			rubyIS = new ItemStack(resourceItem, 1, 0);
			emeraldIS = new ItemStack(resourceItem, 1, 1);
			sapphireIS = new ItemStack(resourceItem, 1, 2);
			silverIS = new ItemStack(resourceItem, 1, 3);
			tinIS = new ItemStack(resourceItem, 1, 4);
			copperIS = new ItemStack(resourceItem, 1, 5);
			nikoliteIS = new ItemStack(resourceItem, 1, 6);
		}
		
		if(alloyItem != null) {
			redAlloyIS = new ItemStack(alloyItem, 1, 0);
			blueAlloyIS = new ItemStack(alloyItem, 1, 1);
			brassIS = new ItemStack(alloyItem, 1, 2);
		}
		
		if(indigoDyeItem != null)
			indigoDyeIS = new ItemStack(indigoDyeItem, 1, 0);
		
		if(logicPartItem != null) {
			stoneWaferIS = new ItemStack(logicPartItem, 1, 0);
			stoneWireIS = new ItemStack(logicPartItem, 1, 1);
			stoneAnodeIS = new ItemStack(logicPartItem, 1, 2);
			stoneCathodeIS = new ItemStack(logicPartItem, 1, 3);
			stonePointerIS = new ItemStack(logicPartItem, 1, 4);
			stoneRedwireIS = new ItemStack(logicPartItem, 1, 5);
			plateAssemblyIS = new ItemStack(logicPartItem, 1, 6);
			siliconChipIS = new ItemStack(logicPartItem, 1, 7);
			taintedChipIS = new ItemStack(logicPartItem, 1, 8);
			stoneBundleIS = new ItemStack(logicPartItem, 1, 9);
		}
		
		if(timerIS != null) {
			timerIS = new ItemStack(logicBlock, 1, 0);
			sequencerIS = new ItemStack(logicBlock, 1, 1);
			stateCellIS = new ItemStack(logicBlock, 1, 2);
			rsLatchIS = new ItemStack(logicBlock, 1, 256);
			norIS = new ItemStack(logicBlock, 1, 257);
			orIS = new ItemStack(logicBlock, 1, 258);
			nandIS = new ItemStack(logicBlock, 1, 259);
			andIS = new ItemStack(logicBlock, 1, 260);
			xnorIS = new ItemStack(logicBlock, 1, 261);
			xorIS = new ItemStack(logicBlock, 1, 262);
			pulseFormerIS = new ItemStack(logicBlock, 1, 263);
			toggleLatchIS = new ItemStack(logicBlock, 1, 264);
			notIS = new ItemStack(logicBlock, 1, 265);
			bufferGateIS = new ItemStack(logicBlock, 1, 266);
			multiplexerIS = new ItemStack(logicBlock, 1, 267);
			repeaterIS = new ItemStack(logicBlock, 1, 268);
			synchronizerIS = new ItemStack(logicBlock, 1, 269);
			randomizerIS = new ItemStack(logicBlock, 1, 270);
			transLatchIS = new ItemStack(logicBlock, 1, 271);
			lightSensorIS = new ItemStack(logicBlock, 1, 272);
			nullCellIS = new ItemStack(logicBlock, 1, 512);
			invertCellIS = new ItemStack(logicBlock, 1, 513);
			nonInvertCellIS = new ItemStack(logicBlock, 1, 514);
			counterIS = new ItemStack(logicBlock, 1, 768);
			busTransceiverIS = new ItemStack(logicBlock, 1, 1024);
		}
	}

	public static boolean useScrewdriver(ItemStack s) {
		if(s == null)
			return false;
		if(screwdriverItem != null && s.itemID == screwdriverItem.itemID) {
			return true;
		}
		if(sonicScrewdriverItem != null && s.itemID == sonicScrewdriverItem.itemID) {
			if(s.getItemDamage() >= 400)
				return false; // empty battery
			return true;
		}
		return false;
	}
}
