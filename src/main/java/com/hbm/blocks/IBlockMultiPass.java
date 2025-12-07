package com.hbm.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockMultiPass {

	int getPasses();

	int renderID = RenderingRegistry.getNextAvailableRenderId();
	static int getRenderType() {
		return renderID;
	}

	default boolean shouldRenderItemMulti() {
		return false;
	}
}
