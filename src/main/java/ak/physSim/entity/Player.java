package ak.physSim.entity;

import ak.physSim.util.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class Player {
    private Vector3f lookVector;
    private Vector3f position;
    private final float speed = 0.01f;
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
        if (key == GLFW.GLFW_KEY_DOWN|| key == GLFW.GLFW_KEY_S)
            down = (action != 0);
        if (key == GLFW.GLFW_KEY_LEFT|| key == GLFW.GLFW_KEY_A)
            left = (action != 0);
        if (key == GLFW.GLFW_KEY_RIGHT|| key == GLFW.GLFW_KEY_D)
            right = (action != 0);
    }

    public void update(int delta) {
        float update = delta*speed;
        if (up) {
            position = position.add(lookVector.x * update, lookVector.y * update, lookVector.z * update);
        }
        if (down) {
            position = position.sub(lookVector.x * update, lookVector.y * update, lookVector.z * update);
        }
        if (right)
            position.zero();
        if (left){
            Logger.log(Logger.LogLevel.DEBUG, String.format("POS: %.3f, %.3f, %.3f", position.x, position.y, position.z));
            Logger.log(Logger.LogLevel.DEBUG, String.format("NORM POS %.3f, %.3f, %.3f",position.x/position.length(), position.y/position.length(), position.z/position.length()));
            Logger.log(Logger.LogLevel.DEBUG, String.format("EYE: %.3f, %.3f, %.3f", lookVector.x, lookVector.y, lookVector.z));
        }
    }

    public Vector3f getLookVector() {
        return lookVector;
    }

        public void setLook(float azimuth, float pitch){
            this.azimuth = (float) ((azimuth + Math.PI * 2) % (Math.PI * 2));
            this.pitch = (float) (((pitch) + Math.PI/2 +  Math.PI*2) % (Math.PI * 2));
            caluclateLookVector();
        }

    private void caluclateLookVector() {
        float sinPitch = (float) Math.sin(-pitch);
        /*
        Matrix4f matrix4f = new Matrix4f().identity();
        matrix4f.rotateX(pitch);
        Logger.log(Logger.LogLevel.DEBUG, matrix4f.toString());
        Logger.log(Logger.LogLevel.DEBUG, matrix4f.m22 + "");
        Vector3f lookVector = new Vector3f(matrix4f.m20, matrix4f.m21, matrix4f.m22);*/
        lookVector = new Vector3f((float) (-sinPitch*Math.sin(azimuth)), (float) Math.cos(pitch), (float) (sinPitch*Math.cos(azimuth)));
        Logger.log(Logger.LogLevel.DEBUG, String.format("EYE: %.3f, %.3f, %.3f", lookVector.x, lookVector.y, lookVector.z));
        Logger.log(Logger.LogLevel.DEBUG, String.format("AXS: %d, %d, %d", getAxisVector().x, getAxisVector().y, getAxisVector().z));

    }

    public Vector3i getAxisVector(){
        return new Vector3i(calNorm(lookVector.x), calNorm(lookVector.y), calNorm(lookVector.z));
    }

    private int calNorm(double number){
        return (int) (number/Math.abs(number));
    }

    public Vector3f getTransform() {
        return position;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return (float) (pitch - Math.PI/2);
    }
}
