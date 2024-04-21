package immibis.tubestuff;

import immibis.core.TileCombined;
import immibis.core.api.porting.SidedProxy;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

public class TileLiquidDuplicator extends TileCombined implements ILiquidTank, ITankContainer, IDuplicator {
	
	public ItemStack item = null;
	public int liquidID = 0, liquidMeta = 0;
	
	private void setLiquid(ItemStack item) {
		LiquidStack l = LiquidContainerRegistry.getLiquidForFilledItem(item);
		if(l == null) {
			liquidID = liquidMeta = 0;
		} else {
			liquidID = l.itemID;
			liquidMeta = l.itemMeta;
		}
	}
	
	private class ItemEditingInventory implements IInventory {

		@Override
		public int getSizeInventory() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int var1) {
			return item;
		}

		@Override
		public ItemStack decrStackSize(int var1, int var2) {
			if(var1 != 0 || item == null)
				return null;
			if(var2 >= item.stackSize) {
				ItemStack i = item;
				item = null;
				return i;
			}
			ItemStack i = item.copy();
			i.stackSize = var2;
			item.stackSize -= var2;
			return i;
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int var1) {
			return null;
		}

		@Override
		public void setInventorySlotContents(int var1, ItemStack var2) {
			if(var1 == 0) {
				item = var2;
				setLiquid(var2);
			}
		}

		@Override
		public String getInvName() {
			return "Duplicator";
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public void onInventoryChanged() {
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer var1) {
			if (!SidedProxy.instance.isOp(var1.username))
				return false;
			if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != TileLiquidDuplicator.this)
	            return false;
	        double distance = var1.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D);
			return distance <= 64;
		}

		@Override
		public void openChest() {
		}

		@Override
		public void closeChest() {
		}
	}
	
	public TileLiquidDuplicator() {
	}
	
	@Override
	public void onPlaced(EntityLiving player, int look) {
		if(!(player instanceof EntityPlayer) || !SidedProxy.instance.isOp(((EntityPlayer)player).username)) {
			if(player instanceof EntityPlayer)
				SidedProxy.instance.sendChat("Only ops can place duplicators.", (EntityPlayer)player);
			worldObj.setBlock(xCoord, yCoord, zCoord, 0);
		}
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		if(worldObj.isRemote)
			return true;
		
		if(!SidedProxy.instance.isOp(player.username)) {
			SidedProxy.instance.sendChat("Only ops can open this GUI.", player);
			return true;
		}
		
		player.openGui(TubeStuff.instance, TubeStuff.GUI_DUPLICATOR, worldObj, xCoord, yCoord, zCoord);
		
		return true;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if(item != null)
		{
			NBTTagCompound itemTag = new NBTTagCompound();
			item.writeToNBT(itemTag);
			tag.setTag("item", itemTag);
		}
		tag.setInteger("lid", liquidID);
		tag.setInteger("lmeta", liquidMeta);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		NBTTagCompound itemTag = tag.getCompoundTag("item");
		if(itemTag != null)
			item = ItemStack.loadItemStackFromNBT(itemTag);
		liquidID = tag.getInteger("lid");
		liquidMeta = tag.getInteger("lmeta");
		if(liquidID < 0 || liquidID > Item.itemsList.length || Item.itemsList[liquidID] == null)
			liquidID = 0;
	}
	
	@Override
	public IInventory getGuiInventory() {
		return new ItemEditingInventory();
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return liquidID == 0 ? null : new LiquidStack(liquidID, maxDrain, liquidMeta);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return liquidID == 0 ? null : new LiquidStack(liquidID, maxDrain, liquidMeta);
	}

	private ILiquidTank[] tanks = {this};
	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return tanks;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return this;
	}

	@Override
	public LiquidStack getLiquid() {
		return liquidID == 0 ? null : new LiquidStack(liquidID, 1000000, liquidMeta);
	}

	@Override
	public int getCapacity() {
		return 1000000;
	}

	@Override
	public int fill(LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public LiquidStack drain(int maxDrain, boolean doDrain) {
		return liquidID == 0 ? null : new LiquidStack(liquidID, maxDrain, liquidMeta);
	}

	@Override
	public int getTankPressure() {
		return 100;
	}
}
