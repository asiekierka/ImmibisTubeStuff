package immibis.tubestuff;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockStorage extends Block {
	
	public static enum Meta {
		SILVER, TIN, COPPER, NIKOLITE, COAL, REDSTONE, BLUE_ALLOY, RED_ALLOY, BRASS
	}

	public BlockStorage(int id) {
		super(id, Material.iron);
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundMetalFootstep);
		setTextureFile("/immibis/tubestuff/blocks.png");
		
		setCreativeTab(CreativeTabs.tabMisc);
	}
	
	@Override
	public void addCreativeItems(ArrayList l) {
		for(int k = 0; k < 10; k++)
			l.add(new ItemStack(this, 1, k));
	}
	
    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
    	if(canFall(par1World.getBlockMetadata(par2, par3, par4)))
    		par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
    }

    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
    	if(canFall(par1World.getBlockMetadata(par2, par3, par4)))
        	par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
    }

    private boolean canFall(int meta) {
		return meta == 3 || meta == 5; // nikolite or redstone
	}

	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
		if(canFall(par1World.getBlockMetadata(par2, par3, par4)))
		   tryToFall(par1World, par2, par3, par4);
    }

    private void tryToFall(World par1World, int par2, int par3, int par4) {
        if (BlockSand.canFallBelow(par1World, par2, par3 - 1, par4) && par3 >= 0)
        {
        	int meta = par1World.getBlockMetadata(par2, par3, par4);
        	
            if (TubeStuff.enableSlowFalling && !BlockSand.fallInstantly && par1World.checkChunksExist(par2 - 32, par3 - 32, par4 - 32, par2 + 32, par3 + 32, par4 + 32))
            {
                if (!par1World.isRemote)
                {
                    EntityFallingSand var9 = new EntityFallingSand(par1World, (double)((float)par2 + 0.5F), (double)((float)par3 + 0.5F), (double)((float)par4 + 0.5F), this.blockID, par1World.getBlockMetadata(par2, par3, par4));
                    par1World.spawnEntityInWorld(var9);
                }
            } else {
	
            	par1World.setBlockWithNotify(par2, par3, par4, 0);
                
                while (BlockSand.canFallBelow(par1World, par2, par3 - 1, par4) && par3 > 0)
	                --par3;
	
	            if (par3 > 0)
	                par1World.setBlockAndMetadataWithNotify(par2, par3, par4, this.blockID, meta);
            }
        }
    }

    @Override
    public int tickRate() {
        return 3;
    }
	
	@Override
	public int damageDropped(int meta) {
		return meta;
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int data) {
		return 32 + data;
	}
	
	@Override
	public final void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		ArrayList al = new ArrayList();
		addCreativeItems(al);
		par3List.addAll(al);
	}

}
