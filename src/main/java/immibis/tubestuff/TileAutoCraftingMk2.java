package immibis.tubestuff;

import cpw.mods.fml.common.FMLLog;
import ic2.core.AdvRecipe;
import ic2.core.AdvShapelessRecipe;
import immibis.core.TileBasicInventory;
import immibis.core.api.util.Dir;
import immibis.core.net.TESync;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class TileAutoCraftingMk2 extends TileBasicInventory implements ISidedInventory {
	// 1 slot output (0)
	// 9 slots recipe (1-9)
	// 9x4 slots input (10-45)
	// 9 slots overflow/input (46-54)
	static final int SLOT_OUTPUT = 0;
	static final int START_RECIPE = SLOT_OUTPUT + 1;
	static final int SIZE_RECIPE = 9;
	static final int START_INPUT = START_RECIPE + SIZE_RECIPE;
	static final int SIZE_INPUT = 9*5;
	static final int START_OVERFLOW = START_INPUT + 9*4;
	static final int SIZE_OVERFLOW = 9;
	static final int INVSIZE = START_INPUT + SIZE_INPUT;
	
	// recipeInputs[slotNumber] = array of possible stacks to fill that slot
	public ItemStack[][] recipeInputs = new ItemStack[9][0];
	protected IRecipe cachedRecipe;
	private boolean invChanged = true;
	private boolean recipeChanged = true;
	private boolean outputFull = false;
	private boolean insufficientInput = false;
	
	public boolean craftMany = false; // If false, crafts only if output slot is empty.
	
	public byte outputFace = Dir.PY;
	
	// invSideMap[outputFace][inputFace] = which line of the inventory this input face maps to (0-4) 
	private static final int[][] invSideMap;
	static {
		invSideMap = new int[6][6];
		for(int out = 0; out < 6; out++) {
			int n = 0;
			for(int in = 0; in < 6; in++) {
				if(in != out && in != (out ^ 1))
					invSideMap[out][in] = n++;
			}
			invSideMap[out][out] = -1;
			invSideMap[out][out ^ 1] = 4;
		}
		
		// +Y is special-cased for backwards compatibility
		for(int k = 2; k < 6; k++)
			invSideMap[Dir.PY][k] = k - 2;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		return TESync.make132(xCoord, yCoord, zCoord, outputFace, 0, 0);
	}
	
	@Override
	public void onDataPacket(Packet132TileEntityData packet) {
		outputFace = (byte)TESync.getFirst(packet);
	}
	
	private static boolean printedUnknownWarning;

	@Override
	public int getStartInventorySide(ForgeDirection dir) {
		if(dir.ordinal() == outputFace)
			return SLOT_OUTPUT;
		else if(dir == ForgeDirection.UNKNOWN) {
			if(!printedUnknownWarning) {
				System.err.println("[TubeStuff] A mod tried to access an ACT2 from an invalid side.");
				System.err.println("[TubeStuff] You should probably report this as a bug. Here's a stack trace so you can tell which mod it is:");
				new Exception("Stack trace").printStackTrace();
				printedUnknownWarning = true;
			}
			return START_INPUT;
		} else
			return START_INPUT + 9 * invSideMap[outputFace][dir.ordinal()];
	}

	@Override
	public int getSizeInventorySide(ForgeDirection dir) {
		return dir.ordinal() == outputFace ? (inv.contents[SLOT_OUTPUT] != null ? 1 : 0) :
			dir == ForgeDirection.UNKNOWN ? 45 : 9;
	}
	
	public TileAutoCraftingMk2() {
		super(INVSIZE, "ACT Mk II");
	}
	
	private InventoryCrafting makeInventoryCrafting(int start) {
		InventoryCraftingACT2 ic = new InventoryCraftingACT2();
		for(int k = 0; k < 9; k++)
			ic.setInventorySlotContents(k, inv.contents[k + start]);
		return ic;
	}
	
	@SuppressWarnings("unchecked")
	void cacheOutput() {
		
		InventoryCrafting ic = makeInventoryCrafting(START_RECIPE);
		cachedRecipe = null;
		for(IRecipe r : (List<IRecipe>)CraftingManager.getInstance().getRecipeList())
		{
			if(r.matches(ic, worldObj))
			{
				cachedRecipe = r;
				break;
			}
		}
	}
	
	private void removeAllMultiInputs() {
		boolean needUpdate = false;
		for(int k = 0; k < SIZE_RECIPE; k++)
			if(recipeInputs[k].length > 1) {
				recipeInputs[k] = new ItemStack[] {recipeInputs[k][0]};
				needUpdate = true;
			}
		cycleRecipe();
		
		if(needUpdate) {
			// tell the container to resend the recipe
			// why is this necessary? i don't know
			forceContainerUpdateCount++;
		}
	}
	
	private void setMultiInputsFromShapedOreRecipe(ShapedOreRecipe sor) {
		int width, height;
		Object[] input;
		
		width = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, sor, "width");
		height = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, sor, "height");
		input = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, sor, "input");

		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				int slot = x + y * 3; 
				
				ItemStack[] new_multi;
				
				Object recipe_item = null;
				if(x < width && y < height)
					recipe_item = input[x + y * width];
				
				if(recipe_item instanceof ItemStack) {
					new_multi = new ItemStack[] {(ItemStack)recipe_item};
				} else if(recipe_item instanceof List) {
					new_multi = (ItemStack[])((List)recipe_item).toArray(new ItemStack[0]);
				} else if(recipe_item == null) {
					new_multi = new ItemStack[0];
				} else {
					removeAllMultiInputs();
					return;
				}
				
				recipeInputs[slot] = zeroStackSizes(new_multi);
				
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setMultiInputsFromAdvRecipe(IRecipe r) throws Throwable {
		// handles IC2 shaped recipes
		AdvRecipe ar = (AdvRecipe)r;
		
		int width = ar.inputWidth;
		int height = ar.input.length / width;
		Object[] input = ar.input;
		
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				int slot = x + y * 3;
				
				Object recipe_item = null;
				if(x < width && y < height)
					recipe_item = input[x + y*width];
				
				if(recipe_item == null)
					recipeInputs[slot] = new ItemStack[0];
				else {
					List<ItemStack> allowedInputs = AdvRecipe.resolveOreDict(recipe_item);
					recipeInputs[slot] = zeroStackSizes((ItemStack[])allowedInputs.toArray(new ItemStack[0]));
				}
			}
		}
	}
	
	private void setMultiInputsFromShapelessOreRecipe(ShapelessOreRecipe r) throws Throwable {
		List<Object> input = ReflectionHelper.getPrivateValue(ShapelessOreRecipe.class, r, "input");
		
		int pos = 0;
		
		for(int y = 0; y < 3; y++) {
			for(int x = 0; x < 3; x++, pos++) {
				if(pos < input.size()) {
					Object ri = input.get(pos);
					if(ri instanceof ItemStack)
						recipeInputs[pos] = zeroStackSizes(new ItemStack[] {(ItemStack)ri});
					else if(ri instanceof List)
						recipeInputs[pos] = zeroStackSizes((ItemStack[])((List)ri).toArray(new ItemStack[0]));
					else
						throw new Throwable("unknown object '"+ri+"' with type '"+ri.getClass().getName()+"' in ShapelessOreRecipe");
				} else {
					recipeInputs[pos] = new ItemStack[0];
				}
			}
		}
	}
	
	private void setMultiInputsFromAdvShapelessRecipe(IRecipe r) throws Throwable {
		AdvShapelessRecipe asr = (AdvShapelessRecipe)r;
		Object[] input = asr.input;
		
		int pos = 0;
		
		for(int y = 0; y < 3; y++) {
			for(int x = 0; x < 3; x++, pos++) {
				if(pos < input.length) {
					List<ItemStack> allowedItems = AdvRecipe.resolveOreDict(input[pos]);
					recipeInputs[pos] = zeroStackSizes((ItemStack[])allowedItems.toArray(new ItemStack[0]));
				} else {
					recipeInputs[pos] = new ItemStack[0];
				}
			}
		}
	}
	
	private ItemStack[] zeroStackSizes(ItemStack[] ar) {
		ItemStack[] rv = new ItemStack[ar.length];
		for(int k = 0; k < ar.length; k++) {
			if(ar[k] == null)
				rv[k] = null;
			else {
				ItemStack is = ar[k].copy();
				is.stackSize = 0;
				rv[k] = is;
			}
		}
		return rv;
	}
	
	private boolean printedSMIFRException = false;
	
	public void setMultiInputsFromRecipe() {
		IRecipe r = cachedRecipe;
		if(r == null) {
			removeAllMultiInputs();
			return;
		}
		
		try {
			if(r instanceof ShapedOreRecipe) {
				setMultiInputsFromShapedOreRecipe((ShapedOreRecipe)r);
				cycleRecipe();
				
			} else if(r instanceof ShapelessOreRecipe) {
				setMultiInputsFromShapelessOreRecipe((ShapelessOreRecipe)r);
				cycleRecipe();
				
			} else {
				
				// try IC2 recipes, in a try-catch block as IC2 might not be installed
				try {
					if(r instanceof AdvRecipe) {
						setMultiInputsFromAdvRecipe(r);
						cycleRecipe();
						return;
					}
					if(r instanceof AdvShapelessRecipe) {
						setMultiInputsFromAdvShapelessRecipe(r);
						cycleRecipe();
						return;
					}
				} catch(Throwable t) {
				}
				
				removeAllMultiInputs();
			}
		} catch(Throwable e) {
			if(!printedSMIFRException) {
				new Exception("[TubeStuff] setMultiInputsFromRecipe failed. This error will only be shown once.", e).printStackTrace();
				printedSMIFRException = true;
			}
			removeAllMultiInputs();
		}
	}
	
	private boolean canUseItem(ItemStack[] allowedInputs, ItemStack item) {
		for(ItemStack is : allowedInputs)
			if(TubeStuff.areItemsEqual(is, item))
				return true;
		return false;
	}

	private void makeOutput() {
		if(inv.contents[SLOT_OUTPUT] != null && !craftMany)
			return;
		if(cachedRecipe == null)
			return;
		
		// slotMap[recipe slot] == input slot or -1
		int[] slotMap = new int[9];
		// used[input slot] == number of items to be used from this slot
		int[] used = new int[45];
		for(int k = 0; k < 9; k++)
		{
			ItemStack[] recipe = recipeInputs[k];
			slotMap[k] = -1;
			if(recipe.length == 0)
				continue;
			for(int i = 44; i >= 0; i--)
			{
				ItemStack input = inv.contents[i + START_INPUT];
				if(input == null || used[i] >= input.stackSize)
					continue;
				if(canUseItem(recipe, input))
				{ 
					slotMap[k] = i;
					used[i]++;
					break;
				}
			}
			if(slotMap[k] == -1)
			{
				insufficientInput = true;
				return;
			}
		}
		
		InventoryCraftingACT2 ic = new InventoryCraftingACT2();
		for(int k = 0; k < 9; k++)
			if(slotMap[k] != -1)
				ic.setInventorySlotContents(k, inv.contents[slotMap[k] + START_INPUT]);
		
		if(!cachedRecipe.matches(ic, worldObj))
		{
			insufficientInput = true;
			return;
		}
		
		if(inv.contents[SLOT_OUTPUT] == null) {
			for(int k = 0; k < 9; k++)
				if(slotMap[k] != -1)
					ic.setInventorySlotContents(k, inv.decrStackSize(slotMap[k] + START_INPUT, 1));
			inv.contents[SLOT_OUTPUT] = cachedRecipe.getCraftingResult(ic);
			decreaseInput(ic, inv.contents[SLOT_OUTPUT]);
			
		} else {
			for(int k = 0; k < 9; k++)
				if(slotMap[k] != -1) {
					// don't take the items out of the inventory yet
					ItemStack stack = inv.getStackInSlot(slotMap[k] + START_INPUT).copy();
					stack.stackSize = 1;
					ic.setInventorySlotContents(k, stack);
				}
			ItemStack output = cachedRecipe.getCraftingResult(ic);
			if(output == null || !TubeStuff.areItemsEqual(output, inv.contents[SLOT_OUTPUT])) {
				outputFull = true;
				return;
			}
			
			int newStackSize = output.stackSize + inv.contents[SLOT_OUTPUT].stackSize;
			if(newStackSize > getInventoryStackLimit() || newStackSize > output.getMaxStackSize()) {
				outputFull = true;
				return;
			}
			
			inv.contents[SLOT_OUTPUT].stackSize = newStackSize;
			decreaseInput(ic, inv.contents[SLOT_OUTPUT]);
			
			for(int k = 0; k < 9; k++)
				if(slotMap[k] != -1)
					// now the craft has succeeded, take the items from the inventory
					inv.decrStackSize(START_INPUT + slotMap[k], 1);
		}
		
		for(int k = 0; k < 9; k++)
		{
			ItemStack leftover = ic.getStackInSlot(k);
			if(leftover != null)
				// if this fails, the item is destroyed as there's nowhere to put it
				inv.mergeStackIntoRange(leftover, START_OVERFLOW, START_OVERFLOW + SIZE_OVERFLOW);
		}
		
		invChanged = true;
		onInventoryChanged();
	}
	
	private static boolean allowCraftingHook = true;
	
	private void decreaseInput(InventoryCrafting ic, ItemStack output)
	{
		if(allowCraftingHook) {
			try {
				GameRegistry.onItemCrafted(TubeStuff.fakePlayer(worldObj), output, ic);
			} catch(Exception e) {
				allowCraftingHook = false;
				e.printStackTrace();
				FMLLog.getLogger().log(Level.WARNING, "TubeStuff: This happened when trying to call a crafting hook with a fake player. I won't try that again, but this may cause some bugs. If you can tell which mod caused the problem, bug its author to fix it.");
			}
		}
		
        for(int i = 0; i < ic.getSizeInventory(); i++)
        {
            ItemStack input = ic.getStackInSlot(i);
            if (input == null)
                continue;
            ic.decrStackSize(i, 1);
            if(input.getItem().hasContainerItem())
            {
            	ItemStack container = input.getItem().getContainerItemStack(input);
            	
            	if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage())
                {
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(TubeStuff.fakePlayer(worldObj), container));
                    container = null;
                }
            	
            	if(ic.getStackInSlot(i) == null)
            		ic.setInventorySlotContents(i, container);
            	else if(container != null)
            	{
            		// if this fails, item is lost because there's nowhere to put it
            		inv.mergeStackIntoRange(container, START_OVERFLOW, START_OVERFLOW + SIZE_OVERFLOW);
            	}
            }
        }
	}
	
	private int recipeCycleTicks = 0;
	
	private Random random = new Random();
	
	private void cycleRecipe() {
		recipeCycleTicks = 0;
		for(int k = 0; k < 9; k++) {
			ItemStack[] ri = recipeInputs[k];
			ItemStack is;
			if(ri.length == 0)
				is = null;
			else if(ri.length == 1)
				is = ri[0];
			else
				is = ri[random.nextInt(ri.length)];
			
			if(is != null && is.stackSize > 0) {
				is = is.copy();
				is.stackSize = 0;
			}
			
			inv.contents[k + START_RECIPE] = is;
		}
	}
	
	@Override
	public void updateEntity() {
		if(worldObj.isRemote)
			return;
		
		if(recipeCycleTicks < 0 || recipeCycleTicks++ > 20) {
			recipeCycleTicks = 0;
			
			cycleRecipe();
		}
		
		// workaround for some things not triggering slotChanging
		insufficientInput = false;
		
		if(recipeChanged)
		{
			cacheOutput();
			recipeChanged = false;
		}
		
		if(inv.contents[SLOT_OUTPUT] == null || craftMany)
		{
			if(cachedRecipe != null && !insufficientInput)
				makeOutput();
		}
		if(inv.contents[SLOT_OUTPUT] != null) {
			/*pulse_ticks = (pulse_ticks + 1) % 20;
			if(pulse_ticks == 0) {
				redstone_output = !redstone_output;
				notifyNeighbouringBlocks();
			}*/
		} else if(redstone_output) {
			redstone_output = false;
			notifyNeighbouringBlocks();
		}
		if(invChanged)
		{
			invChanged = false;
			insufficientInput = false;
		}
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		if(TubeStuff.isValidWrench(player.inventory.getCurrentItem())) {
			outputFace = (byte)((outputFace + 1) % 6);
			resendDescriptionPacket();
			return true;
		}
		
		player.openGui(TubeStuff.instance, TubeStuff.GUI_TABLE, worldObj, xCoord, yCoord, zCoord);
		return true;
	}
	
	private void slotChanging(int i)
	{
		if(i == SLOT_OUTPUT)
			outputFull = false;
		if(i >= START_RECIPE && i < START_RECIPE + SIZE_RECIPE)
			recipeChanged = true;
		invChanged = true;
		insufficientInput = false;
	}
	
	@Override
	public ItemStack decrStackSize(int i, int j) {
		slotChanging(i);
		ItemStack rv = super.decrStackSize(i, j);
		slotChanged(i);
		return rv;
	}
	
	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		slotChanging(i);
		super.setInventorySlotContents(i, itemstack);
		slotChanged(i);
	}
	
	private void slotChanged(int i) {
		ItemStack itemstack = inv.contents[i];
		
		if(i >= START_RECIPE && i < START_RECIPE + SIZE_RECIPE && !worldObj.isRemote) {
			if(itemstack == null)
				recipeInputs[i - START_RECIPE] = new ItemStack[0];
			else
				recipeInputs[i - START_RECIPE] = new ItemStack[] {itemstack};
			removeAllMultiInputs();
		}
	}

	public int forceContainerUpdateCount;
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("craftMany", craftMany);
		nbttagcompound.setByte("facing", outputFace);
		
		NBTTagList rt = new NBTTagList();
		for(int k = 0; k < SIZE_RECIPE; k++) {
			NBTTagList l = new NBTTagList();
			for(ItemStack is : recipeInputs[k]) {
				NBTTagCompound itag = new NBTTagCompound();
				is.writeToNBT(itag);
				l.appendTag(itag);
			}
			rt.appendTag(l);
		}
		nbttagcompound.setTag("recipe", rt);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		craftMany = tag.getBoolean("craftMany");
		if(tag.hasKey("facing"))
			outputFace = tag.getByte("facing");
		else
			outputFace = Dir.PY;
		
		if(tag.hasKey("recipe")) {
			NBTTagList rt = tag.getTagList("recipe");
			for(int k = 0; k < SIZE_RECIPE; k++) {
				NBTTagList l = (NBTTagList)rt.tagAt(k);
				recipeInputs[k] = new ItemStack[l.tagCount()];
				for(int n = 0; n < l.tagCount(); n++) {
					recipeInputs[k][n] = ItemStack.loadItemStackFromNBT((NBTTagCompound)l.tagAt(n));
				}
			}
		} else {
			for(int k = 0; k < SIZE_RECIPE; k++) {
				if(inv.contents[START_RECIPE + k] == null)
					recipeInputs[k] = new ItemStack[0];
				else
					recipeInputs[k] = new ItemStack[] {inv.contents[START_RECIPE + k]};
			}
		}
	}
}
