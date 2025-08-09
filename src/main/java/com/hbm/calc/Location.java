package com.hbm.calc;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Objects;

public class Location extends EasyLocation {

	public World world;

	public Location(World world, double x, double y, double z) {
		super(x,y,z);
		this.world = world;
	}

	public Location add(int xa, int ya, int za) {
		return new Location(world, x + xa, y + ya, z + za);
	}

	public Location add(ForgeDirection dir) {
		return add(dir.offsetX, dir.offsetY, dir.offsetZ);
	}

	public TileEntity getTileEntity() {
		return world.getTileEntity((int)x, (int)y, (int)z);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		Location location = (Location) object;
		return Objects.equals(world, location.world);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), world);
	}
}
