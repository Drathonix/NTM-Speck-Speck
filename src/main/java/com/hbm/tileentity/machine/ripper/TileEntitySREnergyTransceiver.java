package com.hbm.tileentity.machine.ripper;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.Nodespace;
import com.hbm.calc.Location;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.Tuple;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;

public class TileEntitySREnergyTransceiver extends TileEntitySRBase implements IEnergyReceiverMK2, IEnergyProviderMK2, IControlReceiver {
	// Sending energy into the rift (so actually receiving energy from a power net)
	public boolean isReceiving;
	public ConnectionPriority priority = ConnectionPriority.LOW;

	public TileEntitySREnergyTransceiver() {
		super(2);
	}

	@Override
	public long getPower() {
		return linkedPocketDimension == null ? 0 : (isReceiving ? 0 : linkedPocketDimension.getPowerLong());
	}

	@Override
	public void usePower(long power) {
		if(linkedPocketDimension != null) {
			linkedPocketDimension.removeEnergy(power);
		}
	}

	@Override
	public void setPower(long power) {

	}

	@Override
	public long transferPower(long power) {
		if(linkedPocketDimension != null) {
			linkedPocketDimension.insertEnergy(power);
		}
		return 0;
	}

	@Override
	public long getMaxPower() {
		return Long.MAX_VALUE;
	}

	@Override
	public String getName() {
		return "container.spaceRipperEnergyTransceiver";
	}

	@Override
	public void updateEntity() {

	}

	/**
	 * Called by {@link PocketDimension#tick()}
	 */
	@SuppressWarnings("all")
	public void distributePower() {
		if(!isReceiving){
			long power = linkedPocketDimension.getPowerLong();
			long powerRemaining = Library.chargeItemsFromTE(slots,1,power,getMaxPower());
			// This is necessary because the pocket dimension stores energy in big int form.
			linkedPocketDimension.removeEnergy(power-powerRemaining);
		}
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			tryProvide(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
			Nodespace.PowerNode dirNode = (Nodespace.PowerNode) UniNodespace.getNode(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, Nodespace.THE_POWER_PROVIDER);
			if(isReceiving){
				if (dirNode != null && dirNode.hasValidNet()) {
					dirNode.net.addReceiver(this);
				}
				if (dirNode != null && dirNode.hasValidNet()){
					dirNode.net.removeReceiver(this);
				}
			} else {
				tryProvide(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
				if (dirNode != null && dirNode.hasValidNet()){
					dirNode.net.removeProvider(this);
				}
			}
		}
		if(isReceiving){
			linkedPocketDimension.insertEnergy(Library.chargeTEFromItems(slots,0,0,getMaxPower()));
		}
		this.networkPackNT(20);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("mode")){
			isReceiving = !isReceiving;
		}
		if(data.hasKey("priority")){
			ConnectionPriority[] vals = ConnectionPriority.values();
			priority = vals[(priority.ordinal()+1)%vals.length];
		}
	}
}
