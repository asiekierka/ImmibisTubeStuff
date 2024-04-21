package immibis.tubestuff;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
import static org.lwjgl.opengl.GL11.*;
import immibis.core.api.util.Dir;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// render pickaxe coming out the front, swinging when it breaks a block
@SideOnly(Side.CLIENT)
public class RenderTileBlockBreaker extends TileEntitySpecialRenderer {
	private RenderBlocks renderBlocks = new RenderBlocks();
	
	private static final boolean TOOL_STICKS_OUT = false;
	private static final boolean RENDER_PISTON = false;
	static final boolean HALF_HEIGHT = false;

	@Override
	public void renderTileEntityAt(TileEntity var1, double x, double y, double z, float partialTick) {
		TileBlockBreaker te = (TileBlockBreaker)var1;
		ItemStack tool = te.getTool();
		
		float swingTime = te.swingTime + (te.isBreaking || te.swingTime != 0 ? partialTick : 0);
		float angle = (float)(1 - Math.cos(swingTime / TileBlockBreaker.SWING_PERIOD * 2 * Math.PI)) * -45;
		
		//OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		//OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		
		int br = te.worldObj.getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, br & 0xFFFF, br >> 16);
		
		glPushMatrix();
		glTranslated(x+0.5, y+8/16.0, z+0.5);
		
		glDisable(GL_LIGHTING);
		
		switch(te.facing) {
		case Dir.PX: break;
		case Dir.NX: glRotatef(180, 0, 1, 0); break;
		case Dir.PZ: glRotatef(-90, 0, 1, 0); break;
		case Dir.NZ: glRotatef(90, 0, 1, 0); break;
		}
		
		// Render piston
		if(RENDER_PISTON)
		{
			double pistonExtensionDist = angle / -90;
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileEntityRenderer.renderEngine.getTexture("/terrain.png"));
			Tessellator t = Tessellator.instance;
			double u, v, du, dv;
			
			double _x = -0.5;
			double _y = -0.5;
			double _z = -0.25;
			double cx = 0.5, cy=0.5*0.75, cz=0.5;
			
			t.startDrawingQuads();
			t.setBrightness(te.worldObj.getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, 0));
			
			// base
			
			u = 208/256.0; v = 96/256.0; du = dv = 1/16.0;
			
			t.setNormal(0.0F, -1.0F, 0.0F);
	        t.addVertexWithUV(_x   , _y, _z   , u, v);
			t.addVertexWithUV(_x+cx, _y, _z   , u+du, v);
			t.addVertexWithUV(_x+cx, _y, _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x   , _y, _z+cz, u, v+dv);
			
			u = 192/256.0; v = 100/256.0; dv = 12/256.0; 
			
			t.setNormal(0.0F, 0.0F, -1.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z, u, v);
			t.addVertexWithUV(_x+cx, _y   , _z, u, v+dv);
			t.addVertexWithUV(_x   , _y   , _z, u+du, v+dv);
			t.addVertexWithUV(_x   , _y+cy, _z, u+du, v);
			
			t.setNormal(0.0F, 0.0F, 1.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u, v);
			t.addVertexWithUV(_x   , _y+cy, _z+cz, u+du, v);
			t.addVertexWithUV(_x   , _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z+cz, u, v+dv);
			
			t.setNormal(-1.0F, 0.0F, 0.0F);
	        t.addVertexWithUV(_x, _y+cy, _z   , u, v);
			t.addVertexWithUV(_x, _y   , _z   , u, v+dv);
			t.addVertexWithUV(_x, _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x, _y+cy, _z+cz, u+du, v);
			
			t.setNormal(1.0F, 0.0F, 0.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z   , u, v);
			t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u+du, v);
			t.addVertexWithUV(_x+cx, _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z   , u, v+dv);
			
			u = 224/256.0; v = 96/256.0; dv = 1/16.0;
			
			t.setNormal(0.0F, 1.0F, 0.0F);
	        t.addVertexWithUV(_x   , _y+cy, _z   , u, v);
			t.addVertexWithUV(_x   , _y+cy, _z+cz, u, v+dv);
			t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y+cy, _z   , u+du, v);
			
			double baseTop = y + cy;
			
			// head
			cy /= 3;
			//_y = (1 - 4*cy) * pistonExtensionDist - 0.5 + 3*cy;
			_y = Math.sin(-angle*Math.PI/180)*6/16 - cy;
			double headBottom = _y;
			
			u = 176/256.0; v = 96/256.0; du = dv = 1/16.0;
			
			t.setNormal(0.0F, -1.0F, 0.0F);
	        t.addVertexWithUV(_x   , _y, _z   , u, v);
			t.addVertexWithUV(_x+cx, _y, _z   , u+du, v);
			t.addVertexWithUV(_x+cx, _y, _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x   , _y, _z+cz, u, v+dv);
			
			u = 192/256.0; v = 96/256.0; dv = 4/256.0; 
			
			t.setNormal(0.0F, 0.0F, -1.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z, u, v);
			t.addVertexWithUV(_x+cx, _y   , _z, u, v+dv);
			t.addVertexWithUV(_x   , _y   , _z, u+du, v+dv);
			t.addVertexWithUV(_x   , _y+cy, _z, u+du, v);
			
			t.setNormal(0.0F, 0.0F, 1.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u, v);
			t.addVertexWithUV(_x   , _y+cy, _z+cz, u+du, v);
			t.addVertexWithUV(_x   , _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z+cz, u, v+dv);
			
			t.setNormal(-1.0F, 0.0F, 0.0F);
	        t.addVertexWithUV(_x, _y+cy, _z   , u, v);
			t.addVertexWithUV(_x, _y   , _z   , u, v+dv);
			t.addVertexWithUV(_x, _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x, _y+cy, _z+cz, u+du, v);
			
			t.setNormal(1.0F, 0.0F, 0.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z   , u, v);
			t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u+du, v);
			t.addVertexWithUV(_x+cx, _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z   , u, v+dv);
			
			u = 176/256.0; v = 96/256.0; dv = 1/16.0;
			
			t.setNormal(0.0F, 1.0F, 0.0F);
	        t.addVertexWithUV(_x   , _y+cy, _z   , u, v);
			t.addVertexWithUV(_x   , _y+cy, _z+cz, u, v+dv);
			t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y+cy, _z   , u+du, v);
			
			// arm
			cx /= 4;
			cz /= 4;
			cy *= 4;
			_x += cx*1.5;
			_z += cz*1.5;
			_y = headBottom - cy;
			
			//u = 208/256.0; v = 96/256.0; du = dv = 1/16.0;
			
			//t.setNormal(0.0F, -1.0F, 0.0F);
	        //t.addVertexWithUV(_x   , _y, _z   , u, v);
			//t.addVertexWithUV(_x+cx, _y, _z   , u+du, v);
			//t.addVertexWithUV(_x+cx, _y, _z+cz, u+du, v+dv);
			//t.addVertexWithUV(_x   , _y, _z+cz, u, v+dv);
			
			u = 192/256.0; v = 96/256.0; dv = 4/256.0; 
			
			t.setNormal(0.0F, 0.0F, -1.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z, u, v);
			t.addVertexWithUV(_x+cx, _y   , _z, u+du, v);
			t.addVertexWithUV(_x   , _y   , _z, u+du, v+dv);
			t.addVertexWithUV(_x   , _y+cy, _z, u, v+dv);
			
			t.setNormal(0.0F, 0.0F, 1.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u, v);
			t.addVertexWithUV(_x   , _y+cy, _z+cz, u, v+dv);
			t.addVertexWithUV(_x   , _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z+cz, u+du, v);
			
			t.setNormal(-1.0F, 0.0F, 0.0F);
	        t.addVertexWithUV(_x, _y+cy, _z   , u, v);
			t.addVertexWithUV(_x, _y   , _z   , u+du, v);
			t.addVertexWithUV(_x, _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x, _y+cy, _z+cz, u, v+dv);
			
			t.setNormal(1.0F, 0.0F, 0.0F);
	        t.addVertexWithUV(_x+cx, _y+cy, _z   , u, v);
			t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z+cz, u+du, v+dv);
			t.addVertexWithUV(_x+cx, _y   , _z   , u+du, v);
			
			//u = 224/256.0; v = 96/256.0; dv = 1/16.0;
			
			//t.setNormal(0.0F, 1.0F, 0.0F);
	        //t.addVertexWithUV(_x   , _y+cy, _z   , u, v);
			//t.addVertexWithUV(_x   , _y+cy, _z+cz, u, v+dv);
			//t.addVertexWithUV(_x+cx, _y+cy, _z+cz, u+du, v+dv);
			//t.addVertexWithUV(_x+cx, _y+cy, _z   , u+du, v);
			
			t.draw();
		}
		
		
		glRotatef(angle, 0, 0, 1);
		
		glColor3f(1, 1, 1);
		
		// Render tool
		if(tool != null)
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileEntityRenderer.renderEngine.getTexture(tool.getItem().getTextureFile()));
			
			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(tool, EQUIPPED);
			
			glPushMatrix();
			
			if(TOOL_STICKS_OUT)
				glTranslatef(0.25f, 0.25f, 0);
			if(customRenderer != null) {
				glScalef(0.5f, 0.5f, 0.5f);
				
				// TODO player will be null in SMP/joined, do we need an actual player?
				ForgeHooksClient.renderEquippedItem(customRenderer, renderBlocks, te.player, tool);
				
			} else if(tool.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.blocksList[tool.itemID].getRenderType())) {
				glScalef(0.5f, 0.5f, 0.5f);
				
				renderBlocks.renderBlockAsItem(Block.blocksList[tool.itemID], tool.getItemDamage(), 1.0F);
				
			} else {
				glScalef(-1, 1, 1);
				glTranslatef(-0.5f, -0.5f, 0);
				Tessellator var5 = Tessellator.instance;
		        int var6 = tool.getItem().getIconIndex(tool);
		        float var7 = ((float)(var6 % 16 * 16) + 0.0F) / 256.0F;
		        float var8 = ((float)(var6 % 16 * 16) + 15.99F) / 256.0F;
		        float var9 = ((float)(var6 / 16 * 16) + 0.0F) / 256.0F;
		        float var10 = ((float)(var6 / 16 * 16) + 15.99F) / 256.0F;
		        var5.startDrawingQuads();
		        var5.setBrightness(te.worldObj.getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, 0));
		        this.renderItemIn2D(var5, var8, var9, var7, var10);
		        var5.draw();
		    }
		    glPopMatrix();
		}
		
		// Render axle
		{
			double th = 1/16.0;
			Tessellator t = Tessellator.instance;
			glDisable(GL_TEXTURE_2D);
			t.startDrawing(GL_QUAD_STRIP);
			t.setBrightness(te.worldObj.getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, 0));
			t.setColorOpaque(0, 50, 50);
			t.addVertex(-th,  0,-0.5);
			t.addVertex(-th,  0, 0.5);
			t.addVertex(  0, th,-0.5);
			t.addVertex(  0, th, 0.5);
			t.addVertex( th,  0,-0.5);
			t.addVertex( th,  0, 0.5);
			t.addVertex(  0,-th,-0.5);
			t.addVertex(  0,-th, 0.5);
			t.addVertex(-th,  0,-0.5);
			t.addVertex(-th,  0, 0.5);
			t.draw();
			glEnable(GL_TEXTURE_2D);
		}
		
		// Render piston "handle"
		if(RENDER_PISTON)
		{
			Tessellator t = Tessellator.instance;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileEntityRenderer.renderEngine.getTexture("/immibis/tubestuff/blocks.png"));
			t.startDrawing(GL_QUADS);
			t.setBrightness(te.worldObj.getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, 0));
			t.addVertexWithUV( 0  , 0.01, -0.5, 224/256.0, 8/256.0);
			t.addVertexWithUV( 0  , 0.01,  0.5, 240/256.0, 8/256.0);
			t.addVertexWithUV(-0.5, 0.01,  0.5, 240/256.0, 16/256.0);
			t.addVertexWithUV(-0.5, 0.01, -0.5, 224/256.0, 16/256.0);
			
			t.addVertexWithUV( 0  , 0.01, -0.5, 224/256.0, 8/256.0);
			t.addVertexWithUV(-0.5, 0.01, -0.5, 224/256.0, 16/256.0);
			t.addVertexWithUV(-0.5, 0.01,  0.5, 240/256.0, 16/256.0);
			t.addVertexWithUV( 0  , 0.01,  0.5, 240/256.0, 8/256.0);
			t.draw();
		}
        
		glPopMatrix();
		
		glEnable(GL_LIGHTING);
		
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

    private void renderItemIn2D(Tessellator par1Tessellator, float par2, float par3, float par4, float par5)
    {
        float var6 = 1.0F;
        float var7 = 0.0625F / 2;
        
        par1Tessellator.setNormal(0.0F, 0.0F, 1.0F);
        par1Tessellator.addVertexWithUV(0.0D, 1.0D, var7, par2, par3);
        par1Tessellator.addVertexWithUV(var6, 1.0D, var7, par4, par3);
        par1Tessellator.addVertexWithUV(var6, 0.0D, var7, par4, par5);
        par1Tessellator.addVertexWithUV(0.0D, 0.0D, var7, par2, par5);
        
        par1Tessellator.setNormal(0.0F, 0.0F, -1.0F);
        par1Tessellator.addVertexWithUV(0.0D, 0.0D, -var7, par2, par5);
        par1Tessellator.addVertexWithUV(var6, 0.0D, -var7, par4, par5);
        par1Tessellator.addVertexWithUV(var6, 1.0D, -var7, par4, par3);
        par1Tessellator.addVertexWithUV(0.0D, 1.0D, -var7, par2, par3);
        
        par1Tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        int var8;
        float var9;
        float var10;
        float var11;

        int tileSize = cpw.mods.fml.client.TextureFXManager.instance().getTextureDimensions(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)).width / 16;
        
        float tx = 1.0f / (32 * tileSize);
        float tz = 1.0f /  tileSize;

        for (var8 = 0; var8 < tileSize; ++var8)
        {
            var9 = (float)var8 / tileSize;
            var10 = par2 + (par4 - par2) * var9 - tx;
            var11 = var6 * var9;
            par1Tessellator.addVertexWithUV((double)var11, 0.0D, -var7, (double)var10, (double)par5);
            par1Tessellator.addVertexWithUV((double)var11, 1.0D, -var7, (double)var10, (double)par3);
            par1Tessellator.addVertexWithUV((double)var11, 1.0D,  var7, (double)var10, (double)par3);
            par1Tessellator.addVertexWithUV((double)var11, 0.0D,  var7, (double)var10, (double)par5);
            
        }

        par1Tessellator.setNormal(1.0F, 0.0F, 0.0F);

        for (var8 = 0; var8 < tileSize; ++var8)
        {
            var9 = (float)var8 / tileSize;
            var10 = par2 + (par4 - par2) * var9 - tx;
            var11 = var6 * var9 + tz;
            par1Tessellator.addVertexWithUV((double)var11, 1.0D,  var7, (double)var10, (double)par3);
            par1Tessellator.addVertexWithUV((double)var11, 1.0D, -var7, (double)var10, (double)par3);
            par1Tessellator.addVertexWithUV((double)var11, 0.0D, -var7, (double)var10, (double)par5);
            par1Tessellator.addVertexWithUV((double)var11, 0.0D,  var7, (double)var10, (double)par5);
            
        }

        par1Tessellator.setNormal(0.0F, 1.0F, 0.0F);

        for (var8 = 0; var8 < tileSize; ++var8)
        {
            var9 = (float)var8 / tileSize;
            var10 = par5 + (par3 - par5) * var9 - tx;
            var11 = var6 * var9 + tz;
            par1Tessellator.addVertexWithUV(0.0D, var11,  var7, (double)par2, (double)var10);
            par1Tessellator.addVertexWithUV(0.0D, var11, -var7, (double)par2, (double)var10);
            par1Tessellator.addVertexWithUV(var6, var11, -var7, (double)par4, (double)var10);
            par1Tessellator.addVertexWithUV(var6, var11,  var7, (double)par4, (double)var10);
            
        }

        par1Tessellator.setNormal(0.0F, -1.0F, 0.0F);

        for (var8 = 0; var8 < tileSize; ++var8)
        {
            var9 = (float)var8 / tileSize;
            var10 = par5 + (par3 - par5) * var9 - tx;
            var11 = var6 * var9;
            par1Tessellator.addVertexWithUV(var6, var11,  var7, (double)par4, (double)var10);
            par1Tessellator.addVertexWithUV(var6, var11, -var7, (double)par4, (double)var10);
            par1Tessellator.addVertexWithUV(0.0D, var11, -var7, (double)par2, (double)var10);
            par1Tessellator.addVertexWithUV(0.0D, var11,  var7, (double)par2, (double)var10);
            
        }
    }
}