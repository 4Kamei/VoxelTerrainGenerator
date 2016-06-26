package ak.physSim.chunk;

import ak.physSim.chunk.Chunk;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTM;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.invoke.LambdaConversionException;
import java.util.*;
import java.util.zip.CheckedInputStream;

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

    public ArrayList<Renderable> getVisibleChunks(Vector3f playerPos, Vector3f lookVector){
      //TODO COMPUTE VISIBLE CHUNKS FRUSTUM CULLING
        return new ArrayList<>(chunkMap.values());
    }

    public void cleanup() {
        for (Chunk chunk : chunkMap.values()) {

        }
    }
}
