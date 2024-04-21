package immibis.tubestuff;

import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;

public class AutoCraftingMk2CraftingRecipeProvider implements ICraftingRecipeProvider {
    @Override
    public boolean canOpenGui(TileEntity tileEntity) {
        return tileEntity instanceof TileAutoCraftingMk2;
    }

    @Override
    public boolean importRecipe(TileEntity tileEntity, SimpleInventory inventory) {
        if (!(tileEntity instanceof TileAutoCraftingMk2))
            return false;

        TileAutoCraftingMk2 crafterMk2 = (TileAutoCraftingMk2) tileEntity;
        ItemStack[][] inputMatrix = crafterMk2.recipeInputs;
        IRecipe recipe = crafterMk2.cachedRecipe;
        if (inputMatrix == null || recipe == null)
            return false;
        ItemStack result = crafterMk2.cachedRecipe.getRecipeOutput();
        if (result == null)
            return false;

        inventory.setInventorySlotContents(9, result.copy());

        for (int i=0; i < inputMatrix.length; i++) {
            ItemStack stackInSlot = null;
            // Table can have multiple ItemStacks for the same slot if oredictionary mode is enabled
            ItemStack[] possibleInputs = inputMatrix[i];
            if (possibleInputs.length > 0) {
                stackInSlot = possibleInputs[0].copy();
                stackInSlot.stackSize = 1;
            }
            inventory.setInventorySlotContents(i, stackInSlot);
        }

        this.mergeStacks(inventory);

        return true;
    }

    private void mergeStacks(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory() - 1; ++i) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot != null) {
                ItemIdentifier itemInSlot = ItemIdentifier.get(stackInSlot);

                for (int j = i + 1; j < inventory.getSizeInventory() - 1; ++j) {
                    ItemStack stackInOtherSlot = inventory.getStackInSlot(j);
                    if (stackInOtherSlot != null && itemInSlot == ItemIdentifier.get(stackInOtherSlot)) {
                        stackInSlot.stackSize += stackInOtherSlot.stackSize;
                        inventory.setInventorySlotContents(j, null);
                    }
                }
            }
        }

        for (int i = 0; i < inventory.getSizeInventory() - 1; ++i) {
            if (inventory.getStackInSlot(i) == null) {
                for (int j = i + 1; j < inventory.getSizeInventory() - 1; ++j) {
                    if (inventory.getStackInSlot(j) != null) {
                        inventory.setInventorySlotContents(i, inventory.getStackInSlot(j));
                        inventory.setInventorySlotContents(j, null);
                        break;
                    }
                }
            }
        }
    }
}
