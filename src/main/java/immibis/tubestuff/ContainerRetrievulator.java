package immibis.tubestuff;

import immibis.core.SlotFakeCounted;
import immibis.core.api.util.BaseContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerRetrievulator extends BaseContainer {

	public ContainerRetrievulator(EntityPlayer player, IInventory inv) {
		super(player, inv);
		
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				addSlotToContainer(new Slot(inv, x + y*3 + TileRetrievulator.SLOT_RETRIEVER_SETUP, x*18+10, y*18+35));
				addSlotToContainer(new SlotFakeCounted(inv, x + y*3 + TileRetrievulator.SLOT_TARGET, x*18+67, y*18+35));
			}
		}
		
		addSlotToContainer(new Slot(inv, TileRetrievulator.SLOT_BUFFER, 142, 53));
		
		for(int x = 0; x < 9; x++) {
			for(int y = 0; y < 3; y++)
				addSlotToContainer(new Slot(player.inventory, x + y*9 + 9, x*18 + 13, y*18 + 99));
			addSlotToContainer(new Slot(player.inventory, x, x * 18 + 13, 157));
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(int i) {
		return null;
	}

}
