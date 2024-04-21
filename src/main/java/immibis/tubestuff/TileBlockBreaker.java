package immibis.tubestuff;

import immibis.core.TileCombined;
import immibis.core.api.net.IPacket;
import immibis.core.api.util.Dir;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class TileBlockBreaker extends TileCombined implements IInventory {

	public ItemStack tool;
	public byte facing;
	
	public EntityPlayerFakeTS player;
	
	@Override
	public List<ItemStack> getInventoryDrops() {
		if(tool == null)
			return Collections.emptyList();
		else
			return Collections.singletonList(tool);
	}
	
	@Override
	public IPacket getDescriptionPacket2() {
		return new PacketBlockBreakerDescription(xCoord, yCoord, zCoord, tool, facing, isBreaking || forceAnimation);
	}
	
	// for compatibility with older versions of Immibis Core which did not set isChunkDataPacket (before 50.2.5)
	// remove when that version is far enough behind
	@Override
	public Packet getDescriptionPacket() {
		Packet p = super.getDescriptionPacket();
		p.isChunkDataPacket = true;
		return p;
	}

	public ItemStack getTool() {
		return tool;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return var1 == 0 ? tool : null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		if(var1 == 0 && var2 >= 1 && tool != null) {
			ItemStack rv = tool;
			tool = null;
			resendDescriptionPacket();
			return rv;
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		if(var1 == 0) {
			tool = var2;
			if(player != null)
				player.inventory.setInventorySlotContents(0, var2);
			resendDescriptionPacket();
		}
	}

	@Override
	public String getInvName() {
		return "Block breaker";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return false;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		ItemStack h = player.inventory.getCurrentItem();
		if(h == null && tool == null) {
			return false;
		} else if(h != null && h.stackSize > 1) {
			if(tool != null)
				return false;
			setInventorySlotContents(0, player.inventory.decrStackSize(player.inventory.currentItem, 1));
		} else {
			// swap held item and tool
			player.inventory.setInventorySlotContents(player.inventory.currentItem, tool);
			setInventorySlotContents(0, h);
		}
		return true;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		if(tool != null) {
			NBTTagCompound tooltag = new NBTTagCompound();
			tool.writeToNBT(tooltag);
			tag.setCompoundTag("tool", tooltag);
		}
		tag.setByte("facing", facing);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("tool"))
			tool = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("tool"));
		else
			tool = null;
		facing = tag.getByte("facing");
	}
	
	public int swingTime = 0; // used for rendering only
	public static final int SWING_PERIOD = 10; // ditto
	
	public boolean isBreaking = false;
	private int breakingX, breakingY, breakingZ;
	private boolean forceAnimation = false; // if true, description packets will have isBreaking=true even if not breaking
	
	private int initialDamage, curblockDamage = 0;
	private int durabilityRemainingOnBlock;
	
	private static final int COOLDOWN_MAX = 5;
	private int cooldown = 0;
	
	@Override
	public void updateEntity() {
		
		if(isBreaking || (swingTime % SWING_PERIOD) != 0)
			swingTime++;
		else
			swingTime = 0;
		
		if(worldObj.isRemote)
			return;
		
		if(forceAnimation) {
			forceAnimation = false;
			resendDescriptionPacket();
		}
		
		int x = xCoord, y = yCoord, z = zCoord;
		switch(facing) {
		case Dir.PX: x++; break;
		case Dir.PY: y++; break;
		case Dir.PZ: z++; break;
		case Dir.NX: x--; break;
		case Dir.NY: y--; break;
		case Dir.NZ: z--; break;
		}
		
		if(cooldown > 0)
			cooldown--;
		
		if(tool == null) {
			if(isBreaking)
				stopBreaking();
			return;
		}
		
		if(!isBreaking) {
			if(cooldown > 0)
				return;
			
			Block block = Block.blocksList[worldObj.getBlockId(x, y, z)];
			int meta = worldObj.getBlockMetadata(x, y, z);
			
			if(block == null || block.isAirBlock(worldObj, x, y, z))
				return;
			
			if(player == null)
				player = new EntityPlayerFakeTS(worldObj);
			
			updateFakePlayer();
			
			isBreaking = true;
			resendDescriptionPacket();
			breakingX = x;
			breakingY = y;
			breakingZ = z;
			cooldown = COOLDOWN_MAX;
			
			startBreaking(x, y, z, block, meta);
			
		} else {
			updateFakePlayer();
			if(breakingX != x || breakingY != y || breakingZ != z) {
				stopBreaking();
				return;
			}
			continueBreaking();
		}
	}
	
	private void updateFakePlayer() {
		player.inventory.currentItem = 0;
		player.inventory.mainInventory[0] = tool;
		player.onGround = true;
	}
	
	private void stopBreaking() {
		isBreaking = false;
		worldObj.destroyBlockInWorldPartially(player.entityId, breakingX, breakingY, breakingZ, -1);
		setInventorySlotContents(0, player.inventory.mainInventory[0]);
	}
	
	private void startBreaking(int x, int y, int z, Block block, int meta) {
		int side = Dir.PY; // TODO
		
		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.LEFT_CLICK_BLOCK, x, y, z, side);
        if (event.isCanceled())
        {
        	stopBreaking();
            return;
        }
        
		this.initialDamage = this.curblockDamage;
        float var5 = 1.0F;
        

        if (block != null)
        {
            if (event.useBlock != Event.Result.DENY)
            {
                block.onBlockClicked(worldObj, x, y, z, player);
                // worldObj.extinguishFire(player, x, y, z, side);
            }
            var5 = block.getPlayerRelativeBlockHardness(player, worldObj, x, y, z);
        }

        if (event.useItem == Event.Result.DENY) {
        	stopBreaking();
            return;
        }

        if(var5 >= 1.0F)
        {
            this.tryHarvestBlock(x, y, z);
            stopBreaking();
            
            // make sure to animate
            forceAnimation = true;
            resendDescriptionPacket();
        }
        else
        {
            int var7 = (int)(var5 * 10.0F);
            worldObj.destroyBlockInWorldPartially(player.entityId, x, y, z, var7);
            this.durabilityRemainingOnBlock = var7;
        }
	}
	
	private void continueBreaking() {
		++this.curblockDamage;
        int var1;
        float var4;
        int var5;

        var1 = this.curblockDamage - this.initialDamage;
        int var2 = worldObj.getBlockId(breakingX, breakingY, breakingZ);

        if (var2 == 0)
        {
        	stopBreaking();
        }
        else
        {
            Block var3 = Block.blocksList[var2];
            var4 = var3.getPlayerRelativeBlockHardness(player, worldObj, breakingX, breakingY, breakingZ) * (float)(var1 + 1);
            var5 = (int)(var4 * 10.0F);

            if (var5 != this.durabilityRemainingOnBlock)
            {
                worldObj.destroyBlockInWorldPartially(player.entityId, breakingX, breakingY, breakingZ, var5);
                this.durabilityRemainingOnBlock = var5;
            }
            
            if (var4 >= 1.0F)
            {
                this.tryHarvestBlock(breakingX, breakingY, breakingZ);
                stopBreaking();
            }
        }
	}
	
    public boolean tryHarvestBlock(int par1, int par2, int par3)
    {
        //if (this.gameType.isAdventure() && !this.thisPlayerMP.canCurrentToolHarvestBlock(par1, par2, par3))
        //{
        //    return false;
        //}
        //else
        {
            ItemStack stack = tool;
            if (stack != null && stack.getItem().onBlockStartBreak(stack, par1, par2, par3, player))
            {
            	return false;
            }
            int var4 = worldObj.getBlockId(par1, par2, par3);
            int var5 = worldObj.getBlockMetadata(par1, par2, par3);
            worldObj.playAuxSFXAtEntity(player, 2001, par1, par2, par3, var4 + (var5 << 12));
            boolean var6 = false;

            //if (this.isCreative())
            //{
            //    var6 = this.removeBlock(par1, par2, par3);
            //    this.thisPlayerMP.playerNetServerHandler.sendPacketToPlayer(new Packet53BlockChange(par1, par2, par3, this.theWorld));
            //}
            //else
            {
                ItemStack var7 = tool;
                boolean var8 = false;
                Block block = Block.blocksList[var4];
                if (block != null)
                {
                    var8 = block.canHarvestBlock(player, var5);
                }

                int prevSize = worldObj.loadedEntityList.size();
                if (var7 != null)
                {
                    var7.onBlockDestroyed(worldObj, var4, par1, par2, par3, player);

                    if (var7.stackSize == 0)
                    {
                        this.player.destroyCurrentEquippedItem();
                        //MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, var7));
                    }
                }

                var6 = this.removeBlock(par1, par2, par3);
                if (var6 && var8)
                {
                	Block.blocksList[var4].harvestBlock(worldObj, player, par1, par2, par3, var5);
                    int newSize = worldObj.loadedEntityList.size();
                    double x = xCoord + 0.5;
                    double y = yCoord + 0.5;
                    double z = zCoord + 0.5;
                    
                    switch(facing) {
                	case Dir.NX: x += 0.7; break;
                	case Dir.NY: y += 0.7; break;
                	case Dir.NZ: z += 0.7; break;
                	case Dir.PX: x -= 0.7; break;
                	case Dir.PY: y -= 0.7; break;
                	case Dir.PZ: z -= 0.7; break;
                	}
                    
                    for(int k = prevSize; k < newSize; k++) {
                    	Entity e = (Entity)worldObj.loadedEntityList.get(k);
                    	
                    	e.setPosition(x, y, z);
                    	
                    	e.motionX = e.motionY = e.motionZ = 0;
                    	
                    	switch(facing) {
                    	case Dir.NX: e.motionX += 0.30; break;
                    	case Dir.NY: e.motionY += 0.30; break;
                    	case Dir.NZ: e.motionZ += 0.30; break;
                    	case Dir.PX: e.motionX -= 0.30; break;
                    	case Dir.PY: e.motionY -= 0.30; break;
                    	case Dir.PZ: e.motionZ -= 0.30; break;
                    	}
                    }
                }
            }

            return var6;
        }
    }
    
    private boolean removeBlock(int par1, int par2, int par3)
    {
        Block var4 = Block.blocksList[worldObj.getBlockId(par1, par2, par3)];
        int var5 = worldObj.getBlockMetadata(par1, par2, par3);

        if (var4 != null)
        {
            var4.onBlockHarvested(worldObj, par1, par2, par3, var5, player);
        }

        boolean var6 = (var4 != null && var4.removeBlockByPlayer(worldObj, player, par1, par2, par3));

        if (var4 != null && var6)
        {
            var4.onBlockDestroyedByPlayer(worldObj, par1, par2, par3, var5);
        }

        return var6;
    }
	
	@Override
	public void onPlaced(EntityLiving player, int look2) {
		super.onPlaced(player, look2);
		
		Vec3 look = player.getLook(1.0f);
		
        if(Math.abs(look.xCoord) > Math.abs(look.zCoord)) {
        	if(look.xCoord < 0)
        		look2 = Dir.NX;
        	else
        		look2 = Dir.PX;
        } else {
        	if(look.zCoord < 0)
        		look2 = Dir.NZ;
        	else
        		look2 = Dir.PZ;
        }
		
		facing = (byte)look2;
	}
}