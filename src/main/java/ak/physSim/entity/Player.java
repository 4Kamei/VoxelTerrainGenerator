package ak.physSim.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class Player {
    private Vector3f lookVector;
    private Vector3f position;
    private final float speed = 0.1f;
    private boolean up, down, left, right;

    public Player(Vector3f playerPosition, Vector3f playerLookVector) {
        this.lookVector = playerLookVector;
        this.position = playerPosition;
    }

    public void setKeys(int key, int action){
        up = down = left = right = false;
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_W)
            up = (action != 0);
        if (key == GLFW.GLFW_KEY_DOWN|| key == GLFW.GLFW_KEY_S);
            down = (action != 0);
        if (key == GLFW.GLFW_KEY_LEFT|| key == GLFW.GLFW_KEY_A);
            left = (action != 0);
        if (key == GLFW.GLFW_KEY_RIGHT|| key == GLFW.GLFW_KEY_D);
            right = (action != 0);
    }

    public void update(int delta){
        if (up){
            position.add(
                    (float) Math.sin(lookVector.y) * delta * speed,
                    0,
                    (float) -Math.cos(lookVector.y) * delta * speed);
        }
    }

    public Vector3f getLookVector() {
        return lookVector;
    }

    public void setLookVector(double rotX, double rotY, double rotZ){
        lookVector.set((float) rotX, (float) rotY,(float)  rotZ);
    }

    public Vector3f getPosition() {
        return position;
    }
}
