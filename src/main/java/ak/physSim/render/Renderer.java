package ak.physSim.render;

import ak.physSim.chunk.Chunk;
import ak.physSim.util.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Renderer {
    private ShaderProgram program;
    private ArrayList<RenderableBase> renderableBases;

    public Renderer(ShaderProgram program) {
        this.program = program;
        renderableBases = new ArrayList<>();
    }

    public void render(Matrix4f projectionMatrix, Vector3i cameraAxisVector) throws Exception {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        //Resize window handling

        program.bind();

        program.setUniform("projectionMatrix", projectionMatrix);

        for (RenderableBase renderableBase : renderableBases) {
            program.setUniform("worldMatrix", renderableBase.getTransformation().getTranslationMatrix());
            if (renderableBase instanceof Chunk)
                ((Chunk) renderableBase).render(cameraAxisVector);
        }

        program.unbind();

        renderableBases.clear();
    }

    public void addRenderables(ArrayList<RenderableBase> renderableBase){
        renderableBases.addAll(renderableBase);
    }
}
