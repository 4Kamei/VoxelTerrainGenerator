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

    private boolean consoleOpen;

    private Console console;

    private HashMap<GameAction, KeyAction> action;

    private KeyBindingManager defaultBindings;
    private KeyBindingManager consoleBindings;

    public KeyManager(Console console) {
        super();
        this.console = console;
        action = new HashMap<>();
        defaultBindings = new KeyBindingManager(true);
        consoleBindings = new KeyBindingManager(false);
        consoleBindings.bind(28, GameAction.CONSOLE_CLOSE);
        defaultBindings.bind(20, GameAction.OPEN_CONSOLE);
        registerAction(GameAction.OPEN_CONSOLE, up -> {
            consoleOpen = true;
        } );
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (consoleOpen && action == 1) {
            if (consoleBindings.getKey(scancode) == GameAction.CONSOLE_CLOSE) {
                console.sendCommand();
                consoleOpen = false;
                return;
            }
            console.parse(scancode, key, mods == 1);
            return;
        } else if (!consoleOpen) {
            doAction(scancode, action);
        }
        Logger.log(Logger.LogLevel.DEBUG, scancode + " : " + action + " : " + mods);
    }

    public void registerAction(GameAction trigger, KeyAction action) {
        if (this.action.containsKey(trigger))
            this.action.remove(trigger);
        this.action.put(trigger, action);
    }

    public void doAction(int scanCode, int action) {
        try {
            this.action.get(defaultBindings.getKey(scanCode)).doAction(action != 0);
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
