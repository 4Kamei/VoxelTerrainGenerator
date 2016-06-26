package ak.physSim.chunk;

import ak.physSim.render.Mesh;
import ak.physSim.render.Renderable;
import ak.physSim.render.Transformation;
import ak.physSim.util.Reference;
import ak.physSim.voxel.Voxel;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;
import java.util.HashSet;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Chunk extends Renderable {

    //Chunk size in blocks;

    private Voxel[][][] voxels = new Voxel[Reference.CHUNK_SIZE][Reference.CHUNK_SIZE][Reference.CHUNK_SIZE];

    //Stores if mesh needs to be recreated

    //Position as vector, used for transform
    private Vector3f position;

    public Chunk(int x, int y, int z) {
        this.position = new Vector3f(x, y, z).mul(16);
    }

    public Voxel[][][] getVoxels() {
        return voxels;
    }

    public Voxel getVoxel(int x, int y, int z) {
        return voxels[x][y][z];
    }

    public void setVoxel(int x, int y, int z, Voxel voxel) {
        voxels[x][y][z] = voxel;
    }

    public void render() {
        GL30.glBindVertexArray(mesh.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glDrawElements(GL_TRIANGLES, mesh.getVertCount(), GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void setup(GLCapabilities capabilities){
        GL.setCapabilities(capabilities);
        transformation  = new Transformation(position, new Vector3f(0, 0, 0), 1);
    }

    public void setMesh(Mesh mesh){
        this.mesh = mesh;
    }

    /*
    public void createMesh(){
        Voxel v;
        ArrayList<Float> floats = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        int ind = 0;
        for (int x = 1; x < voxels.length -1; x++) {
            for (int y = 1; y < voxels[x].length -1; y++) {
                for (int z = 1; z < voxels.length -1; z++) {
                     v = voxels[x][y][z];
                    int offset = ind;
                    if (v.getIsVisible()){
                        if (!voxels[x][y + 1][z].getIsVisible()){
                            indices.add(0 + offset);
                            indices.add(4 + offset);
                            indices.add(3 + offset);
                            indices.add(3 + offset);
                            indices.add(4 + offset);
                            indices.add(7 + offset);
                            ind += 6;
                        }
                        //BOTTOM
                        if (!voxels[x][y - 1][z].getIsVisible()){
                            indices.add(1 + offset);
                            indices.add(5 + offset);
                            indices.add(2 + offset);
                            indices.add(2 + offset);
                            indices.add(5 + offset);
                            indices.add(6 + offset);
                            ind += 6;
                        }
                        //RIGHT
                        if (!voxels[x - 1][y][z].getIsVisible()){
                            indices.add(3 + offset);
                            indices.add(2 + offset);
                            indices.add(7 + offset);
                            indices.add(7 + offset);
                            indices.add(2 + offset);
                            indices.add(6 + offset);
                            ind += 6;
                        }
                        //LEFT
                        if (!voxels[x + 1][y][z].getIsVisible()){
                            indices.add(0 + offset);
                            indices.add(1 + offset);
                            indices.add(4 + offset);
                            indices.add(4 + offset);
                            indices.add(1 + offset);
                            indices.add(5 + offset);
                            ind += 6;
                        }
                        //FRONT
                        if (!voxels[x][y][z + 1].getIsVisible()){
                            indices.add(0 + offset);
                            indices.add(1 + offset);
                            indices.add(3 + offset);
                            indices.add(3 + offset);
                            indices.add(1 + offset);
                            indices.add(2 + offset);
                            ind += 6;
                        }
                        //BACK
                        if (!voxels[x][y][z - 1].getIsVisible()){
                            indices.add(4 + offset);
                            indices.add(5 + offset);
                            indices.add(7 + offset);
                            indices.add(7 + offset);
                            indices.add(5 + offset);
                            indices.add(6 + offset);
                            ind += 6;
                        }

                    }
                }
            }
        }
        float[] vertices = {
                -0.5f,   0.5f,  0.5f,
                -0.5f,  -0.5f,  0.5f,
                0.5f,  -0.5f,  0.5f,
                0.5f,   0.5f,  0.5f,
                -0.5f,   0.5f, -0.5f,
                -0.5f,  -0.5f, -0.5f,
                0.5f,  -0.5f, -0.5f,
                0.5f,   0.5f, -0.5f,
        };
        float[] colours = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.5f,
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.5f,
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[]{
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                0, 4, 3, 3, 4, 7,
                // Right face
                3, 2, 7, 7, 2, 6,
                // Left face
                0, 1, 4, 4, 1, 5,
                // Bottom face
                1, 5, 2, 2, 5, 6,
                // Back face
                4, 5, 7, 7, 5, 6
        };

        mesh = new Mesh(vertices, colours, indices);
        needsUpdate = false;
    }*/

    @Override
    public void cleanup() {
        mesh.cleanup();
        transformation = null;
    }
}
