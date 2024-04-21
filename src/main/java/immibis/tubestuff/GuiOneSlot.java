package immibis.tubestuff;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOneSlot extends GuiContainer {
	
	private String name;

	public GuiOneSlot(Container par1Container, String name) {
		super(par1Container);
		xSize = 190;
		ySize = 147;
		this.name = name;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		int k = mc.renderEngine.getTexture("/immibis/tubestuff/one-slot-gui.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(k);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
        
        int name_w = fontRenderer.getStringWidth(name);
        fontRenderer.drawString(name, (xSize - name_w) / 2, 8, 0x404040);
	}

}
