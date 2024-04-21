package immibis.tubestuff;

import immibis.core.api.net.IPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketBlockBreakerDescription implements IPacket {

	public ItemStack tool;
	public int x, y, z;
	public byte facing;
	public boolean isBreaking;
	
	public PacketBlockBreakerDescription() {}
	
	public PacketBlockBreakerDescription(int x, int y, int z, ItemStack tool, byte facing, boolean isBreaking) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tool = tool;
		this.facing = facing;
		this.isBreaking = isBreaking;
	}
	
	@Override
	public byte getID() {
		return TubeStuff.PKT_BLOCK_BREAKER_DESC;
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		facing = in.readByte();
		isBreaking = in.readBoolean();
		
		short id = in.readShort();
		if(id >= 0) {
			short dmg = in.readShort();
			tool = new ItemStack(id, 1, dmg);
		} else
			tool = null;
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeByte(facing);
		out.writeBoolean(isBreaking);
		
		if(tool == null)
			out.writeShort(-1);
		else {
			out.writeShort(tool.itemID);
			out.writeShort(tool.getItemDamage());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onReceived(EntityPlayer source) {
		TileEntity te = net.minecraft.client.Minecraft.getMinecraft().theWorld.getBlockTileEntity(x, y, z);
		if(te instanceof TileBlockBreaker) {
			TileBlockBreaker tbb = (TileBlockBreaker)te;
			tbb.tool = tool;
			tbb.facing = facing;
			tbb.isBreaking = isBreaking;
		}
	}
	
	@Override
	public String getChannel() {
		return TubeStuff.CHANNEL;
	}

}
