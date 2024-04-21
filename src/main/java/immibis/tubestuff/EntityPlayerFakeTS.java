package immibis.tubestuff;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumStatus;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityPlayerFakeTS extends EntityPlayer {
	
	public EntityPlayerFakeTS(World w) {
		super(w);
		username = "[TubeStuff]";
	}

// OVERRIDE ALL THE THINGS!
	
	// TODO update full list with 1.3

	@Override public ItemStack getItemInUse() {return null;}
	@Override public int getItemInUseCount() {return 0;}
	@Override public int getItemInUseDuration() {return 0;}
	@Override public void handleHealthUpdate(byte par1) {}
	@Override public void updateCloak() {}
	@Override public void preparePlayerToSpawn() {}
	@Override public int getScore() {return 0;}
	@Override public void respawnPlayer() {}
	@Override public float getBedOrientationInDegrees() {return 0;}
	@Override public int getSleepTimer() {return 0;}
	@Override public int getItemIcon(ItemStack par1ItemStack, int par2) {return 0;}

@Override public int getMaxHealth() {return 20;}
@Override public boolean isBlocking() {return false;}
@Override public void onUpdate() {}
@Override protected void updateItemUse(ItemStack par1ItemStack, int par2) {}
@Override protected void onItemUseFinish() {}
@Override protected boolean isMovementBlocked() {return false;}
@Override public void closeScreen() {}
@Override public void updateRidden() {}
@Override protected void updateEntityActionState() {}
@Override public void onLivingUpdate() {}
@Override public void onDeath(DamageSource par1DamageSource) {}
@Override public void addToPlayerScore(Entity par1Entity, int par2) {}
@Override protected int decreaseAirSupply(int par1) {return 0;}
@Override public EntityItem dropPlayerItem(ItemStack par1ItemStack) {return null;}
@Override public EntityItem dropPlayerItemWithRandomChoice(ItemStack par1ItemStack, boolean par2) {return null;}
@Override public void joinEntityItemWithWorld(EntityItem par1EntityItem) {}
@Override public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}
@Override public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}
@Override public void displayGUIChest(IInventory par1IInventory) {}
@Override public void displayGUIEnchantment(int par1, int par2, int par3) {}
@Override public void onItemPickup(Entity par1Entity, int par2) {}
@Override public float getEyeHeight() {return 0.12f;}
@Override protected void resetHeight() {}
@Override public boolean attackEntityFrom(DamageSource par1DamageSource, int par2) {return false;}
@Override protected int applyPotionDamageCalculations(DamageSource par1DamageSource, int par2) {return 0;}
@Override protected boolean isPVPEnabled() {return false;}
@Override protected void alertWolves(EntityLiving par1EntityLiving, boolean par2) {}
@Override protected void damageArmor(int par1) {}
@Override public int getTotalArmorValue() {return 0;}
@Override protected void damageEntity(DamageSource par1DamageSource, int par2) {}
@Override public void displayGUIFurnace(TileEntityFurnace par1TileEntityFurnace) {}
@Override public void displayGUIDispenser(TileEntityDispenser par1TileEntityDispenser) {}
@Override public void displayGUIBrewingStand(TileEntityBrewingStand par1TileEntityBrewingStand) {}
@Override public void displayGUIMerchant(IMerchant par1IMerchant) {}
@Override public void displayGUIBook(ItemStack par1ItemStack) {}
@Override public void displayGUIBeacon(TileEntityBeacon par1TileEntityBeacon) {}
@Override public void addExperienceLevel(int i) {}
@Override public void displayGUIEditSign(TileEntity par1TileEntitySign) {}
@Override public double getYOffset() {return yOffset - 0.5f;}
@Override public void swingItem() {}
@Override public void attackTargetEntityWithCurrentItem(Entity par1Entity) {}
@Override public void onCriticalHit(Entity par1Entity) {}
@Override public void onEnchantmentCritical(Entity par1Entity) {}
@Override public void setDead() {}
@Override public boolean isEntityInsideOpaqueBlock() {return false;}
@Override public EnumStatus sleepInBedAt(int par1, int par2, int par3) {return EnumStatus.OTHER_PROBLEM;}
@Override public void wakeUpPlayer(boolean par1, boolean par2, boolean par3) {}
@Override public boolean isPlayerSleeping() {return false;}
@Override public boolean isPlayerFullyAsleep() {return false;}
@Override public void addChatMessage(String par1Str) {}
@Override public void triggerAchievement(StatBase par1StatBase) {}
@Override public void addStat(StatBase par1StatBase, int par2) {}
@Override protected void jump() {}
@Override public void moveEntityWithHeading(float par1, float par2) {}
@Override public void addMovementStat(double par1, double par3, double par5) {}
@Override protected void fall(float par1) {}
@Override public void onKillEntity(EntityLiving par1EntityLiving) {}
@Override public void setInPortal() {}
@Override public void addExperience(int par1) {}
@Override public int xpBarCap() {return 7;}
@Override public void addExhaustion(float par1) {}
@Override public boolean canEat(boolean par1) {return false;}
@Override public boolean shouldHeal() {return true;}
@Override public void setItemInUse(ItemStack par1ItemStack, int par2) {}
@Override protected int getExperiencePoints(EntityPlayer par1EntityPlayer) {return 0;}
@Override protected boolean isPlayer() {return true;}
@Override public void travelToDimension(int par1) {}
@Override protected boolean canTriggerWalking() {return false;}
@Override public void sendChatToPlayer(String var1) {}
@Override public boolean canCommandSenderUseCommand(int var1, String var2) {return false;}
@Override public ChunkCoordinates getPlayerCoordinates() {return null;}
}
