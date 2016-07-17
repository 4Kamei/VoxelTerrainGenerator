package ak.physSim.render.light;

import org.joml.Vector3f;

/**
 * Created by Aleksander on 16/07/2016.
 */
public class AreaLight {
    private Vector3f position;

    public AreaLight(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition(){
        return position;
    }
}
