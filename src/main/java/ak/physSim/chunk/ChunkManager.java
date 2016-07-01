package ak.physSim.chunk;

import ak.physSim.render.meshes.Mesh;
import ak.physSim.render.RenderableBase;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class ChunkManager {
    private HashMap<Point3d, Chunk> chunkMap;
    private LinkedList<Point3d> needsMeshUpdate;

    public ChunkManager() {
        this.chunkMap = new HashMap<>();
        needsMeshUpdate = new LinkedList<>();
    }

    public void addChunk(Point3d point, Chunk chunk){
        if (!chunkMap.containsKey(point))
            chunkMap.put(point, chunk);
        needsMeshUpdate.add(point);
    }

    public void setNeedsMeshUpdate(Point3d position){
        if (!chunkMap.containsKey(position))
            Logger.log(Logger.LogLevel.ERROR, "CHUNK WITH UNKNOWN POSITION ADDED {" + position.toString());
        needsMeshUpdate.add(position);
    }

    public void comupteAll() throws Exception {
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

        Mesh mesh = new Mesh(vertices, colours, indices);
        while (needsMeshUpdate.peek() != null){
            Chunk chunk = chunkMap.get(needsMeshUpdate.poll());
            ChunkMesher mesher = new ChunkMesher(chunk);
            mesher.run();
            chunk.setMesh(mesher.getMesh());
        }
    }

    public void computeUpdates() throws Exception {
        for (int i = 0; i < 5 && needsMeshUpdate.peek() != null; i++) {
            Chunk chunk = chunkMap.get(needsMeshUpdate.poll());
            ChunkMesher mesher = new ChunkMesher(chunk);
            mesher.run();
            chunk.setMesh(mesher.getMesh());
        }
    }

    public ArrayList<RenderableBase> getVisibleChunks(Vector3f playerPos, Vector3f lookVector){
      //TODO COMPUTE VISIBLE CHUNKS FRUSTUM CULLING
        return new ArrayList<>(chunkMap.values());
    }

    public void cleanup() {
        for (Chunk chunk : chunkMap.values()) {

        }
    }
}
