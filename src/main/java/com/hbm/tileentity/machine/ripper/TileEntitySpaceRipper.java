package com.hbm.tileentity.machine.ripper;

import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.TileEntityMachineBase;

public class TileEntitySpaceRipper extends TileEntitySRBase {
	public static final long MAX_POWER = 100_000_000;
	public long power = 0;

	public TileEntitySpaceRipper() {
		super(2);
	}

	@Override
	public String getName() {
		return "container.spaceRipper";
	}

	@Override
	public void updateEntity() {

	}
}
