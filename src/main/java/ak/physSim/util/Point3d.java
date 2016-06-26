package ak.physSim.util;

/**
 * Created by Aleksander on 25/06/2016.
 */
public class Point3d {
    private int x, y, z;

    public Point3d(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point3d)) return false;

        Point3d point3d = (Point3d) o;

        if (x != point3d.x) return false;
        if (y != point3d.y) return false;
        if (z != point3d.z) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ((x * 73856093) ^ (y * 83492879) ^ (z * 10002563));
    }
}
