package ak.physSim;

import org.joml.Vector3f;

/**
 * Created by Aleksander on 16/07/2016.
 */
public class Light {
    private Vector3f position;

    public Light(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition(){
        return position;
    }
}
