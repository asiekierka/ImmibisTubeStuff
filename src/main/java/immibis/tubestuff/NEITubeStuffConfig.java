package immibis.tubestuff;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.api.IOverlayHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NEITubeStuffConfig implements IConfigureNEI {
	
	public static class ACT2OverlayHandler implements IOverlayHandler {

		@Override
		public void overlayRecipe(GuiContainer gui, List ingredients, boolean shift) {
			ContainerAutoCraftingMk2 c = (ContainerAutoCraftingMk2)gui.inventorySlots;
			PacketACT2RecipeUpdate p = new PacketACT2RecipeUpdate();
			for(PositionedStack ps : (List<PositionedStack>) ingredients) {
				int x = (ps.relx - 25) / 18;
				int y = (ps.rely - 6) / 18;
				int slot = x + y * 3;
				if(x < 0 || x > 2 || y < 0 || y > 2 || slot < 0 || slot > 8) {
					Minecraft.getMinecraft().thePlayer.sendChatMessage("TubeStuff NEI integration needs updating, this button is broken.");
					return;
				}
				
				p.stacks[slot] = ps.items;
			}
			c.sendActionPacket(p);
		}
		
	}

	@Override
	public void loadConfig() {
		API.registerGuiOverlayHandler(GuiAutoCraftingMk2.class, new ACT2OverlayHandler(), "crafting");
		API.registerGuiOverlayHandler(GuiAutoCraftingMk2.class, new ACT2OverlayHandler(), "crafting2x2");
	}

	@Override
	public String getName() {
		return TubeStuff.class.getAnnotation(Mod.class).name();
	}

	@Override
	public String getVersion() {
		return TubeStuff.class.getAnnotation(Mod.class).version();
	}
	
}
