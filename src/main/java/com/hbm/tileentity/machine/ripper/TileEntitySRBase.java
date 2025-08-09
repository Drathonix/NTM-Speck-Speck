package com.hbm.tileentity.machine.ripper;

import api.hbm.block.IHasPocketDimension;
import com.hbm.calc.Location;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntitySRBase extends TileEntityMachineBase implements IHasPocketDimension {
	public @Nullable PocketDimension linkedPocketDimension = null;
	public @Nullable Location riftPosition = null;

	public TileEntitySRBase(int slotCount) {
		super(slotCount);
	}

	@Override
	public void validate() {
		super.validate();
		relink();
	}

	public void relink() {
		Tuple.Pair<PocketDimension, Location> l = PocketDimension.getNearestPocketDimensionFromAllNets(worldObj, xCoord, yCoord, zCoord, 128);
		if (l != null) {
			linkToPocketDimension(l.key,l.value);
		}
	}

	@Override
	public void linkToPocketDimension(@Nonnull PocketDimension dimension, @Nonnull Location rift) {
		this.linkedPocketDimension=dimension;
		this.riftPosition=rift;
	}

	@Nullable
	@Override
	public PocketDimension getPocketDimension() {
		return linkedPocketDimension;
	}

	@Nullable
	@Override
	public Location getRift() {
		return riftPosition;
	}
}
