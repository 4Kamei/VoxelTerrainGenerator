package ak.physSim.entity;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class Player {
    private Vector3f lookVector;
    private Vector3f position;
    private final float speed = 0.1f;
    private boolean up, down, left, right;
    private float azimuth, pitch;

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
                    (float) -Math.sin(lookVector.y) * delta * speed,
                    0,
                    (float) Math.cos(lookVector.y) * delta * speed);
        }
    }

    public Vector3f getLookVector() {
        return lookVector;
    }

    public void setLook(float azimuth, float pitch){
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.lookVector = caluclateLookVector();
    }

    public Vector3f caluclateLookVector() {
        float sinPitch = (float) Math.sin(pitch);
        return new Vector3f((float) (sinPitch*Math.cos(azimuth)), (float) Math.cos(pitch), (float) -(sinPitch*Math.sin(pitch)));
    }

    public Vector3i getAxisVector(){
        return new Vector3i(calNorm(lookVector.x), calNorm(lookVector.y), calNorm(lookVector.z));
    }

    private int calNorm(double number){
        return (int) (number/Math.abs(number));
    }

    public Vector3f getTransform() {
        return position.negate();
    }
}
