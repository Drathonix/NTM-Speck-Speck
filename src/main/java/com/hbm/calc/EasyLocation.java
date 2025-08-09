package com.hbm.calc;

import java.util.Objects;

public class EasyLocation {

	public double x;
	public double y;
	public double z;

	public EasyLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double distSq(Location l) {
		double x = l.x-this.x;
		double y = l.y-this.y;
		double z = l.z-this.z;
		return x*x + y*y + z*z;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		EasyLocation that = (EasyLocation) object;
		return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
}
