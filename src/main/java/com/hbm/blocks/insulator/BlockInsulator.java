package com.hbm.blocks.insulator;

import api.hbm.block.IInsulator;
import com.hbm.blocks.generic.BlockRotatablePillar;
import net.minecraft.block.material.Material;

public class BlockInsulator extends BlockRotatablePillar implements IInsulator {
	private final float efficiency;


	public BlockInsulator(Material mat, String top, float efficiency) {
		super(mat, top);
		this.efficiency = efficiency;
	}

	@Override
	public float radiativeCoolingLossPrevention() {
		return efficiency;
	}
}
