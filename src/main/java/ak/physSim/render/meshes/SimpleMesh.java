package ak.physSim.render.meshes;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import javax.swing.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * Created by Aleksander on 31/08/2016.
 */
public class SimpleMesh implements Mesh{

    private int vboIndicesId;
    private int vertCount;
    private int vaoID;
    private int vboVertexId;

    public SimpleMesh(FloatBuffer vertices, IntBuffer indices, int indicesCount) {
        createMesh(vertices, indices, indicesCount);
    }
    public SimpleMesh(float[] vertices, int[] indices) {
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices);
        verticesBuffer.flip();

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices);
        indicesBuffer.flip();
        createMesh(verticesBuffer, indicesBuffer, indices.length);
    }

    public void createMesh(FloatBuffer vertices, IntBuffer indices, int vertCount) {

        this.vertCount = vertCount;

        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        vboIndicesId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);

        vboVertexId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 1, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);
    }


    @Override
    public void cleanup() {
        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboVertexId);
        glDeleteBuffers(vboIndicesId);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoID);
    }

    @Override
    public int getVaoID() {
        return 0;
    }

    @Override
    public int getVertexCount() {
        return vertCount;
    }
}
