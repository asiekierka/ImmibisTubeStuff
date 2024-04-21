package immibis.tubestuff;

import immibis.core.api.porting.SidedProxy;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemStorage extends ItemBlock {
	public ItemStorage(int id_minus_256) {
		super(id_minus_256);
		
		setMaxDamage(0);
	    setHasSubtypes(true);
	    
	    SidedProxy.instance.addLocalization("tubestuff.storage.0.name", "Silver Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.1.name", "Tin Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.2.name", "Copper Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.3.name", "Nikolite Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.4.name", "Coal Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.5.name", "Redstone Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.6.name", "Blue Alloy Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.7.name", "Red Alloy Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.8.name", "Brass Block");
	    SidedProxy.instance.addLocalization("tubestuff.storage.9.name", "Charcoal Block");
	}
	
	@Override
    public int getMetadata(int meta) {
        return meta;
    }
	
	@Override
	public String getItemNameIS(ItemStack s) {
		return "tubestuff.storage."+s.getItemDamage();
	}
}
