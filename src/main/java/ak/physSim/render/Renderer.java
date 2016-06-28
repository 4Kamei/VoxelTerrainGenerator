package ak.physSim.render;

import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Renderer {
    private ShaderProgram program;
    private ArrayList<Renderable> renderables;

    public Renderer(ShaderProgram program) {
        this.program = program;
        renderables = new ArrayList<>();
    }

    public void render(Matrix4f projectionMatrix) throws Exception {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        //Resize window handling

        program.bind();

        program.setUniform("projectionMatrix", projectionMatrix);

        for (Renderable renderable : renderables) {
            program.setUniform("worldMatrix", renderable.getTransformation().getTranslationMatrix());
            renderable.render();
        }

        program.unbind();

        renderables.clear();
    }

    public void addRenderables(ArrayList<Renderable> renderable){
        renderables.addAll(renderable);
    }
}
