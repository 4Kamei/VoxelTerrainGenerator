package ak.physSim.chunk;

import ak.physSim.LightNode;
import ak.physSim.render.meshes.FullMesh;
import ak.physSim.render.Renderable;
import ak.physSim.render.Transformation;
import ak.physSim.util.ShaderProgram;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;

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
    private int[][][] lightmap = new int[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

    //Lightnode propagation in this chunk.
    private ArrayList<LightNode> lightSources;

    private FullMesh mesh;

    //Position as vector, used for transform
    private Vector3i position;

    private boolean lightingNeedsUpdating;

    public Chunk(int x, int y, int z) {
        for (int lX = 0; lX < 16; lX++) {
            for (int lY = 0; lY < 16; lY++) {
                for (int lZ = 0; lZ < 16; lZ++) {
                    lightmap[lX][lY][lZ] = 15; //OH
                }
            }
        }
        this.position = new Vector3i(x, y, z).mul(CHUNK_SIZE);
        lightSources = new ArrayList<>();
    }

    public Voxel[][][] getVoxels() {
        return voxels;
    }

    public Voxel getVoxel(int x, int y, int z) {
        return voxels[x][y][z];
    }

    public void setVoxel(int x, int y, int z, Voxel voxel) {
        voxels[x][y][z] = voxel;
        lightingNeedsUpdating = true;
        if (voxel == null)
            return;
        if (voxel.getType().isLighting()) {
            byte value = (byte) voxel.getType().getLightingLevel();
            setArtificialLighting(x, y, z, value);
            lightSources.add(new LightNode(x, y, z, value, this));
        }
    }

    public void bindLighting(ShaderProgram program) {
        program.setUniform("voxelLight", lightmap);
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

    public void setMesh(FullMesh mesh){
        this.mesh = mesh;
    }

    @Override
    public void cleanup() {
        mesh.cleanup();
        transformation = null;
    }

    public Vector3i getPosition() {
        return new Vector3i(position);
    }

    //0000 0000 0000 SSSS
    public int getSunlighting(int x, int y, int z){
        return lightmap[x][y][z] & 0xF;
    }

    public void setSunlight(int x, int y, int z, int value){
        lightmap[x][y][z] = (short) ((lightmap[x][y][z] & 0xfff0) | (value & 0xf));
    }

    //0000 0000 XXXX 0000
    private void setArtificialLighting(int x, int y, int z, int value){
        lightmap[x][y][z] = (short) ((lightmap[x][y][z] * 0xf0) | (value & 0xf) << 4);
    }

    private int getArtificialLighting(int x, int y, int z){
        return lightmap[x][y][z] >> 4 & 0xf;
    }

    public void doChunkUpdate() {
        //Chunk update
    }

    public LightNode checkLighting(int x, int y, int z, LightNode node) {
        if (voxels[x][y][z].getType() == VoxelType.AIR) { //TODO:: Check if transparent instead of air
            byte lightLevel = node.level;
            if (lightLevel == 0) {
                return null;
            }
            if (lightmap[x][y][z] < lightLevel) {
                lightmap[x][y][z] = lightLevel;
                return new LightNode(x, y, z, lightLevel, this);
            }
        } else {
            lightmap[x][y][z] = 0;
        }
        return null;
    }


    public boolean lightingNeedsUpdating() {
        return lightingNeedsUpdating;
    }

    public LightNode[] getLightingSources() {
        return lightSources.toArray(new LightNode[lightSources.size()]);
    }

}
