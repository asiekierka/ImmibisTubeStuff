package immibis.tubestuff;

import static immibis.tubestuff.RedPowerItems.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLogicCrafter extends GuiContainer {
	private IInventory chest;
	private ContainerLogicCrafter container;
	public GuiLogicCrafter(EntityPlayer player, TileLogicCrafter chest) {
		super(new ContainerLogicCrafter(player, chest));
		this.container = (ContainerLogicCrafter)inventorySlots;
		this.chest = chest;
		xSize = 186;
		ySize = 249; 
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		int k = mc.renderEngine.getTexture("/immibis/tubestuff/rplc-gui.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = guiLeft = (width - xSize) / 2;
        int i1 = guiTop = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)guiLeft, (float)guiTop, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        Slot var6 = null;
        short var7 = 240;
        short var8 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var7 / 1.0F, (float)var8 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        if(logicPartItem != null) {
        	itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneWaferIS, 10, 10);
        	itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneWireIS, 10, 29);
        	itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneAnodeIS, 10, 48);
        	itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneCathodeIS, 10, 67);
        	itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stonePointerIS, 10, 86);
        	itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, siliconChipIS, 10, 105);
        	//itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneRedwireIS, 10, 124);
        	//itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, plateAssemblyIS, 10, 143);
        	//itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, taintedChipIS, 10, 162);
        	//itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneBundleIS, 10, 181);
        	//itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, stoneWaferIS, 10, 200);
        }
        
        GL11.glPopMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}
}
