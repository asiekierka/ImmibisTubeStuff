package immibis.tubestuff;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.server.MinecraftServer;
import immibis.core.TileCombined;

public class TileOnlineDetector extends TileCombined {
	public String owner;
	
	@Override
	public void onPlaced(EntityLiving player, int look) {
		if(player instanceof EntityPlayer)
			owner = ((EntityPlayer)player).username;
		else
			owner = "";
	}
	
	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setString("owner", owner);
		par1nbtTagCompound.setBoolean("out", redstone_output);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		owner = par1nbtTagCompound.getString("owner");
		redstone_output = par1nbtTagCompound.getBoolean("out");
	}
	
	@Override
	public Packet getDescriptionPacket() {
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, redstone_output ? 1 : 0, null);
	}
	
	@Override
	public void onDataPacket(Packet132TileEntityData packet) {
		redstone_output = (packet.actionType != 0);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	private int updateTime = 0; 
	
	@Override
	public void updateEntity() {
		if(!worldObj.isRemote && --updateTime < 0) {
			updateTime = 20;
			updateNow();
		}
	}
	
	public void updateNow() {
		boolean old = redstone_output;
		redstone_output = (MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(owner) != null);
		
		if(redstone_output != old) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType().blockID);
		}
	}
}
