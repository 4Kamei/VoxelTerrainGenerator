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
    private final float speed = 0.1f;
    private boolean up, down, left, right;
    private float azimuth, pitch;
    private float dmouseX, lmouseX;
    private float dmouseY, lmouseY;
    private boolean active;
    private boolean activeUpdated;

    public Player(Vector3f playerPosition, float azimuth, float pitch) {
        this.position = playerPosition;
        this.pitch = pitch;
        this.azimuth = azimuth;
        caluclateLookVector();
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
        if (key == GLFW.GLFW_KEY_ENTER){
            active = (action != 0 ? !active : active);
            activeUpdated = true;
        }
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
            Logger.log(Logger.LogLevel.DEBUG, String.format("Azimuth: %.2f \n Pitch: %.2f", azimuth/Math.PI, pitch/Math.PI));
        }
    }

    public Vector3f getLookVector() {
        return lookVector;
    }

    public void updateMouse(float mouseX, float mouseY){
        dmouseX = mouseX - lmouseX;
        lmouseX = mouseX;
        dmouseY = mouseY - lmouseY;
        lmouseY = mouseY;
        if(active){
            float dAz = (float) (dmouseX * Math.PI * 2);
            float dPi = (float) (dmouseY * Math.PI * 2);
            addLook(dAz, dPi);
        }
    }

    private void addLook(float azimuth, float pitch){
        this.azimuth += azimuth;
        this.pitch += pitch;
        caluclateLookVector();
    }
    private void setLook(double azimuth, double pitch){
        this.azimuth = (float) ((azimuth + Math.PI * 2) % (Math.PI * 2));
        this.pitch = (float) (((pitch) + Math.PI/2 +  Math.PI*2) % (Math.PI * 2));
        caluclateLookVector();
    }

    private void caluclateLookVector() {
        float sinPitch = (float) Math.sin(-pitch);
        lookVector = new Vector3f((float) (-sinPitch*Math.sin(azimuth)), (float) Math.cos(pitch), (float) (sinPitch*Math.cos(azimuth)));

    }

    public Vector3i getAxisVector(){
        return new Vector3i(calNorm(lookVector.x), calNorm(lookVector.y), calNorm(lookVector.z));
    }

    private int calNorm(double number){
        return (int) (number/Math.abs(number));
    }

    public Vector3f getTransform() {
        return new Vector3f(position).mul(-1);
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return (float) (pitch - Math.PI/2);
    }

    public boolean isActiveUpdated() {
        if (activeUpdated) {
            activeUpdated = false;
            return true;
        }
        return false;
    }

    public boolean lookActive() {
        activeUpdated = true;
        return active;
    }
}