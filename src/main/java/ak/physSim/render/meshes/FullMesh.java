package ak.physSim.render.meshes;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * Created by Aleksander on 22/06/2016.
 */
public class FullMesh implements Mesh{

    private int vaoId;
    private int vboVertexId;
    private int vboIndicesId;
    private int vboColourId;
    private int vertCount;
    private int vboNormalsId;

    public FullMesh(FloatBuffer floatBuffer, FloatBuffer colourBuffer, IntBuffer intBuffer, int vertCount){
        createMesh(floatBuffer, colourBuffer, intBuffer, null, vertCount);
    }

    public FullMesh(float[] vertices, float[] colours, int[] indices, float[] normals) {
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices);
        verticesBuffer.flip();

        FloatBuffer colourBuffer = BufferUtils.createFloatBuffer(colours.length);
        colourBuffer.put(colours);
        colourBuffer.flip();

        FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
        normalsBuffer.put(normals);
        normalsBuffer.flip();

        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        createMesh(verticesBuffer, colourBuffer, indicesBuffer, normalsBuffer, indices.length);

    }

    private void createMesh(FloatBuffer vericesBuffer,
                            FloatBuffer colourBuffer,
                            IntBuffer intBuffer,
                            FloatBuffer normalsBuffer,
                            int vertCount){

        this.vertCount = vertCount;

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        vboIndicesId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndicesId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL_DYNAMIC_DRAW);

        vboNormalsId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalsId);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

        vboColourId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboColourId);
        glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        vboVertexId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexId);
        glBufferData(GL_ARRAY_BUFFER, vericesBuffer, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);

    }

    @Override
    public int getVaoID() {
        return vaoId;
    }

    @Override
    public int getVertexCount() {
        return vertCount;
    }

    public int getVertCount() {
        return vertCount;
    }

    public int getVaoId() {
        return vaoId;
    }

    public void cleanup(){

        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboVertexId);
        glDeleteBuffers(vboIndicesId);
        glDeleteBuffers(vboColourId);
        glDeleteBuffers(vboNormalsId);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
