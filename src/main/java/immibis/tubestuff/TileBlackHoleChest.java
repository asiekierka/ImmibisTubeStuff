package immibis.tubestuff;

import immibis.core.TileCombined;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TileBlackHoleChest extends TileCombined implements IInventory {
	public List<ItemStack> items = new ArrayList<ItemStack>();
	
	int maxPages = 1;
	
	public static final int PAGESIZE = ContainerBlackHoleChest.WIDTH * ContainerBlackHoleChest.HEIGHT;
	
	public void updateMaxPages() {
		if(worldObj.isRemote)
			return;
		while(items.size() > 0 && items.get(items.size() - 1) == null)
			items.remove(items.size() - 1);
		maxPages = Math.max(1, (items.size() + PAGESIZE) / PAGESIZE);
	}
	
	public TileBlackHoleChest() {
	}
	
	//@Override
	public boolean canUpdate() {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		player.openGui(TubeStuff.instance, TubeStuff.GUI_CHEST, worldObj, xCoord, yCoord, zCoord);
		return true;
	}

	@Override
	public int getSizeInventory() {
		return (items.size() + PAGESIZE) / PAGESIZE * PAGESIZE;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return (i < items.size() ? items.get(i) : null);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(items.get(i) == null)
			return null;
		if(items.get(i).stackSize <= j)
		{
			ItemStack old = items.get(i);
			items.set(i, null);
			updateMaxPages();
			return old;
		}
		return items.get(i).splitStack(j);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);

        NBTTagList tag = nbttagcompound.getTagList("Items");
        items = new ArrayList<ItemStack>(tag.tagCount());
		for(int i = 0; i < tag.tagCount(); i++)
			items.add(ItemStack.loadItemStackFromNBT((NBTTagCompound)tag.tagAt(i)));
		nbttagcompound.setTag("Items", tag);
		
		maxPages = -1;
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        
        NBTTagList tag = new NBTTagList("Items");
		for(int i = 0; i < items.size(); i++)
		{
			ItemStack item = items.get(i);
			if(item == null)
				continue;
			NBTTagCompound item_nbt = new NBTTagCompound();
			item.writeToNBT(item_nbt);
			tag.appendTag(item_nbt);
		}
        nbttagcompound.setTag("Items", tag);
    }

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		i = Math.min(i, items.size());
		if(i == items.size())
			items.add(itemstack);
		else
			items.set(i, itemstack);
		updateMaxPages();
	}

	@Override
	public String getInvName() {
		return "Black hole chest";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
            return false;
        double distance = entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D);
		return distance <= 64;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}
}
