package ak.physSim.input;

import ak.physSim.render.Renderable;
import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Aleksander on 30/08/2016.
 */
public class Console {

    private boolean visible;

    private String command;

    private int maxLength = 30;

    String[] text;

    public void writeText(String text) {
        String[] texts = text.split("\n");
        for (String s : texts) {
            putText(s);
        }
    }

    private void putText(String s) {
        System.arraycopy(text, 0, text, 1, text.length - 1);
        text[0] = s;
    }

    public void parse(int scancode, int key, boolean shift){
        String s = GLFW.glfwGetKeyName(key, scancode);
        if (s != null && s.length() == 1) {
            if (shift)
                s = s.toUpperCase();
            else
                s = s.toLowerCase();
            char c = s.charAt(0);
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                command += c;
            }
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (command.length() > 0) {
                command = command.substring(0, command.length() - 1);
            }
        }
    }

    public void sendCommand() {
        System.out.println();
        if (command == null)
            return;
        Logger.log(Logger.LogLevel.ALL, command);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }


    public String[] getText() {
        return text;
    }
}
