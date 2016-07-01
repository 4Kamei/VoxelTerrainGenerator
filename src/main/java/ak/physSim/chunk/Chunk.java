package ak.physSim.chunk;

import ak.physSim.render.meshes.DirectionalMesh;
import ak.physSim.render.RenderableBase;
import ak.physSim.render.Transformation;
import ak.physSim.render.meshes.FaceMesh;
import ak.physSim.render.meshes.Mesh;
import ak.physSim.render.meshes.Side;
import ak.physSim.util.Reference;
import ak.physSim.voxel.Voxel;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Chunk extends RenderableBase {

    //Chunk size in blocks;

    DirectionalMesh directionalMesh;
    private Voxel[][][] voxels = new Voxel[Reference.CHUNK_SIZE][Reference.CHUNK_SIZE][Reference.CHUNK_SIZE];

    //Stores if mesh needs to be recreated

    //Position as vector, used for transform
    private Vector3f position;

    public Chunk(int x, int y, int z) {
        directionalMesh = new DirectionalMesh();
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

    public void render(Vector3i axisLook) {
        for (FaceMesh faceMesh : directionalMesh.getVisible(axisLook)) {
            GL30.glBindVertexArray(faceMesh.getVaoID());
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
//        Logger.log(Logger.LogLevel.DEBUG, "Rendering at " + String.format("{%.1f,%.1f,%.1f}", position.x, position.y, position.z));
            glDrawElements(GL_TRIANGLES, faceMesh.getVertexCount(), GL_UNSIGNED_INT, 0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
            GL30.glBindVertexArray(0);
        }
    }

    public void setup(GLCapabilities capabilities){
        GL.setCapabilities(capabilities);
        transformation  = new Transformation(position, new Vector3f(0, 0, 0), 1f);
    }

    public void setMesh(DirectionalMesh mesh){
        directionalMesh.cleanup();
        directionalMesh = mesh;
    }


    @Override
    public void cleanup() {
        directionalMesh.cleanup();
        transformation = null;
    }
}
