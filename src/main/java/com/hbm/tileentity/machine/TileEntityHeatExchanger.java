package com.hbm.tileentity.machine;

import api.hbm.block.IToolable;
import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.tile.IHeatSource;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Based off of {@link TileEntityHeaterHeatex}
 * This heat exchanger takes in cold or hot coolant and passively converts between the two. Usually this will result in a balance of hot and cold coolant within the exchanger.
 * This block will transfer heat to any adjacent blocks, including other heat exchangers which may cause state changes in their fluids.
 * @author Jack Andersen
 */
public class TileEntityHeatExchanger extends TileEntityMachineBase implements IHeatSource, IFluidStandardTransceiver, IFluidCopiable {
	// Tank 0 is the hot result, Tank 1 is the cold result.
	public FluidTank[] tanks;
	public int tickDelay = 1;
	public int heatEnergy;
	public int step = 0;
	protected HeatExchangingRates rates = new HeatExchangingRates();

	public TileEntityHeatExchanger() {
		super(1);
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.COOLANT_HOT, 8_000);
		this.tanks[1] = new FluidTank(Fluids.COOLANT, 8_000);
	}

	public void recalculateConsts(){
		rates = new HeatExchangingRates();
	}

	@Override
	public String getName() {
		return "container.heatExchanger";
	}


	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {
			this.tanks[0].setType(0, slots);
			this.setupTanks();
			this.updateConnections();

			this.heatEnergy *= 0.999;

			this.tryConvert();

			networkPackNT(25);

			for(DirPos pos : getConPos()) {
				if(this.tanks[0].getFill() > 0) this.sendFluid(tanks[0], worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				if(this.tanks[1].getFill() > 0) this.sendFluid(tanks[1], worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		ByteBuf tanks = Unpooled.buffer();
		this.tanks[0].serialize(tanks);
		this.tanks[1].serialize(tanks);
		buf.writeBytes(tanks);
		buf.writeInt(this.heatEnergy);
		buf.writeInt(this.tickDelay);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		this.heatEnergy = buf.readInt();
		this.tickDelay = buf.readInt();
	}

	protected void setupTanks() {

		if(tanks[0].getTankType().hasTrait(FT_Coolable.class)) {
			FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);
			if(trait.getEfficiency(CoolingType.HEATEXCHANGER) > 0) {
				tanks[1].setTankType(trait.coolsTo);
				return;
			}
		}

		tanks[0].setTankType(Fluids.NONE);
		tanks[1].setTankType(Fluids.NONE);
	}

	protected void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), worldObj, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	protected void tryConvert() {
		if(!rates.isSet()) return;
		if(tickDelay < 1) tickDelay = 1;
		if(worldObj.getTotalWorldTime() % tickDelay != 0) return;

		int consumableEnergy = rates.getConsumableEnergy();
		int releasableEnergy = rates.getReleasableEnergy();
		int machineReleasableEnergy = rates.getMachineReleasableEnergy();

		// By the end the hot coolant and machine energy should be changed towards this point.
		int balancingPoint = (releasableEnergy+machineReleasableEnergy)/2;
		// When equal do nothing, the heat is balanced.
		// When there is more energy in the machine than the hot coolant.
		// cold -> hot.
		if(machineReleasableEnergy > releasableEnergy){
			// The change in MRE and CE is negative and the change in RE is positive.
			int diff = machineReleasableEnergy-balancingPoint;
			// Truncate to consumable energy present.
			diff=Math.min(diff,consumableEnergy);
			// Truncate to maximum hot fluid space.
			diff=Math.min(diff,rates.maxReleasableEnergy-releasableEnergy);
			consumableEnergy-=diff;
			machineReleasableEnergy-=diff;
			releasableEnergy+=diff;
		}
		// When there is less energy in the machine than the hot coolant
		// hot -> cold.
		else if(machineReleasableEnergy < releasableEnergy){
			// The change in MRE and RE is positive and the change in CE is negative.
			int diff = releasableEnergy-balancingPoint;
			diff = Math.min(diff, rates.maxConsumableEnergy-consumableEnergy);
			consumableEnergy+=diff;
			machineReleasableEnergy+=diff;
			releasableEnergy-=diff;
		}

		tanks[0].setFill(releasableEnergy/rates.coolingEnergy);
		tanks[1].setFill(consumableEnergy/rates.heatingEnergy);
		heatEnergy = machineReleasableEnergy/rates.heatingEnergy;
		this.markChanged();
	}

	protected DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(xCoord + 1, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord - 1, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord, yCoord + 1, zCoord, Library.POS_Y),
			new DirPos(xCoord, yCoord - 1, zCoord, Library.NEG_Y),
			new DirPos(xCoord, yCoord, zCoord + 1, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord - 1, Library.NEG_Z)
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.tanks[0].readFromNBT(nbt, "0");
		this.tanks[1].readFromNBT(nbt, "1");
		this.step = nbt.getInteger("step");
		this.heatEnergy = nbt.getInteger("heatEnergy");
		this.tickDelay = nbt.getInteger("delay");
		recalculateConsts();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		this.tanks[0].writeToNBT(nbt, "0");
		this.tanks[1].writeToNBT(nbt, "1");
		nbt.setInteger("step",step);
		nbt.setInteger("heatEnergy", heatEnergy);
		nbt.setInteger("delay", tickDelay);
	}

	@Override
	public int getHeatStored() {
		return heatEnergy;
	}

	@Override
	public void useUpHeat(int heat) {
		this.heatEnergy = Math.max(0, this.heatEnergy - heat);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		ForgeDirection facing = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		return dir == facing || dir == facing.getOpposite();
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					xCoord - 1,
					yCoord,
					zCoord - 1,
					xCoord + 2,
					yCoord + 1,
					zCoord + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();
		if(getFluidIDToCopy().length > 0)
			nbt.setIntArray("fluidID", getFluidIDToCopy());
		return nbt;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		int[] ids = nbt.getIntArray("fluidID");
		if(ids.length > 0) {
			int id = ids[index];
			tanks[0].setTankType(Fluids.fromID(id));
		}
	}

	public void changeStep() {
		if(rates.isSet()) {
			step = (step+1)%rates.heatable.getStepCount();
			recalculateConsts();
		}
		markChanged();
	}

	public class HeatExchangingRates {
		private final int heatingEnergy;
		private final int coolingEnergy;
		private final int maxConsumableEnergy;
		private final int maxReleasableEnergy;
		private final FT_Coolable coolable;
		private final FT_Heatable heatable;
		private final FT_Heatable.HeatingStep step;

		public HeatExchangingRates() {
			coolable = tanks[0].getTankType().getTrait(FT_Coolable.class);
			heatable = tanks[1].getTankType().getTrait(FT_Heatable.class);
			if(coolable == null || heatable == null) {
				heatingEnergy = 0;
				coolingEnergy = 0;
				maxConsumableEnergy = 0;
				maxReleasableEnergy = 0;
				step=null;
				return;
			}
			this.step = heatable.getStep(TileEntityHeatExchanger.this.step);

			// Amount of energy consumed per heating operation.
			heatingEnergy = (int) (step.heatReq * heatable.getHeatConsumptionMultiplier(FT_Heatable.HeatingType.HEATEXCHANGER));
			// Amount of energy produced per cooling operation.
			coolingEnergy = (int) (coolable.heatEnergy * coolable.getHeatOutputMultiplier(CoolingType.HEATEXCHANGER));

			maxConsumableEnergy = tanks[1].getMaxFill() / step.amountReq * heatingEnergy;
			maxReleasableEnergy = tanks[0].getMaxFill() / coolable.amountReq * coolingEnergy;
		}

		public int getConsumableEnergy(){
			return tanks[1].getFill()/step.amountReq*heatingEnergy;
		}

		public int getReleasableEnergy(){
			return tanks[0].getFill()/coolable.amountReq*coolingEnergy;
		}

		public int getMachineReleasableEnergy(){
			return heatEnergy/step.amountReq*heatingEnergy;
		}

		public boolean isSet(){
			return coolable != null;
		}
	}
}
