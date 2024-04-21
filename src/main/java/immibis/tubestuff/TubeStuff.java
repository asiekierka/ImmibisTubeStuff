package immibis.tubestuff;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import ic2.api.Ic2Recipes;
import ic2.core.Ic2Items;
import immibis.core.Config;
import immibis.core.api.APILocator;
import immibis.core.api.FMLModInfo;
import immibis.core.api.IIDCallback;
import immibis.core.api.net.IPacket;
import immibis.core.api.net.IPacketMap;
import immibis.core.api.porting.PortableBaseMod;
import immibis.core.api.porting.SidedProxy;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.proxy.SimpleServiceLocator;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

import buildcraft.api.tools.IToolWrench;

import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(version="52.3.2", modid="Tubestuff", name="Tubestuff", dependencies="required-after:ImmibisCore")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
@FMLModInfo(
		url="http://www.minecraftforum.net/topic/1001131-110-immibiss-mods-smp/",
		description="A collection of blocks that are useful with BuildCraft or RedPower.",
		authors="immibis"
		)
public class TubeStuff extends PortableBaseMod implements IPacketMap, IGuiHandler {
	public static TubeStuff instance;
	public static BlockTubestuff block;
	public static BlockStorage blockStorage;
	public static Item itemUseless;

	public static boolean enableSlowFalling;

	private static EntityPlayerFakeTS fakePlayer;

	public static final int GUI_BUFFER = 0;
	public static final int GUI_TABLE = 1;
	public static final int GUI_CHEST = 2;
	public static final int GUI_LOGICCRAFTER = 3;
	public static final int GUI_RETRIEVULATOR = 4;
	public static final int GUI_DUPLICATOR = 5;

	public static final byte PKT_BLOCK_BREAKER_DESC = 0;
	public static final byte PKT_ACT2_RECIPE_UPDATE = 1;

	public static final String CHANNEL = "TubeStuff";

	public static EntityPlayerFakeTS fakePlayer(World world) {
		if(fakePlayer == null)
			fakePlayer = new EntityPlayerFakeTS(world);
		return fakePlayer;
	}

	public TubeStuff() {
		instance = this;
	}

	public String getPriorities() {
		return "after:mod_ImmibisCore";
	}

	private boolean hadFirstTick = false;
	@Override
	public boolean onTickInGame() {
		if(!hadFirstTick) {
			SharedProxy.FirstTick();
			hadFirstTick = true;
		}
		return false;
	}
	
	public static List<Item> findItemsByClass(String className) {
		List<Item> matches = new ArrayList<Item>();
		for(Item i : Item.itemsList)
			if(i != null && i.getClass().getName().equals(className)) {
				//System.out.println("TubeStuff: found "+className+" at ID "+i.shiftedIndex+", first name is "+i.getItemNameIS(new ItemStack(i, 1, 0)));
				matches.add(i);
			}
		return matches;
	}
	
	public static List<Block> findBlocksByClass(String className) {
		List<Block> matches = new ArrayList<Block>();
		for(Block i : Block.blocksList)
			if(i != null && i.getClass().getName().equals(className)) {
				//System.out.println("TubeStuff: found "+className+" at ID "+i.blockID+", first name is "+Item.itemsList[i.blockID].getItemNameIS(new ItemStack(i, 1, 0)));
				matches.add(i);
			}
		return matches;
	}
	
	public static Item findItemByClass(String className) {
		List<Item> matches = findItemsByClass(className);
		if(matches.size() == 0)
			return null;
		if(matches.size() == 1)
			return matches.get(0);
		throw new RuntimeException("Multiple items found with class: "+className);
	}
	
	public static Block findBlockByClass(String className) {
		for(Block b : Block.blocksList)
			if(b != null && b.getClass().getName().equals(className)) {
				//System.out.println("TubeStuff: found "+className+" at ID "+b.blockID);
				return b;
			}
		return null;
	}

	@PreInit
	public void preinit(FMLPreInitializationEvent evt) {
		APILocator.getIDAllocator().requestBlock(this, "tubestuff", new IIDCallback() {
			@Override
			public void register(int id) {
				block = new BlockTubestuff(id);
				GameRegistry.registerBlock(block, ItemTubestuff.class);
			}
		});

		SharedProxy.enableStorageBlocks = Config.getBoolean("tubestuff.enableStorageBlocks", true);
		SharedProxy.enableStorageBlocksVanilla = Config.getBoolean("tubestuff.enableStorageBlocks.vanilla", true);
		SharedProxy.enableStorageBlockOreDictionary = Config.getBoolean("tubestuff.enableStorageBlocks.useOreDictionary", true);

		enableSlowFalling = Config.getBoolean("tubestuff.enableSlowDustFalling", false);

		if(SharedProxy.enableStorageBlocks) {
			APILocator.getIDAllocator().requestBlock(this, "tubestuff.storage", new IIDCallback() {
				@Override
				public void register(int id) {
					blockStorage = new BlockStorage(id);
					GameRegistry.registerBlock(blockStorage, ItemStorage.class);
				}
			});
		}

		APILocator.getIDAllocator().requestItem(this, "tubestuff.uselessItem", new IIDCallback() {
			@Override
			public void register(int id) {
				itemUseless = (new Item(id-256));
				itemUseless.setTextureFile("/immibis/tubestuff/blocks.png");
				itemUseless.setItemName("tubestuff.uselessItem");
				itemUseless.setIconIndex(5);
				itemUseless.setMaxStackSize(1);
				LanguageRegistry.instance().addNameForObject(itemUseless, "en_US", "Retriever jammer");
			}
		});
	}

	@Init
	public void load(FMLInitializationEvent evt) {
		BlockTubestuff.model = SidedProxy.instance.getUniqueBlockModelID("immibis.tubestuff.BlockRenderer", true);

		enableClockTicks(true);
		enableClockTicks(false);

		NetworkRegistry.instance().registerGuiHandler(this, this);

		GameRegistry.registerTileEntity(TileBuffer.class, "TubeStuff buffer");
		GameRegistry.registerTileEntity(TileAutoCraftingMk2.class, "TubeStuff crafting table");
		GameRegistry.registerTileEntity(TileLogicCrafter.class, "TubeStuff logic crafter");
		GameRegistry.registerTileEntity(TileRetrievulator.class, "TubeStuff retrievulator");
		GameRegistry.registerTileEntity(TileIncinerator.class, "TubeStuff incinerator");
		GameRegistry.registerTileEntity(TileDuplicator.class, "TubeStuff duplicator");
		SidedProxy.instance.registerTileEntity(TileBlackHoleChest.class, "TubeStuff infinite chest", "immibis.tubestuff.RenderTileBlackHoleChest");
		SidedProxy.instance.registerTileEntity(TileBlockBreaker.class, "TubeStuff block breaker", "immibis.tubestuff.RenderTileBlockBreaker");
		GameRegistry.registerTileEntity(TileLiquidIncinerator.class, "TubeStuff liquid incinerator");
		GameRegistry.registerTileEntity(TileLiquidDuplicator.class, "TubeStuff liquid duplicator");
		GameRegistry.registerTileEntity(TileOnlineDetector.class, "TubeStuff online detector");
		
		MinecraftForge.EVENT_BUS.register(this);

		try {
			Ic2Recipes.addRecyclerBlacklistItem(itemUseless);
		} catch(Throwable e) {

		}

		APILocator.getNetManager().listen(this);
	}

	@Mod.PostInit
	public void postInit(FMLPostInitializationEvent evt) {
		SimpleServiceLocator.addCraftingRecipeProvider(new AutoCraftingMk2CraftingRecipeProvider());
	}

	public static final boolean areItemsEqual(ItemStack recipe, ItemStack input)
	{
		return input.itemID == recipe.itemID && (!input.getHasSubtypes() || input.getItemDamage() == recipe.getItemDamage());
	}

	@ForgeSubscribe
	public void onPickup(EntityItemPickupEvent evt) {
		if(evt.item.getEntityItem() != null && evt.item.getEntityItem().stackSize > 0 && evt.item.getEntityItem().itemID == itemUseless.itemID) {
			evt.item.setDead();
			evt.setCanceled(true);
		}
	}

	private static Object icWrenchItem = null;
	private static Class<?> bcWrenchInterface = null;

	/**
	 * Accepts any of the following items:
	 * <ul>
	 * <li>BuildCraft wrench</li>
	 * <li>IndustrialCraft wrench</li>
	 * <li>RedPower screwdriver</li>
	 * <li>RedPower sonic screwdriver (if charged)</li>
	 * <li>Vanilla stone hoe</li>
	 * </ul>
	 * No charge or durability is used.
	 */
	public static boolean isValidWrench(ItemStack s) {
		if(s == null)
			return false;

		Item i = s.getItem();
		if(i == Item.hoeStone)
			return true;

		if(bcWrenchInterface == null) {
			try {
				bcWrenchInterface = IToolWrench.class;
			} catch(Throwable t) {
				bcWrenchInterface = TubeStuff.class; // some random unrelated class
			}
		}

		if(icWrenchItem == null) {
			try {
				icWrenchItem = Ic2Items.wrench.getItem();
			} catch(Throwable e) {
				icWrenchItem = new Object();
			}
		}

		if(i == icWrenchItem || bcWrenchInterface.isInstance(i))
			return true;

		if(RedPowerItems.useScrewdriver(s))
			return true;

		return false;
	}

	@Override
	public String getChannel() {
		return CHANNEL;
	}

	@Override
	public IPacket createS2CPacket(byte id) {
		switch(id) {
		case PKT_BLOCK_BREAKER_DESC: return new PacketBlockBreakerDescription();
		case PKT_ACT2_RECIPE_UPDATE: return new PacketACT2RecipeUpdate();
		default: return null;
		}
	}

	@Override
	public IPacket createC2SPacket(byte id) {
		switch(id) {
		case PKT_ACT2_RECIPE_UPDATE: return new PacketACT2RecipeUpdate();
		default: return null;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		switch(ID) {
		case GUI_BUFFER:
			return new ContainerChest(player.inventory, (TileBuffer)tile);
		case GUI_TABLE:
			return new ContainerAutoCraftingMk2(player, (TileAutoCraftingMk2)tile);
		case GUI_CHEST:
			return new ContainerBlackHoleChest(player, (TileBlackHoleChest)tile);
		case GUI_LOGICCRAFTER:
			return new ContainerLogicCrafter(player, (TileLogicCrafter)tile);
		case GUI_DUPLICATOR:
			return new ContainerOneSlot(player, ((IDuplicator)tile).getGuiInventory());
		case GUI_RETRIEVULATOR:
			return new ContainerRetrievulator(player, (TileRetrievulator)tile);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		switch(ID) {
		case GUI_BUFFER:
			return new GuiChest(player.inventory, (TileBuffer)tile);
		case GUI_TABLE:
			return new GuiAutoCraftingMk2(player, (TileAutoCraftingMk2)tile);
		case GUI_CHEST:
			return new GuiBlackHoleChest(player, (TileBlackHoleChest)tile);
		case GUI_LOGICCRAFTER:
			return new GuiLogicCrafter(player, (TileLogicCrafter)tile);
		case GUI_DUPLICATOR:
			return new GuiOneSlot(new ContainerOneSlot(player, ((IDuplicator)tile).getGuiInventory()), "Duplicator");
		case GUI_RETRIEVULATOR:
			return new GuiRetrievulator(player, (TileRetrievulator)tile);
		}
		return null;
	}
}
