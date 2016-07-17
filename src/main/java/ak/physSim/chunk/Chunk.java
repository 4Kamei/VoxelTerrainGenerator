package ak.physSim.chunk;

import ak.physSim.render.meshes.Mesh;
import ak.physSim.render.Renderable;
import ak.physSim.render.Transformation;
import ak.physSim.util.Logger;
import ak.physSim.util.Reference;
import ak.physSim.voxel.Voxel;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;
import java.util.HashSet;

import static ak.physSim.util.Reference.CHUNK_SIZE;
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
    public byte[][][] lightmap = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

    Mesh mesh;

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
        voxels[x][y][z] = voxel;
    }

    public void render() {
        GL30.glBindVertexArray(mesh.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
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

    public void setMesh(Mesh mesh){
        this.mesh = mesh;
    }

    @Override
    public void cleanup() {
        mesh.cleanup();
        transformation = null;
    }

    public Vector3i getPosition() {
        return position;
    }

    //XXXX0000
    public final int getSunlighting(int x, int y, int z){
        return (lightmap[x][y][z] >> 4) & 0xF;
    }

    //0000XXXX
    public final int getArtificialLight(int x, int y, int z){
        return (lightmap[x][y][z] & 0xF);
    }

    public final void setSunlight(int x, int y, int z, int value){
        lightmap[x][y][z] = (byte) ((lightmap[x][y][z] & 0xF) | value << 4);
    }

    public final void setArtificialLight(int x, int y, int z, int value){
        lightmap[x][y][z] = (byte) ((lightmap[x][y][z] & 0xF0) | value);
    }

}
