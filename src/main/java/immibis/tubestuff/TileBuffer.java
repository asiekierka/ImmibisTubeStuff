package immibis.tubestuff;

import cpw.mods.fml.common.FMLLog;
import immibis.core.TileBasicInventory;
import immibis.core.api.util.Dir;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileBuffer extends TileBasicInventory {

	public static final int INVSIZE = 18;
	
	public TileBuffer() {
		super(INVSIZE, "Buffer");
	}

	protected int update_ticks = 0;
	private boolean inv_empty = false;
	private boolean find_pipe = true;
	private TileEntity out_pipe = null;
	protected int delay = 20;
	
	private static final int MIN_DELAY = 10;
	private static final int MAX_DELAY = 100;
	
	private static Class<?> class_PipeTransportItems = null, class_EntityPassiveItem = null, class_TileGenericPipe = null;
	private static Class<?> class_Pipe = null;
	private static Constructor<?> cons_EntityPassiveItem = null;
	private static Method meth_entityEntering = null;
	private static Field field_transport = null, field_pipe = null;
	
	private boolean enterPipe(ItemStack stack) {
		if(out_pipe == null)
			return true;
		
		try {
			Object dir = null;
			if(out_pipe.xCoord < xCoord)
				dir = ForgeDirection.VALID_DIRECTIONS[Dir.NX];
			else if(out_pipe.xCoord > xCoord)
				dir = ForgeDirection.VALID_DIRECTIONS[Dir.PX];
			else if(out_pipe.yCoord < yCoord)
				dir = ForgeDirection.VALID_DIRECTIONS[Dir.NY];
			else if(out_pipe.yCoord > yCoord)
				dir = ForgeDirection.VALID_DIRECTIONS[Dir.PY];
			else if(out_pipe.zCoord < zCoord)
				dir = ForgeDirection.VALID_DIRECTIONS[Dir.NZ];
			else if(out_pipe.zCoord > zCoord)
				dir = ForgeDirection.VALID_DIRECTIONS[Dir.PZ];
			
			Object item = cons_EntityPassiveItem.newInstance(worldObj, (double)(xCoord + out_pipe.xCoord + 1)/2, (double)(yCoord + out_pipe.yCoord)/2, (double)(zCoord + out_pipe.zCoord + 1)/2, stack);
			meth_entityEntering.invoke(field_transport.get(field_pipe.get(out_pipe)), item, dir);
		} catch (Exception e) {
			e.printStackTrace();
			out_pipe = null;
			return false;
		}
		return true;
	}
	
	static {
		try {
			ClassLoader cl = TileBuffer.class.getClassLoader();

			class_PipeTransportItems = cl.loadClass("buildcraft.transport.PipeTransportItems");
			class_Pipe = cl.loadClass("buildcraft.transport.Pipe");
			class_EntityPassiveItem = cl.loadClass("buildcraft.core.EntityPassiveItem");
			class_TileGenericPipe = cl.loadClass("buildcraft.transport.TileGenericPipe");
			cons_EntityPassiveItem = class_EntityPassiveItem.getConstructor(World.class, Double.TYPE, Double.TYPE, Double.TYPE, ItemStack.class);
			meth_entityEntering = class_PipeTransportItems.getMethod("entityEntering", new Class[] {cl.loadClass("buildcraft.api.transport.IPipedItem"), ForgeDirection.class});
			field_pipe = class_TileGenericPipe.getField("pipe");
			field_transport = class_Pipe.getField("transport");
		} catch (Exception e) {
			FMLLog.getLogger().info("Could not access BuildCraft for buffer output because:");
			FMLLog.getLogger().info(e.getClass().getName()+": "+e.getMessage());
			
			class_PipeTransportItems = null;
			class_Pipe = null;
			class_EntityPassiveItem = null;
			class_TileGenericPipe = null;
			cons_EntityPassiveItem = null;
			meth_entityEntering = null;
			field_pipe = null;
			field_transport = null;
		}
	}
	
	private void checkPipe(TileEntity te)
	{
		if(te != null && out_pipe == null && te.getClass().getName().equals("buildcraft.transport.TileGenericPipe"))
		{
			try
			{
				String pipeclass = field_pipe.get(te).getClass().getName();
				if(pipeclass.equals("buildcraft.transport.pipes.PipeItemsWood")
				|| pipeclass.equals("buildcraft.transport.pipes.PipeItemsGold")
				|| pipeclass.equals("buildcraft.krapht.pipes.PipeItemsBasicLogistics"))
					out_pipe = te;
			} catch(Exception e) {
			}
		}
	}
	
	protected int chooseNextSlotToEmit() {
		ItemStack[] contents = inv.contents;
		int best = 0;
		int best_size = 0;
		int worst = 0;
		int worst_size = 64;
		int used = 0;
		for(int k = 0; k < INVSIZE; k++)
		{
			if(contents[k] == null)
				continue;
			++used;
			if(contents[k].stackSize > best_size)
			{
				best_size = contents[k].stackSize;
				best = k;
			}
			if(contents[k].stackSize < worst_size)
			{
				worst_size = contents[k].stackSize;
				worst = k;
			}
		}
		
		if(best_size == 0 || used == 0)
		{
			inv_empty = true;
			return -1;
		}
		
		delay = MAX_DELAY + (int)((MIN_DELAY - MAX_DELAY) * (used / (double)INVSIZE));
		
		if(best_size < contents[best].getMaxStackSize() / 2)
		{
			best_size = worst_size;
			best = worst;
		}
		return best;
	}

	@Override
	public void updateEntity() {
		if(inv_empty || worldObj.isRemote)
			return;
		if(++update_ticks < delay)
		{
			if(update_ticks > 2 && redstone_output)
			{
				redstone_output = false;
				notifyNeighbouringBlocks();
			}
			return;
		}
		update_ticks = 0;
		
		// find largest stack
		int best = chooseNextSlotToEmit();
		ItemStack[] contents = inv.contents;
		if(best < 0)
			return;
		
		if(best != 0)
		{
			// put it in the first slot so filters will grab it first
			ItemStack temp = contents[best];
			contents[best] = contents[0];
			contents[0] = temp;
			onInventoryChanged();
		}
		
		if(find_pipe)
		{
			out_pipe = null;
			
			checkPipe(worldObj.getBlockTileEntity(xCoord-1, yCoord, zCoord));
			checkPipe(worldObj.getBlockTileEntity(xCoord+1, yCoord, zCoord));
			checkPipe(worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord));
			checkPipe(worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord));
			checkPipe(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord-1));
			checkPipe(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord+1));
			
			find_pipe = false;
		}
		
		if(out_pipe != null)
		{
			if(contents[0] != null)
			{
				if(enterPipe(contents[0]))
				{
					contents[0] = null;
					onInventoryChanged();
				}
				else
					out_pipe = null;
			}
		}
		else if(!redstone_output)
		{
			redstone_output = true;
			notifyNeighbouringBlocks();
		}
	}
	
	@Override
	public void onBlockNeighbourChange() {
		find_pipe = true;
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		player.openGui(TubeStuff.instance, TubeStuff.GUI_BUFFER, worldObj, xCoord, yCoord, zCoord);
		return true;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        super.setInventorySlotContents(i, itemstack);
        inv_empty = false;
    }
}
