package ak.physSim.entity;

import ak.physSim.input.GameAction;
import ak.physSim.util.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class Player extends Camera {

    private final float speed = 100f;
    private boolean up, down, left, right;
    private float dmouseX, lmouseX;
    private float dmouseY, lmouseY;
    private boolean active;
    private boolean activeUpdated;

    public Player(Vector3f playerPosition, float azimuth, float pitch) {
        super(playerPosition, pitch, azimuth);
        caluclateLookVector();
    }

    public void setKeysMovement(GameAction action, boolean statePressed) {
        if (action == GameAction.PLAYER_LEFT) {
            left = statePressed;
        }
        if (action == GameAction.PLAYER_RIGHT) {
            right = statePressed;
        }
        if (action == GameAction.PLAYER_UP) {
            up = statePressed;
        }
        if (action == GameAction.PLAYER_DOWN) {
            down = statePressed;
        }
    }

    public void setKeysOther(GameAction action, boolean statePressed) {
        switch(action) {
            case PLAYER_ENTER : active = (statePressed ? !active : active);
                break;
        }
    }

    public void update(float delta) {
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


    private void setLook(double azimuth, double pitch){
        this.azimuth = (float) ((azimuth + Math.PI * 2) % (Math.PI * 2));
        this.pitch = (float) (((pitch) + Math.PI/2 +  Math.PI*2) % (Math.PI * 2));
        caluclateLookVector();
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