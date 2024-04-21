package immibis.tubestuff;

import cpw.mods.fml.client.FMLClientHandler;
import immibis.core.BlockCombined;
import immibis.core.api.util.Dir;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTubestuff extends BlockCombined {
	public BlockTubestuff(int i) {
		super(i, Material.iron, "/immibis/tubestuff/blocks.png");
		setHardness(2.0F);
		
		//if(NonSharedProxy.CLIENT)
			setTickRandomly(true);
	}
	
	public static final int META_BUFFER = 0;
	public static final int META_ACT2 = 1;
	public static final int META_BHC = 2;
	public static final int META_INCINERATOR = 3;
	public static final int META_DUPLICATOR = 4;
	public static final int META_RETRIEVULATOR = 5;
	public static final int META_BLOCK_BREAKER = 6;
	public static final int META_LIQUID_INCINERATOR = 7;
	public static final int META_LIQUID_DUPLICATOR = 8;
	public static final int META_ONLINE_DETECTOR = 9;
	
	public static int model;
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, net.minecraftforge.common.ForgeDirection side) {
		int meta = world.getBlockMetadata(x, y, z);
		if(meta == 6) return false; // block breaker
		return true;
	}
	
	@Override
	public int getRenderType() {
		return model;
	}
	
	@Override @SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
	    int meta = world.getBlockMetadata(x, y, z);
	    
	    if(meta == 2)
	    {
	    	if(SharedProxy.enableBHCParticles)
	    	{
		    	for(int k = 0; k < 1 + world.rand.nextInt(3); k++)
		    	{
		    		EntityFX fx = new EntityBlackHoleFX(world, x + 0.5, y + 0.5, z + 0.5); 
		    		FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
		    	}
	    	}
	    }
	}
	
	@Override
	public TileEntity getBlockEntity(int data)
	{
		switch(data) {
		case META_BUFFER: return new TileBuffer();
		case META_ACT2: return new TileAutoCraftingMk2();
		case META_BHC: return new TileBlackHoleChest();
		// case 3: return new TileLogicCrafter();
		case META_INCINERATOR: return new TileIncinerator();
		case META_DUPLICATOR: return new TileDuplicator();
		case META_RETRIEVULATOR: return new TileRetrievulator();
		case META_BLOCK_BREAKER: return new TileBlockBreaker();
		case META_LIQUID_INCINERATOR: return new TileLiquidIncinerator();
		case META_LIQUID_DUPLICATOR: return new TileLiquidDuplicator();
		case META_ONLINE_DETECTOR: return new TileOnlineDetector();
		default: return null;
		}
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int data)
	{
		if(data == 0) {
			// buffer
			return 3;
		} else if(data == 1) {
			// auto crafting table mk2
			if(side == 0)
				return 1;
			else if(side == 1)
				return 0;
			else
				return 2;
		} else if(data == 2) {
			// black hole chest
			return 4;
		/*} else if(data == 3) {
			// logic crafter
			return 5;*/
		} else if(data == 3) {
			// incinerator
			return (side == Dir.PY ? 7 : side == Dir.NY ? 1 : 6);
		} else if(data == 4) {
			// duplicator
			return 8;
		} else if(data == 5) {
			// retrievulator
			return (side == Dir.PY ? 9 : 10);
		} else if(data == 6) {
			// block breaker; this is only used for particles
			return 12;
		} else if(data == 7) {
			// liquid incinerator
			return (side == Dir.PY ? 7 : side == Dir.NY ? 1 : 6);
		} else if(data == 8) {
			// liquid duplicator
			return 8;
		} else if(data == META_ONLINE_DETECTOR) {
			return 14;
		}
		return 0; // unknown
	}
	
	private static int[][] actTextureMap = new int[][] {
		{ 0, 1,16,16,16,16}, // output -Y
		{ 1, 0, 2, 2, 2, 2}, // output +Y
		{ 2, 2, 0, 1,17,18}, // output -Z
		{16,16, 1, 0,18,17}, // output +Z
		{17,17,18,17, 0, 1}, // output -X
		{18,18,17,18, 1, 0}, // output +X
	};
	
	@Override
	public int getBlockTexture(IBlockAccess w, int x, int y, int z, int side) {
		int meta = w.getBlockMetadata(x, y, z);
		if(meta == 1) {
			// ACT2
			TileAutoCraftingMk2 te = (TileAutoCraftingMk2)w.getBlockTileEntity(x, y, z);
			return actTextureMap[te.outputFace][side];
			
		} else if(meta == 5) {
			// retrievulator
			TileRetrievulator te = (TileRetrievulator)w.getBlockTileEntity(x, y, z);
			if(side == te.outputFace)
				return 11;
			else if(side == (te.outputFace ^ 1))
				return 9;
			else
				return 10;
			
		} else if(meta == META_ONLINE_DETECTOR) {
			TileOnlineDetector tod = (TileOnlineDetector)w.getBlockTileEntity(x, y, z);
			return tod.redstone_output ? 15 : 14;
			
		} else {
			return getBlockTextureFromSideAndMetadata(side, meta);
		}
	}
	
	@Override
    public void addCreativeItems(ArrayList arraylist)
    {
		arraylist.add(new ItemStack(this, 1, 0));
		arraylist.add(new ItemStack(this, 1, 1));
		arraylist.add(new ItemStack(this, 1, 2));
		arraylist.add(new ItemStack(this, 1, 3));
		arraylist.add(new ItemStack(this, 1, 4));
		arraylist.add(new ItemStack(this, 1, 5));
		arraylist.add(new ItemStack(this, 1, 6));
		arraylist.add(new ItemStack(this, 1, 7));
		arraylist.add(new ItemStack(this, 1, 8));
		arraylist.add(new ItemStack(this, 1, 9));
    }
}
