package com.hbm.tileentity.machine.ripper;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.Nodespace;
import com.hbm.blocks.machine.MachineBattery;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRipperEnergyTransceiver extends TileEntityMachineBase implements IEnergyReceiverMK2, IEnergyProviderMK2 {
	public static final long maxPower = 100_000_000;
	public boolean isSending;
	public PocketDimension linkedPocketDimension = null;
	public ConnectionPriority priority = ConnectionPriority.LOW;

	public TileEntityRipperEnergyTransceiver() {
		super(2);
	}

	@Override
	public long getPower() {
		return isSending ? linkedPocketDimension.getPowerLong() : 0;
	}

	@Override
	public void usePower(long power) {
		linkedPocketDimension.removeEnergy(power);
	}

	@Override
	public void setPower(long power) {

	}

	@Override
	public long transferPower(long power) {
		linkedPocketDimension.insertEnergy(power);
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
		if(!worldObj.isRemote) {
			int mode = this.getRelevantMode(false);

			long prevPower = this.power;

			power = Library.chargeItemsFromTE(slots, 1, power, getMaxPower());

			// In buffer mode, becomes a cable block and provides power to itself
			// otherwise, acts like a regular power providing/accepting machine
			if(mode == mode_buffer) {
				if(UniNodespace.isUnstable(this.node)) {
					this.node = (Nodespace.PowerNode) UniNodespace.getOrCreateNode(worldObj, xCoord, yCoord, zCoord, Nodespace.THE_POWER_PROVIDER,this::createNode);
				}

				this.tryProvide(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN);
				if(node != null && node.hasValidNet()) node.net.addReceiver(this);
			} else {
				if(this.node != null) {
					UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, Nodespace.THE_POWER_PROVIDER);
					this.node = null;
				}

				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					Nodespace.PowerNode dirNode = (Nodespace.PowerNode) UniNodespace.getNode(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, Nodespace.THE_POWER_PROVIDER);

					if(mode == mode_output) {
						tryProvide(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
					} else {
						if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
					}

					if(mode == mode_input) {
						if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
					} else {
						if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
					}
				}
			}

			byte comp = this.getComparatorPower();
			if(comp != this.lastRedstone)
				this.markDirty();
			this.lastRedstone = comp;

			power = Library.chargeTEFromItems(slots, 0, power, getMaxPower());

			long avg = (power + prevPower) / 2;
			this.delta = avg - this.log[0];

			for(int i = 1; i < this.log.length; i++) {
				this.log[i - 1] = this.log[i];
			}

			this.log[19] = avg;

			prevPowerState = power;

			this.networkPackNT(20);
		}
	}
}
