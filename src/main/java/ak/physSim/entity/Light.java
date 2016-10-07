package ak.physSim.entity;

import org.joml.Vector3f;

/**
 * Created by 10akaminski on 28/09/2016.
 */
public class Light extends Camera {

    private float power;

    public Light(float power, Vector3f position, float pitch, float azimuth) {
        super(position, pitch, azimuth);
        this.power = power;
        caluclateLookVector();
    }

    public float getLightPower() {
        return power;
    }

    @Override
    public String toString() {
        return power + "{A=" + azimuth/Math.PI + ", P=" + pitch/Math.PI + "}";
    }

    public void addPower(double v) {
        power += v;
    }
}
