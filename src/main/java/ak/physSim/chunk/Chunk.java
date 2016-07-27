package ak.physSim.chunk;

import ak.physSim.LightNode;
import ak.physSim.render.meshes.Mesh;
import ak.physSim.render.Renderable;
import ak.physSim.render.Transformation;
import ak.physSim.util.Logger;
import ak.physSim.util.Reference;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import static ak.physSim.util.Reference.CHUNK_SIZE;
import static ak.physSim.util.Reference.CHUNK_SIZE_POW2;
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
    //RRRR GGGG BBBB SSSS
    public short[] lightmap = new byte[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE]; //x + y*size + z*size^2

    Queue<LightNode> lightPropagation;

    Mesh mesh;

    //Position as vector, used for transform
    private Vector3i position;

    public Chunk(int x, int y, int z) {
        this.position = new Vector3i(x, y, z).mul(CHUNK_SIZE);
        lightPropagation = new LinkedList<>();
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

        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
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
    //0000 0000 0000 SSSS
    public int getSunlighting(int x, int y, int z){
        return lightmap[getIndex(x, y, z)] & 0xF;
    }

    public void setSunlight(int x, int y, int z, int value){
        int index = getIndex(x, y, z);
        lightmap[index] = (short) ((lightmap[index] & 0xfff0) | (value & 0xf));
    }

    //0000 0000 XXXX 0000
    public void setArtificialRed(int x, int y, int z, int value){
        int index = getIndex(x, y, z);
        lightmap[index] = (short) ((lightmap[index] * 0xf0) | (value & 0xf) << 4);
    }

    public int getArtificialRed(int x, int y, int z){
        return lightmap[getIndex(x, y, z)] >> 4 & 0xf;
    }

    //0000 XXXX 0000 0000
    public void setArtificialGreen(int x, int y, int z, int value){
        int index = getIndex(x, y, z);
        lightmap[index] = (short) ((lightmap[index] * 0xf00) | (value & 0xf) << 8);
    }

    public int getArtificialGreen(int x, int y, int z){
        return lightmap[getIndex(x, y, z)] >> 8 & 0xf;
    }

    //XXXX 0000 0000 0000
    public void setArtificialBlue(int x, int y, int z, int value){
        int index = getIndex(x, y, z);
        lightmap[index] = (short) ((lightmap[index] * 0xf000) | (value & 0xf) << 12);
    }

    public int getArtificialBlue(int x, int y, int z){
        return lightmap[getIndex(x, y, z)] >> 12 & 0xf;
    }

    private int getIndex(int x, int y, int z){
        int val = z << CHUNK_SIZE_POW2;
        val = (val | y) << CHUNK_SIZE_POW2;
        return val | x;
    }

    public void addToLightPropagationQueue(LightNode node){
        lightPropagation.add(node); //TODO: LIGHT PROP ALGORITH IN CHUNK MANAGER
    }

}
