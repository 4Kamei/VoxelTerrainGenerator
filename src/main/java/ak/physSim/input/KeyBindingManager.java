package ak.physSim.input;

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
        keyMap.put(17, GameAction.PLAYER_UP);
        keyMap.put(31, GameAction.PLAYER_DOWN);
        keyMap.put(30, GameAction.PLAYER_LEFT);
        keyMap.put(28, GameAction.PLAYER_ENTER);
        keyMap.put(1, GameAction.GLFW_EXIT);
    }
}
