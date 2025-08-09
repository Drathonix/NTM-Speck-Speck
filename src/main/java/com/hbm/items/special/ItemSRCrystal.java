package com.hbm.items.special;

import net.minecraft.item.Item;

public class ItemSRCrystal extends Item {
	public final int tier;
	public final long powerPerTick;
	public final long powerAboveInstability;
	public final boolean isAuxillary;

	public ItemSRCrystal(int tier, long powerPerTick, long powerAboveInstability, boolean isAuxillary) {
		this.tier = tier;
		this.powerPerTick = powerPerTick;
		this.powerAboveInstability = powerAboveInstability;
		this.isAuxillary = isAuxillary;
	}
}
