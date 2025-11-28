package com.hbm.tileentity.machine;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.tile.IHeatable;
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
import com.hbm.uninos.UniNodespace;
import com.hbm.util.fauxpointtwelve.BlockPos;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Based off of {@link TileEntityHeaterHeatex}
 * This heat exchanger takes in cold or hot coolant and passively converts between the two. Usually this will result in a balance of hot and cold coolant within the exchanger.
 * This block will transfer heat to any adjacent blocks, including other heat exchangers which may cause state changes in their fluids.
 * @author Jack Andersen
 */
public class TileEntityHeatExchanger extends TileEntityMachineBase implements IHeatable, IFluidStandardTransceiverMK2, IFluidCopiable {
	public static final float radiativity = 0.0002F;
	public static final float diffusion = 0.85F;
	public static int baseTankSize = 8000;
	// Tank 0 is the hot result, Tank 1 is the cold result.
	public FluidTank[] tanks = new FluidTank[]{
		new FluidTank(Fluids.STEAM, baseTankSize),
		new FluidTank(Fluids.WATER, baseTankSize)
	};
	protected FluidNode[] nodes = new FluidNode[2];

	public FluidTank getHotTank(){
		return tanks[0];
	}
	public FluidTank getColdTank(){
		return tanks[1];
	}

	public int tickDelay = 1;
	public int heatEnergy=0;
	public int step = 0;
	protected HeatExchangingRates rates;

	public TileEntityHeatExchanger() {
		super(1);
		recalculateConsts();
	}

	public void recalculateConsts(){
		rates = new HeatExchangingRates();
		if(rates.isSet()) {
			for (int i = 0; i < tanks.length; i++) {
				FluidTank tank = tanks[i];
				this.nodes[i] = (FluidNode) UniNodespace.getOrCreateNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider(),()->this.createNode(tank.getTankType()));
			}
			getHotTank().changeTankSize(rates.coolable.amountReq * baseTankSize);
		}
	}

	protected FluidNode createNode(FluidType type) {
		DirPos[] conPos = getConPos();

		HashSet<BlockPos> posSet = new HashSet<>();
		posSet.add(new BlockPos(this));
		for(DirPos pos : conPos) {
			ForgeDirection dir = pos.getDir();
			posSet.add(new BlockPos(pos.getX() - dir.offsetX, pos.getY() - dir.offsetY, pos.getZ() - dir.offsetZ));
		}

		return new FluidNode(type.getNetworkProvider(), posSet.toArray(new BlockPos[posSet.size()])).setConnections(conPos);
	}

	@Override
	public boolean canAbsorbFrom(int x, int y, int z) {
		return true;
	}

	@Override
	public boolean canDistributeTo(int x, int y, int z) {
		return true;
	}

	@Override
	public String getName() {
		return "container.heatExchanger";
	}


	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {
			this.updateConnections();

			//this.heatEnergy *= 0.999;

			this.tryConvert();
			this.doTankBehavior();
			this.doHeatDistribute();
			networkPackNT(25);
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
		recalculateConsts();
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
		rates.balance();
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
	public void setHeat(int heatEnergy) {
		this.heatEnergy = heatEnergy;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
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
			tanks[0].setTankType(rates.step.typeProduced);
			tanks[0].setFill(0);
			recalculateConsts();
			markChanged();
		}
	}

	public void doHeatDistribute(){
		for (DirPos pos : getConPos()) {
			balanceHeat(worldObj,diffusion,pos.getX(),pos.getY(),pos.getZ());
		}
		radiateAllSides(worldObj,radiativity,getConPos());
	}

	public void doTankBehavior() {
		if(!worldObj.isRemote) {
			for (int i = 0; i < nodes.length; i++) {
				FluidNode node = nodes[i];
				if(node != null && node.hasValidNet()) {
					node.net.addProvider(this);
					node.net.addReceiver(this);
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!worldObj.isRemote) {
			for (int i = 0; i < nodes.length; i++) {
				FluidNode node = nodes[i];
				if (node != null) {
					for (FluidTank tank : tanks) {
						UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, tank.getTankType().getNetworkProvider());
					}
				}
			}
		}
	}


	public class HeatExchangingRates {
		private final int heatingEnergy;
		private final int coolingEnergy;
		private final FT_Coolable coolable;
		private final FT_Heatable heatable;
		private final FT_Heatable.HeatingStep step;

		public HeatExchangingRates() {
			coolable = getHotTank().getTankType().getTrait(FT_Coolable.class);
			heatable = getColdTank().getTankType().getTrait(FT_Heatable.class);
			if(coolable == null) {
				heatingEnergy = 0;
				coolingEnergy = 0;
				step=null;
				return;
			}
			this.step = heatable.getStep(TileEntityHeatExchanger.this.step);

			// Amount of energy consumed per heating operation.
			heatingEnergy = (int) (step.heatReq * heatable.getHeatConsumptionMultiplier(FT_Heatable.HeatingType.HEATEXCHANGER));
			// Amount of energy produced per cooling operation.
			coolingEnergy = (int) (coolable.heatEnergy * coolable.getHeatOutputMultiplier(CoolingType.HEATEXCHANGER));
		}

		public void balance(){
			FluidTank cold = getColdTank();
			FluidTank hot = getHotTank();
			// Amount of cooling ops possible with the hot coolant, ignoring inefficiency.
			int hotFluidEnergy = coolable.heatEnergy*hot.getFill()/coolable.amountReq;
			int machineHotEnergy = heatEnergy/coolable.heatEnergy;
			int availableEnergy = (heatEnergy+step.heatReq*hot.getFill())/2;
			// Try to heat cold coolant
			if(machineHotEnergy > hotFluidEnergy){
				int ops = machineHotEnergy-hotFluidEnergy;
				ops = Math.min(ops,cold.getFill()/step.amountReq);
				ops = Math.min(ops,hot.getRemainingFill()/step.amountProduced);
				ops = Math.min(ops,availableEnergy/heatingEnergy);
				if(ops <= 0){
					return;
				}
				heatEnergy-=ops*heatingEnergy;
				hot.grow(ops*step.amountProduced);
				cold.shrink(ops*step.amountReq);
			} else if(machineHotEnergy < hotFluidEnergy){
				int ops = hotFluidEnergy-machineHotEnergy;
				//System.out.println("1: " + ops + ", " + hotFluidEnergy + ", " + machineHotEnergy);
				ops = Math.min(ops,hot.getFill()/coolable.amountReq);
				//System.out.println("2: " + ops + ", " + hot.getFill() + ", " + coolable.amountReq);
				ops = Math.min(ops,cold.getRemainingFill()/coolable.amountProduced);
				//System.out.println("3: " + ops + ", " + cold.getRemainingFill() + ", " + coolable.amountProduced);
				ops = Math.min(ops,availableEnergy/coolingEnergy);
				//System.out.println("4: " + ops + ", " + availableEnergy + ", " + coolingEnergy);
				if(ops <= 0){
					return;
				}
				//System.out.println("5: " + ops + ", " + ops*step.amountReq + ", " + ops*step.amountProduced);
				heatEnergy+=ops*coolingEnergy;
				hot.shrink(ops*step.amountProduced);
				cold.grow(ops*step.amountReq);
			}
		}

		public boolean isSet(){
			return step != null;
		}

		@Override
		public String toString() {
			return "HeatExchangingRates{" +
				"heatingEnergy=" + heatingEnergy +
				", coolingEnergy=" + coolingEnergy +
				", coolable=" + coolable +
				", heatable=" + heatable +
				", step=" + step +
				'}';
		}
	}
}
