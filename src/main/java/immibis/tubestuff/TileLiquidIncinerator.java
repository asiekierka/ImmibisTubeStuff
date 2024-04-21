package immibis.tubestuff;

import immibis.core.TileCombined;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

public class TileLiquidIncinerator extends TileCombined implements ILiquidTank, ITankContainer {

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return resource.amount;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		return resource.amount;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return null;
	}

	private ILiquidTank[] tanks = {this};
	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return tanks;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return this;
	}

	@Override
	public LiquidStack getLiquid() {
		return null;
	}

	@Override
	public int getCapacity() {
		return 10000;
	}

	@Override
	public int fill(LiquidStack resource, boolean doFill) {
		return resource.amount;
	}

	@Override
	public LiquidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public int getTankPressure() {
		return -1;
	}

}
