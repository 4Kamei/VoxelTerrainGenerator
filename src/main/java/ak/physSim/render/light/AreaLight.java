package ak.physSim.render.light;

import org.joml.Vector3f;

/**
 * Created by Aleksander on 16/07/2016.
 */
public class AreaLight {
    private Vector3f position;
    private float power;
    public AreaLight(Vector3f position) {
        this.position = position;
        this.power = 20;
    }

    public Vector3f getPosition(){
        return position;
    }

    public float power() {
        return power;
    }

    public void addPower(double dir) {
        this.power += dir;
    }
}
