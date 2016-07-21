package ak.physSim.render;

import ak.physSim.chunk.Chunk;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Renderer {
    private ShaderProgram program;
    private ArrayList<Renderable> renderables;
    private long startTime;
    private long totalTime;
    private Matrix4f projectionMatrix;
    int frameCount;
    private Matrix4f frustumMatrix;

    public Renderer(ShaderProgram program, Matrix4f projectionMatrix) throws Exception {
        this.program = program;
        this.projectionMatrix = projectionMatrix;
        renderables = new ArrayList<>();

    }

    public void render(Matrix4f viewMatrix) throws Exception {
        startTime = System.nanoTime();
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
    }

    public void addRenderables(ArrayList<Renderable> renderable){
        renderables.addAll(renderable);
    }
}
