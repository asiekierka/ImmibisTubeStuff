package immibis.tubestuff;

import static immibis.tubestuff.RedPowerItems.*;
import static immibis.tubestuff.TileLogicCrafter.*;
import immibis.core.api.util.BaseContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerLogicCrafter extends BaseContainer<TileLogicCrafter> {
	
	public TileLogicCrafter tile;
	
	public int countIngredients(int slot) {
		int c = 0;
    	for(int k = slot; k < slot + AREA_SIZE; k++)
    		c += tile.inv.contents[k] != null ? tile.inv.contents[k].stackSize : 0;
    	return c;
    }
	
	public class SlotCrafting extends Slot {
		private int[] ingredients;
		private InventoryCraftResult inv;
		private ItemStack result;
		private Map<Integer, Integer> ingred_counts = new HashMap<Integer, Integer>();
		public SlotCrafting(int x, int y, ItemStack result, int[] ingreds) {
			super(new InventoryCraftResult(), 0, x, y);
			if(ingreds.length != 9)
				throw new IllegalArgumentException("Not 9 ingredients - typoed recipe?");
			inv = (InventoryCraftResult)this.inventory;
			ingredients = ingreds;
			this.result = result;
			
			for(int slot : ingreds) {
				if(ingred_counts.containsKey(slot))
					ingred_counts.put(slot, ingred_counts.get(slot) + 1);
				else
					ingred_counts.put(slot, 1);
			}
		}

		@Override
	    public boolean isItemValid(ItemStack par1ItemStack) {
	        return false;
	    }
	    
	    public void updateSlot() {
	    	inv.setInventorySlotContents(0, checkIngredients() ? result.copy() : null);
	    }
	    
	    public boolean checkIngredients() {
	    	for(Map.Entry<Integer, Integer> e : ingred_counts.entrySet())
	    		if(countIngredients(e.getKey()) < e.getValue())
	    			return false;
	    	return true;
	    }
	    
	    private boolean removeIngredients() {
	    	for(int slot : ingredients) {
	    		boolean found = false;
	    		for(int realSlot = slot; realSlot < slot + AREA_SIZE; realSlot++) {
	    			ItemStack removed = tile.inv.decrStackSize(realSlot, 1);
	    			if(removed != null && removed.stackSize > 0) {
	    				found = true;
	    				break;
	    			}
	    		}
	    		if(!found)
	    			return false;
	    	}
	    	return true;
	    }

	    /**
	     * Called when the player picks up an item from an inventory slot
	     */
	    public void onPickupFromSlot(ItemStack par1ItemStack)
	    {
	        //ModLoader.takenFromCrafting(player, par1ItemStack, this.craftMatrix);
	        //ForgeHooks.onTakenFromCrafting(player, par1ItemStack, craftMatrix);
	        
	    	par1ItemStack.onCrafting(player.worldObj, player, par1ItemStack.stackSize);

	    	if(!removeIngredients())
	    		throw new IllegalStateException("Items were removed from crafting slot, but ingredients were not available.");
	    	
	    	tile.onInventoryChanged();
	    }
	}
	
	public ContainerLogicCrafter(EntityPlayer player, TileLogicCrafter tile) {
		super(player, tile);
		this.tile = tile;
		
		for(int k = 0; k < 4; k++) {
			int x = 34 + k * 18;
			addSlotToContainer(new SlotFiltered(tile, SLOT_WAFER + k, x, 10, stoneWaferIS));
			addSlotToContainer(new SlotFiltered(tile, SLOT_WIRE + k, x, 29, stoneWireIS));
			addSlotToContainer(new SlotFiltered(tile, SLOT_ANODE + k, x, 48, stoneAnodeIS));
			addSlotToContainer(new SlotFiltered(tile, SLOT_CATHODE + k, x, 67, stoneCathodeIS));
			addSlotToContainer(new SlotFiltered(tile, SLOT_POINTER + k, x, 86, stonePointerIS));
			addSlotToContainer(new SlotFiltered(tile, SLOT_CHIP + k, x, 105, siliconChipIS));
		}
		
		for(int x = 0; x < 9; x++) {
			for(int y = 0; y < 3; y++)
				addSlotToContainer(new Slot(player.inventory, x + y*9 + 9, x*18 + 13, y*18 + 168));
			addSlotToContainer(new Slot(player.inventory, x, x * 18 + 13, 226));
		}
		
		addSlotToContainer(new SlotCrafting(115, 10, timerIS,			new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_POINTER, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(137, 10, sequencerIS,		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_POINTER}));
		addSlotToContainer(new SlotCrafting(159, 10, stateCellIS,		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_POINTER, SLOT_CHIP, SLOT_CATHODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(115, 32, rsLatchIS,    		new int[] {SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(137, 32, norIS,        		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(159, 32, orIS,         		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE}));
		addSlotToContainer(new SlotCrafting(115, 54, nandIS,       		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(137, 54, andIS,        		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(159, 54, xnorIS,       		new int[] {SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(115, 76, xorIS,        		new int[] {SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(137, 76, pulseFormerIS,		new int[] {SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(159, 76, notIS, 			new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(115, 98, bufferGateIS, 		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(137, 98, multiplexerIS, 	new int[] {SLOT_WAFER, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE, SLOT_ANODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(159, 98, repeaterIS, 		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(115,120, synchronizerIS, 	new int[] {SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_WIRE, SLOT_ANODE, SLOT_CATHODE, SLOT_CHIP, SLOT_CHIP}));
		addSlotToContainer(new SlotCrafting(137,120, transLatchIS, 		new int[] {SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_CATHODE, SLOT_ANODE}));
		addSlotToContainer(new SlotCrafting(159,120, counterIS, 		new int[] {SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WAFER, SLOT_WIRE, SLOT_WIRE, SLOT_POINTER, SLOT_CATHODE, SLOT_CATHODE}));
	}
	
	@Override
	public ItemStack slotClick(int a, int b, int c, EntityPlayer d) {
		ItemStack result = super.slotClick(a, b, c, d);
		updateCrafting();
		return result;
	}
	
	@Override
	public ItemStack transferStackInSlot(int slot) {
		return null;
	}
	
	public void updateCrafting() {
		for(Slot s : (List<Slot>)inventorySlots)
			if(s instanceof SlotCrafting)
				((SlotCrafting) s).updateSlot();
	}

}
