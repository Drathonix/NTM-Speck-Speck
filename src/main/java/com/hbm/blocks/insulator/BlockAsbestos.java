package com.hbm.blocks.insulator;

import api.hbm.block.IInsulator;
import com.hbm.blocks.generic.BlockOutgas;
import com.hbm.blocks.generic.BlockRotatablePillar;
import net.minecraft.block.material.Material;

public class BlockAsbestos extends BlockOutgas implements IInsulator {
	private final float efficiency;

	public BlockAsbestos(Material mat, boolean randomTick, int rate, boolean onBreak, float efficiency) {
		super(mat, randomTick, rate, onBreak);
		this.efficiency=efficiency;
	}


	@Override
	public float radiativeCoolingLossPrevention() {
		return efficiency;
	}
}
