package ak.physSim.entity;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Created by 10akaminski on 28/09/2016.
 */
public class Camera {
    protected Vector3f lookVector;
    protected Vector3f position;
    protected float azimuth;
    protected float pitch;

    public Camera(Vector3f position, float pitch, float azimuth) {
        this.position = position;
        this.pitch = pitch;
        this.azimuth = azimuth;
    }

    public Vector3f getLookVector() {
        return lookVector;
    }

    public Vector3i getAxisVector(){
        return new Vector3i(calNorm(lookVector.x), calNorm(lookVector.y), calNorm(lookVector.z));
    }

    public void setLook(float pitch, float azimuth) {
        this.pitch = pitch;
        this.azimuth = azimuth;
        caluclateLookVector();
    }

    public void addLook(float azimuth, float pitch){
        this.pitch += pitch;
        if (this.pitch + pitch > Math.PI) {
            this.pitch = (float) Math.PI;
        } if (this.pitch + pitch < 0){
            this.pitch = 0;
        }
        this.azimuth += azimuth;
        caluclateLookVector();
    }

    protected void caluclateLookVector() {
        float sinPitch = (float) Math.sin(-pitch);
        lookVector = new Vector3f((float) (-sinPitch*Math.sin(azimuth)), (float) Math.cos(pitch), (float) (sinPitch*Math.cos(azimuth)));
    }

    private int calNorm(double number){
        return (int) (number/Math.abs(number));
    }

    public Vector3f getTransform() {
        return new Vector3f(position).mul(-1);
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return (float) (pitch - Math.PI/2);
    }

    public void setPosition(int x, int y, int z) {
        this.position = new Vector3f(x, y, z);
    }
}
