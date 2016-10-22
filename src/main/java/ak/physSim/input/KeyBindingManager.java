package ak.physSim.input;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

/**
 * Created by Aleksander on 23/08/2016.
 */
public class KeyBindingManager {
    private HashMap<Integer, GameAction> keyMap;

    public KeyBindingManager(boolean defaluts) {
        keyMap = new HashMap<>();
        if (defaluts)
            setDefaults();
    }

    public GameAction getKey(int scancode) {
        if (keyMap.containsKey(scancode))
            return keyMap.get(scancode);
        return null;
    }

    private void setDefaults() {
        //Bindings need a keyDown and a keyUp mapping.
        keyMap.put(GLFW.GLFW_KEY_W, GameAction.PLAYER_UP);
        keyMap.put(GLFW.GLFW_KEY_S, GameAction.PLAYER_DOWN);
        keyMap.put(GLFW.GLFW_KEY_A, GameAction.PLAYER_LEFT);
        keyMap.put(GLFW.GLFW_KEY_D, GameAction.PLAYER_RIGHT);
        keyMap.put(GLFW.GLFW_KEY_ENTER, GameAction.PLAYER_ENTER);
        keyMap.put(GLFW.GLFW_KEY_ESCAPE, GameAction.GLFW_EXIT);
        keyMap.put(GLFW.GLFW_KEY_T, GameAction.OPEN_CONSOLE);
        keyMap.put(GLFW.GLFW_KEY_Q, GameAction.SET_LIGHT);
        keyMap.put(GLFW.GLFW_KEY_E, GameAction.PLAYER_FLY);
    }

    public void bind(int scancode, GameAction action) {
        keyMap.put(scancode, action);
    }
}
