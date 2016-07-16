package ak.physSim.render;

import ak.physSim.util.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Transformation {

    private final Vector3f position;

    private final Vector3f rotation;

    private final Matrix4f transltionMatrix = new Matrix4f();

    private final float scale;

    public Transformation(Vector3f position, Vector3f rotation, float scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        transltionMatrix.identity()
                .translate(position)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .scale(scale);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public Matrix4f getTranslationMatrix() {
        return transltionMatrix;
    }
}
