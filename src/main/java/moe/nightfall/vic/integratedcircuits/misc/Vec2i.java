package moe.nightfall.vic.integratedcircuits.misc;

import net.minecraft.util.EnumFacing;

import java.util.Objects;

/** An int value pair **/
public class Vec2i {

	public static final Vec2i zero = new Vec2i(0, 0);

	public final int x, y;

	public Vec2i(int a, int b) {
		this.x = a;
		this.y = b;
	}

	public Vec2i offset(EnumFacing dir) {
		return new Vec2i(x + dir.getFrontOffsetX(), y + dir.getFrontOffsetZ());
	}

	public double distanceTo(Vec2i other) {
		return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vec2i other = (Vec2i) obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString() {
		return "Vec2i[" + x + ", " + y + "]";
	}
}
