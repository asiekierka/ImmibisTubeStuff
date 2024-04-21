package immibis.tubestuff;

import immibis.core.BasicInventory;
import immibis.core.TileBasicInventory;
import immibis.core.api.util.Dir;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TileRetrievulator extends TileBasicInventory implements ISidedInventory {
	
	public int outputFace;
	
	private int ticksSinceItemPassed = 0;
	private int pulseInterval = 10;
	private int ticksToNextPulse = 0;
	
	private boolean incorrectSetup = true;
	private boolean needAnyItems = false;
	
	@Override
	public List<ItemStack> getInventoryDrops() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for(int k = 0; k < 9; k++)
			dropStack(k + SLOT_RETRIEVER_SETUP, list);
		dropStack(SLOT_BUFFER, list);
		return list;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("outputFace", outputFace);
		tag.setInteger("itemTicks", ticksSinceItemPassed);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		outputFace = tag.getInteger("outputFace");
		ticksSinceItemPassed = tag.getInteger("itemTicks");
	}
	
	@Override
	public void onDataPacket(Packet132TileEntityData pkt) {
		outputFace = pkt.actionType;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		Packet132TileEntityData p = new Packet132TileEntityData();
		p.actionType = outputFace;
		p.xPosition = xCoord;
		p.yPosition = yCoord;
		p.zPosition = zCoord;
		p.isChunkDataPacket = true;
		return p;
	}
	
	public static final int SLOT_RETRIEVER_SETUP = 0;
	public static final int SLOT_TARGET = 9;
	public static final int SLOT_BUFFER = 18;

	public TileRetrievulator() {
		super(19, "Retrievulator");
	}
	
	@Override
	public void onPlaced(EntityLiving player, int dir) {
		outputFace = dir;
	}
	
	private TileEntity getTarget(int side) {
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		switch(side) {
		case Dir.NX: x--; break;
		case Dir.NY: y--; break;
		case Dir.NZ: z--; break;
		case Dir.PX: x++; break;
		case Dir.PY: y++; break;
		case Dir.PZ: z++; break;
		}
		return worldObj.getBlockTileEntity(x, y, z);
	}
	
	private int updateTicks = 0;
	private TileEntity retriever;
	private IInventory target;
	private int targetSlotsStart; // The first slot in the target inventory to use
	private int targetSlotsEnd; // The slot AFTER the last slot in the target inventory to use
	
	@Override
	public void onBlockNeighbourChange() {
		updateTicks = 0;
	}
	
	@Override
	public void updateEntity() {
		if(worldObj.isRemote)
			return;
		
		if(updateTicks <= 0) {
			updateTicks = 20;
			TileEntity tetarget = getTarget(outputFace);
			retriever = getTarget(outputFace ^ 1);
			if(retriever != null && !isValidRetrieverClass(retriever.getClass().getName()))
				retriever = null;
			if(!(tetarget instanceof IInventory))
				target = null;
			else
				target = ((IInventory)tetarget);
		}
		
		updateTicks--;
		if(target == null || retriever == null)
			return;
		
		if(target instanceof ISidedInventory) {
			ISidedInventory isi = (ISidedInventory)target;
			int side = outputFace ^ 1;
			targetSlotsStart = isi.getStartInventorySide(ForgeDirection.values()[side]);
			targetSlotsEnd = targetSlotsStart + isi.getSizeInventorySide(ForgeDirection.values()[side]);
		} else {
			targetSlotsStart = 0;
			targetSlotsEnd = ((IInventory)target).getSizeInventory();
		}
		
		if(inv.contents[SLOT_BUFFER] != null) {
			BasicInventory.mergeStackIntoRange(inv, target, SLOT_BUFFER, targetSlotsStart, targetSlotsEnd);
			if(inv.contents[SLOT_BUFFER] != null) {
				return;
			}
			shuffleInventories();
			ticksSinceItemPassed = 0;
		} else
			ticksSinceItemPassed++;
		
		if(ticksToNextPulse == 0) {
			if(!redstone_output)
				shuffleInventories();
			
			boolean oldOutput = redstone_output;
			
			if(!needAnyItems)
				redstone_output = false;
			else
				redstone_output = !redstone_output;
			
			if(redstone_output != oldOutput) {
				notifyNeighbouringBlocks();
				worldObj.markBlockForUpdate(retriever.xCoord, retriever.yCoord, retriever.zCoord);
			}
			
			if(!redstone_output) {
				pulseInterval = calcPulseInterval();
				ticksToNextPulse = pulseInterval;
			} else
				ticksToNextPulse = 10;
			
		} else
			ticksToNextPulse--;
	}
	
	private int calcPulseInterval() {
		final int MAX_INTERVAL = 400;
		final int MIN_INTERVAL = 10;
		return Math.min(MAX_INTERVAL, Math.max(MIN_INTERVAL, ticksSinceItemPassed / 3));
	}

	/**
	 * Moves items between the R grid and the retriever depending on
	 * the T grid and the contents of the target inventory.
	 * 
	 * Sets incorrectSetup to true if the retrievulator has a setup error
	 * (such as filling the same slots of the T grid and retriever) or
	 * false if everything is OK.
	 * 
	 * Sets needAnyItems to true if the retrievulator needs to request
	 * any items - i.e. if there should not be a retriever jammer in
	 * the retriever.
	 * 
	 * Handles spawning and deletion of retriever jammers.
	 */
	private void shuffleInventories() {
		incorrectSetup = false;
		needAnyItems = false;
		
		IInventory iir = (IInventory)retriever;
		for(int k = 0; k < 9; k++) {
			boolean retriever_filled = iir.getStackInSlot(k) != null && iir.getStackInSlot(k).itemID != TubeStuff.itemUseless.itemID;
			boolean this_filled = inv.contents[SLOT_RETRIEVER_SETUP + k] != null;
			if(inv.contents[SLOT_TARGET + k] == null) {
				// If no target item, there should be no filter items either
				incorrectSetup |= retriever_filled;
				continue;
			}
			
			if(!checkTarget(targetSlotsStart, targetSlotsEnd, target, inv.contents[SLOT_TARGET + k])) {
				// Not enough items in target
				if(this_filled && !retriever_filled) {
					// so add this to the retriever's filter
					iir.setInventorySlotContents(k, getStackInSlot(SLOT_RETRIEVER_SETUP + k));
					setInventorySlotContents(SLOT_RETRIEVER_SETUP + k, null);
				} else if(this_filled || !retriever_filled)
					incorrectSetup = true;
				needAnyItems = true;
				
			} else {
				// Enough items in the target
				if(retriever_filled && !this_filled) {
					// so remove this from the retriever's filter
					setInventorySlotContents(SLOT_RETRIEVER_SETUP + k, iir.getStackInSlot(k));
					iir.setInventorySlotContents(k, null);
				} else if(!this_filled || retriever_filled)
					incorrectSetup = true;
			}
		}
		
		if(!incorrectSetup) {
			ItemStack slot0 = iir.getStackInSlot(0);
			
			if(needAnyItems) {
				if(slot0 != null && slot0.itemID == TubeStuff.itemUseless.itemID) {
					iir.setInventorySlotContents(0, null);
				}
			} else {
				if(slot0 == null) {
					iir.setInventorySlotContents(0, new ItemStack(TubeStuff.itemUseless, 1, 0));
				}
			}
		}
	}

	private boolean isValidRetrieverClass(String name) {
		if(name.equals("com.eloraam.redpower.machine.TileRetriever"))
			return true;
		if(name.equals("com.eloraam.redpower.machine.TileFilter"))
			return true;
		return false;
	}

	private boolean checkTarget(int start, int end, IInventory iinv, ItemStack tis) {
		if(tis == null)
			return true;
		int need = tis.stackSize;
		for(int slot = start; slot < end && need > 0; slot++) {
			ItemStack ais = iinv.getStackInSlot(slot);
			if(ais == null)
				continue;
			if(TubeStuff.areItemsEqual(tis, ais))
				need -= ais.stackSize;
		}
		return need <= 0;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side) {
		return SLOT_BUFFER;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side) {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer ply) {
		if(worldObj.isRemote)
			return true;
		
		ItemStack h = ply.getCurrentEquippedItem();
		if(RedPowerItems.useScrewdriver(h)) {
			outputFace = (outputFace + 1) % 6;
			resendDescriptionPacket();
			return true;
		}
		ply.openGui(TubeStuff.instance, TubeStuff.GUI_RETRIEVULATOR, worldObj, xCoord, yCoord, zCoord);
		return true;
	}
}
