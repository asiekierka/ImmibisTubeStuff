package immibis.tubestuff;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAutoCraftingMk2 extends GuiContainer {
	private ContainerAutoCraftingMk2 container;
	public GuiAutoCraftingMk2(EntityPlayer player, TileAutoCraftingMk2 table) {
		super(new ContainerAutoCraftingMk2(player, table));
		container = (ContainerAutoCraftingMk2)inventorySlots;
		
		xSize = 186;
		ySize = 249; 
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		int k = mc.renderEngine.getTexture("/immibis/tubestuff/crafting-gui.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        
        stackButton.displayString = container.tile.craftMany ? "64" : "1";
	}
	
	private GuiButton stackButton, oreDictButton, clearButton;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		clearButton = new GuiButton(2, guiLeft + 156, guiTop + 100, 20, 20, "C");
		stackButton = new GuiButton(0, guiLeft + 156, guiTop + 122, 20, 20, container.tile.craftMany ? "64" : "1");
		oreDictButton = new GuiButton(1, guiLeft + 156, guiTop + 144, 20, 20, "OD");
		
		controlList.add(stackButton);
		controlList.add(oreDictButton);
		controlList.add(clearButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		((ContainerAutoCraftingMk2)inventorySlots).sendButtonPressed(par1GuiButton.id);
	}
}
