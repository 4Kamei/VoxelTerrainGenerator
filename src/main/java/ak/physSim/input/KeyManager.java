package ak.physSim.input;

import ak.physSim.util.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashMap;

/**
 * Created by Aleksander on 22/08/2016.
 */
public class KeyManager extends GLFWKeyCallback {

    private HashMap<GameAction, Runnable> actions;

    public KeyManager() {
        super();
        actions = new HashMap<>();
    }

    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS)
            switch (key) {
                case GLFW.GLFW_KEY_ESCAPE : doAction(GameAction.GLFW_EXIT);
                    break;
                case GLFW.GLFW_KEY_UP : doAction(GameAction.PLAYER_UP_PRESS);
                    break;
                case GLFW.GLFW_KEY_DOWN : doAction(GameAction.PLAYER_DOWN_PRESS);
                    break;
                case GLFW.GLFW_KEY_ENTER : doAction(GameAction.PLAYER_ENTER);
                    break;
            }
        if (action == GLFW.GLFW_RELEASE)
            switch (key) {
                case GLFW.GLFW_KEY_DOWN : doAction(GameAction.PLAYER_DOWN_RELEASE);
                    break;
                case GLFW.GLFW_KEY_UP : doAction(GameAction.PLAYER_UP_RELEASE);
                    break;
            }

        Logger.log(Logger.LogLevel.DEBUG, "window = " + window +
                "\nscancode = " + scancode +
                "\naction = " + action +
                "\nmods = " + mods);
    }

    public void registerAction(GameAction trigger, Runnable action) {
        if (actions.containsKey(trigger))
            actions.remove(trigger);
        actions.put(trigger, action);
    }
    
    private void doAction(GameAction action) {
        actions.get(action).run();
    }
}
