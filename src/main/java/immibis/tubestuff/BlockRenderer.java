package immibis.tubestuff;

import immibis.core.RenderUtils;
import immibis.core.api.porting.PortableBlockRenderer;
import immibis.core.api.util.Dir;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockRenderer implements PortableBlockRenderer {
	@Override
	public void renderInvBlock(RenderBlocks rb, Block block, int meta, int model) {
		if(meta == 6) {
			// block breaker
			Tessellator.instance.startDrawingQuads();
			renderBlockBreaker(rb.overrideBlockTexture, Tessellator.instance, -0.5, -0.5, -0.5, Dir.NX);
			Tessellator.instance.draw();
			
		} else {
			BlockTubestuff.model = 0;
			rb.renderBlockAsItem(block, meta, 1.0f);
			BlockTubestuff.model = model;
		}
	}
	
	@Override
	public boolean renderWorldBlock(RenderBlocks rb, IBlockAccess w, int x, int y, int z, Block block, int model) {
		int meta = w.getBlockMetadata(x, y, z);
		if(meta == 6) {
			// block breaker
			TileEntity te = w.getBlockTileEntity(x, y, z);
			int facing = Dir.PX;
			if(te instanceof TileBlockBreaker)
				facing = ((TileBlockBreaker)te).facing;
			
			RenderUtils.setBrightness(w, x, y, z);
			renderBlockBreaker(rb.overrideBlockTexture, Tessellator.instance, x, y, z, facing);
			
		} else if(meta != 2 || !SharedProxy.enableBHCAnim()) {
			BlockTubestuff.model = 0;
			rb.renderStandardBlock(block, x, y, z);
			BlockTubestuff.model = model;
		}
		return true;
	}
	
	private void renderBlockBreaker(int overrideTexture, Tessellator t, double x, double y, double z, int facing) {
		double u = overrideTexture < 0 ? 0 : (overrideTexture % 16) / 16.0;
		double v = overrideTexture < 0 ? 0 : (overrideTexture / 16) / 16.0;
		double du = 1/16.0, dv=1/16.0;
		
		final double h = RenderTileBlockBreaker.HALF_HEIGHT ? 0.5 : 1.0;
		
		final double e = 0.01; // small offset added to inside faces to prevent z-fighting
		
		if(overrideTexture < 0) u = 12/16.0;
		t.setColorOpaque(255, 255, 255);
		t.setNormal(0.0F, -1.0F, 0.0F);
        t.addVertexWithUV(x   , y, z   , u, v);
		t.addVertexWithUV(x+1 , y, z   , u+du, v);
		t.addVertexWithUV(x+1 , y, z+1 , u+du, v+dv);
		t.addVertexWithUV(x   , y, z+1 , u, v+dv);
		t.setNormal(0.0F, 1.0F, 0.0F);
		t.addVertexWithUV(x   , y+e, z   , u, v);
		t.addVertexWithUV(x   , y+e, z+1 , u, v+dv);
		t.addVertexWithUV(x+1 , y+e, z+1 , u+du, v+dv);
		t.addVertexWithUV(x+1 , y+e, z   , u+du, v);
		
		v += (1-h)/16.0;
		dv = h/16.0;
		
		if(overrideTexture < 0) u = (facing == Dir.PX || facing == Dir.NX ? 13 : 12)/16.0;
		t.setNormal(0.0F, 0.0F, -1.0F);
        t.addVertexWithUV(x+1 , y+h , z, u, v);
		t.addVertexWithUV(x+1 , y   , z, u, v+dv);
		t.addVertexWithUV(x   , y   , z, u+du, v+dv);
		t.addVertexWithUV(x   , y+h , z, u+du, v);
		t.setNormal(0.0F, 0.0F, 1.0F);
		t.addVertexWithUV(x+1 , y+h , z+e, u, v);
		t.addVertexWithUV(x   , y+h , z+e, u+du, v);
		t.addVertexWithUV(x   , y   , z+e, u+du, v+dv);
		t.addVertexWithUV(x+1 , y   , z+e, u, v+dv);
		
		t.setNormal(0.0F, 0.0F, 1.0F);
        t.addVertexWithUV(x+1 , y+h , z+1 , u, v);
		t.addVertexWithUV(x   , y+h , z+1 , u+du, v);
		t.addVertexWithUV(x   , y   , z+1 , u+du, v+dv);
		t.addVertexWithUV(x+1 , y   , z+1 , u, v+dv);
		t.setNormal(0.0F, 0.0F, -1.0F);
		t.addVertexWithUV(x+1 , y+h , z+1-e , u, v);
		t.addVertexWithUV(x+1 , y   , z+1-e , u, v+dv);
		t.addVertexWithUV(x   , y   , z+1-e , u+du, v+dv);
		t.addVertexWithUV(x   , y+h , z+1-e , u+du, v);
		
		if(overrideTexture < 0) u = (facing == Dir.PZ || facing == Dir.NZ ? 13 : 12)/16.0;
		t.setNormal(-1.0F, 0.0F, 0.0F);
        t.addVertexWithUV(x, y+h , z   , u, v);
		t.addVertexWithUV(x, y   , z   , u, v+dv);
		t.addVertexWithUV(x, y   , z+1 , u+du, v+dv);
		t.addVertexWithUV(x, y+h , z+1 , u+du, v);
		t.setNormal(1.0F, 0.0F, 0.0F);
		t.addVertexWithUV(x+e, y+h , z   , u, v);
		t.addVertexWithUV(x+e, y+h , z+1 , u+du, v);
		t.addVertexWithUV(x+e, y   , z+1 , u+du, v+dv);
		t.addVertexWithUV(x+e, y   , z   , u, v+dv);
		
		t.setNormal(1.0F, 0.0F, 0.0F);
        t.addVertexWithUV(x+1 , y+h , z   , u, v);
		t.addVertexWithUV(x+1 , y+h , z+1 , u+du, v);
		t.addVertexWithUV(x+1 , y   , z+1 , u+du, v+dv);
		t.addVertexWithUV(x+1 , y   , z   , u, v+dv);
		t.setNormal(-1.0F, 0.0F, 0.0F);
		t.addVertexWithUV(x+1-e , y+h , z   , u, v);
		t.addVertexWithUV(x+1-e , y   , z   , u, v+dv);
		t.addVertexWithUV(x+1-e , y   , z+1 , u+du, v+dv);
		t.addVertexWithUV(x+1-e , y+h , z+1 , u+du, v);
		
		dv = 1/16.0;
		v -= (1-h)/16.0;
		
		if(overrideTexture < 0) u = 12/16.0;
		t.setNormal(0.0F, 1.0F, 0.0F);
        t.addVertexWithUV(x   , y+h , z   , u, v);
		t.addVertexWithUV(x   , y+h , z+1 , u, v+dv);
		t.addVertexWithUV(x+1 , y+h , z+1 , u+du, v+dv);
		t.addVertexWithUV(x+1 , y+h , z   , u+du, v);
		t.setNormal(0.0F, -1.0F, 0.0F);
		t.addVertexWithUV(x   , y+h-e , z   , u, v);
		t.addVertexWithUV(x+1 , y+h-e , z   , u+du, v);
		t.addVertexWithUV(x+1 , y+h-e , z+1 , u+du, v+dv);
		t.addVertexWithUV(x   , y+h-e , z+1 , u, v+dv);
	}
}