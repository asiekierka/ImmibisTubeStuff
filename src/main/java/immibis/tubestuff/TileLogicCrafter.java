package immibis.tubestuff;

import immibis.core.TileBasicInventory;
import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.src.forge.ISidedInventory;

public class TileLogicCrafter extends TileBasicInventory /*implements ISidedInventory*/ {

	/**
	 * Input:
	 * Stone wafer
	 * Stone wire
	 * Stone anode
	 * Stone cathode
	 * Stone pointer
	 * Silicon chip
	 * 
	 * Creates:
	 * Timer, sequencer, state cell, RS latch, NOR, OR, NAND, AND, XNOR, XOR,
	 * pulse former, NOT, buffer gate, multiplexer, repeater, synchronizer,
	 * transparent latch, counter
	 * 
	 * Cannot create: toggle latch, randomizer, light sensor, null cell,
	 * invert cell, non-invert cell, bus transceiver
	 */
	
	public boolean onBlockActivated(EntityPlayer player) {
		player.openGui(TubeStuff.instance, TubeStuff.GUI_LOGICCRAFTER, worldObj, xCoord, yCoord, zCoord);
		return true;
	}
	
	public static final int AREA_SIZE = 4;
	
	public static final int SLOT_WAFER = 0;
	public static final int SLOT_WIRE = 4;
	public static final int SLOT_ANODE = 8;
	public static final int SLOT_CATHODE = 12;
	public static final int SLOT_POINTER = 16;
	public static final int SLOT_CHIP = 20;
	public static final int INVSIZE = 24;

	public TileLogicCrafter() {
		super(INVSIZE, "CNC Machine");
	}
	
	/*
	// Does not allow tube access. TODO: Allow pipe access.
	@Override
	public int getStartInventorySide(int side) {
		return 0;
	}
	@Override
	public int getSizeInventorySide(int side) {
		return 0;
	}*/

}
