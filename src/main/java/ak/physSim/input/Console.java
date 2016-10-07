package ak.physSim.input;

import ak.physSim.main.WorldManager;
import ak.physSim.render.Renderable;
import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Created by Aleksander on 30/08/2016.
 */
public class Console {

    private boolean visible;

    private String command = "";

    private int maxLength = 30;

    private WorldManager manager;

    public void setManager(WorldManager manager) {
        this.manager = manager;
    }

    public void writeText(String text) {
        String[] texts = text.split("\n");
        for (String s : texts) {
            //
        }
    }

    public void parse(int scancode, int key, boolean shift){
        String s = GLFW.glfwGetKeyName(key, scancode);
        System.out.println("{" + s + "}");
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
        if (key == GLFW.GLFW_KEY_SPACE)
            command += " ";
        System.out.println(command);
    }

    public void sendCommand() {
        if (command == null)
            return;
        Logger.log(Logger.LogLevel.ALL, command);
        command = "";
    }


    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }


}
