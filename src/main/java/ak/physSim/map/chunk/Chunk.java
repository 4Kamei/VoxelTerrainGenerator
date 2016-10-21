package ak.physSim.map.chunk;

import ak.physSim.render.meshes.FullMesh;
import ak.physSim.render.Renderable;
import ak.physSim.render.Transformation;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import static ak.physSim.util.Reference.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Chunk extends Renderable {

    //Store block data
    private Voxel[][][] voxels = new Voxel[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

    //Store lightmap data
    //Data stored like this
    //TODO: U is usused. Probably going to make colours 8bit instead of 4
    //UUUU UUUU UUUU UUUU RRRR GGGG BBBB SSSS

    //Lightnode propagation in this chunk.

    private FullMesh mesh;

    //Position as vector, used for transform
    private Vector3i position;

    public Chunk(int x, int y, int z) {
        this.position = new Vector3i(x, y, z).mul(CHUNK_SIZE);
    }

    public Voxel[][][] getVoxels() {
        return voxels;
    }

    public Voxel getVoxel(int x, int y, int z) {
        return voxels[x][y][z];
    }

    public void setVoxel(int x, int y, int z, Voxel voxel) {
        //Set voxel, air and null are the same (delete voxel)
        if(voxel.getType() == VoxelType.AIR) {
            voxels[x][y][z] = null;
            return;
        }
        voxels[x][y][z] = voxel;
    }

    public void render() {
        GL30.glBindVertexArray(mesh.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
/*
        if (b)
            glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
        else
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        */
//        Logger.log(Logger.LogLevel.DEBUG, "Rendering at " + String.format("{%.1f,%.1f,%.1f}", position.x, position.y, position.z));
        glDrawElements(GL_TRIANGLES, mesh.getVertCount(), GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        GL30.glBindVertexArray(0);
    }

    public void setup(GLCapabilities capabilities){
        GL.setCapabilities(capabilities);
        transformation  = new Transformation(new Vector3f(position.x, position.y, position.z), new Vector3f(0, 0, 0), 1f);
    }

    public void setMesh(FullMesh mesh){
        this.mesh = mesh;
    }

    @Override
    public void cleanup() {
        if (mesh != null)
        mesh.cleanup();
        transformation = null;
    }

    public final Vector3i getPosition() {
        return position;
    }
}
