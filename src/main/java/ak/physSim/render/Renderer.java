package ak.physSim.render;

import ak.physSim.chunk.Chunk;
import ak.physSim.input.Console;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Renderer {

    int frameCount;
    private long totalTime;

    //Shader for rendering the scene
    private ShaderProgram program;
    //All of the scene objects. Subject to change.
    private ArrayList<Renderable> renderables;
    //Scene projection matrix
    private Matrix4f projectionMatrix;

    //Shader for rendering the console/ui
    private ShaderProgram orthRender;
    //Orthogonal projection matrix. For UI
    private Matrix4f orthMatrix;
    //Console object pointer.
    private Console console;

    public Renderer(ShaderProgram defaultProgram, ShaderProgram orthRender, Matrix4f projectionMatrix, Matrix4f orthMatrix) throws Exception {
        this.program = defaultProgram;
        this.orthRender = orthRender;
        this.projectionMatrix = projectionMatrix;
        renderables = new ArrayList<>();
        this.orthMatrix = orthMatrix;
    }

    public void setConsole(Console c) {
        this.console = c;
    }

    public void render(Matrix4f viewMatrix) throws Exception {
        long startTime = System.nanoTime();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);


        //Resize window handling

        program.bind();
        program.setUniform("view", viewMatrix);
        program.setUniform("projection", projectionMatrix);

        FrustumIntersection intersection = new FrustumIntersection(new Matrix4f(projectionMatrix).mul(viewMatrix));
        renderables.stream().filter(renderable -> renderable instanceof Chunk).forEach(renderable -> {
            Chunk chunk = (Chunk) renderable;
            Vector3i pos = chunk.getPosition();
            if (intersection.testAab(pos.x, pos.y, pos.z, pos.x + 16, pos.y + 16, pos.z + 16)) {
                program.setUniform("model", renderable.getTransformation().getTranslationMatrix());
                chunk.bindLighting(program);
                chunk.render();
            }
        });

        program.unbind();

        renderables.clear();

        totalTime += System.nanoTime() - startTime;
        frameCount++;
        if (frameCount == 59) {
            frameCount = 0;
            Logger.log(Logger.LogLevel.DEBUG, String.valueOf(((totalTime / 60) / 1000000f)));
            totalTime = 0;
        }

        if (orthRender != null) {
            orthRender.bind();
            orthRender.setUniform("projection", orthMatrix);
            if (console != null && console.isVisible()) {
                renderConsole(console);
            }
            orthRender.unbind();
        }
    }

    private void renderConsole(Console console) {
        int yPos = 0;
        for (String s : console.getText()) {
            drawText(s, 0, yPos);
            yPos += 20;
        }
    }

    private void drawText(String text, int x, int y) {
        Text.drawString(text, x, y, 1, 1);
    }

    public void addRenderables(ArrayList<Renderable> renderable){
        renderables.addAll(renderable);
    }
}
