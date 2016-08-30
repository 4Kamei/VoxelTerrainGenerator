package ak.physSim.input;

import ak.physSim.entity.Player;
import ak.physSim.util.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashMap;

/**
 * Created by Aleksander on 22/08/2016.
 */
public class KeyManager extends GLFWKeyCallback {

    private HashMap<GameAction, KeyAction> actions;
    private KeyBindingManager bindings;
    public KeyManager() {
        super();
        actions = new HashMap<>();
        bindings = new KeyBindingManager(true);
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {

        doAction(scancode, action);
        System.out.println(action + " : " + mods);
    }

    public void registerAction(GameAction trigger, KeyAction action) {
        if (actions.containsKey(trigger))
            actions.remove(trigger);
        actions.put(trigger, action);
    }

    public void doAction(int scanCode, int action) {
        try {
            actions.get(bindings.getKey(scanCode)).doAction(action != 0);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "Key " + scanCode + " not registered!");
        }
    }

    public void registerPlayerActions(Player player) {
        registerAction(GameAction.PLAYER_DOWN, (keyPressed) -> player.setKeysMovement(GameAction.PLAYER_DOWN, keyPressed));
        registerAction(GameAction.PLAYER_UP, (keyPressed) -> player.setKeysMovement(GameAction.PLAYER_UP, keyPressed));
        registerAction(GameAction.PLAYER_LEFT, (keyPressed) -> player.setKeysMovement(GameAction.PLAYER_LEFT, keyPressed));
        registerAction(GameAction.PLAYER_RIGHT, (keyPressed) -> player.setKeysMovement(GameAction.PLAYER_RIGHT, keyPressed));
        registerAction(GameAction.PLAYER_ENTER, (keyPressed) -> player.setKeysOther(GameAction.PLAYER_ENTER, keyPressed));
    }
}
