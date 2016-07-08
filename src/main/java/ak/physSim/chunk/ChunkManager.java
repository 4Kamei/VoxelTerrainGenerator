package ak.physSim.chunk;

import ak.physSim.chunk.Chunk;
import ak.physSim.render.Mesh;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTM;
import javafx.geometry.Point3D;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLCapabilities;

import java.awt.*;
import java.lang.invoke.LambdaConversionException;
import java.util.*;
import java.util.zip.CheckedInputStream;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class ChunkManager {
    private HashMap<Point3d, Chunk> chunkMap;
    private LinkedList<Point3d> needsMeshUpdate;
    private GLCapabilities glCapabilities;

    public ChunkManager(GLCapabilities glCapabilities) {
        this.glCapabilities = glCapabilities;
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
        if (!needsMeshUpdate.contains(position))
            needsMeshUpdate.add(position);
    }

    public void comupteAll() {
        int i = 0;
        while (needsMeshUpdate.peek() != null){
            i++;
            Chunk chunk = chunkMap.get(needsMeshUpdate.poll());
            ChunkMesher mesher = new ChunkMesher(chunk);
            mesher.run();
            try {
                chunk.setMesh(mesher.getMesh());
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.ERROR, e.getMessage());
            }
            Logger.log(Logger.LogLevel.DEBUG, "Chunk Loop is " + i);
            Logger.log(Logger.LogLevel.DEBUG, "Melh Update " + needsMeshUpdate.size());
        }

    }
    public void computeUpdates() {
        for (int i = 0; i < 5 && needsMeshUpdate.peek() != null; i++) {
            Chunk chunk = chunkMap.get(needsMeshUpdate.poll());
            ChunkMesher mesher = new ChunkMesher(chunk);
            mesher.run();
            try {
                chunk.setMesh(mesher.getMesh());
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.ERROR, e.getMessage());
            }
        }
    }

    public ArrayList<Renderable> getVisibleChunks(Vector3f playerPos, Vector3f lookVector){
      //TODO COMPUTE VISIBLE CHUNKS FRUSTUM CULLING
        return new ArrayList<>(chunkMap.values());
    }

    public void cleanup() {
        for (Chunk chunk : chunkMap.values()) {
            //TODO IMPLEMENT CLEANUP
        }
    }


    public void addPoint(int x, int y, int z, Voxel voxel) {
        Point3d chunkPoint = new Point3d(x/16, y/16, z/16);
        if (!chunkMap.containsKey(chunkPoint))
            chunkMap.put(chunkPoint, createNewChunk(x / 16, y / 16, z / 16));

        Chunk c = chunkMap.get(chunkPoint);
        c.setVoxel(x % 16, y % 16, z % 16, voxel);
        setNeedsMeshUpdate(chunkPoint);
    }

    private Chunk createNewChunk(int x, int y, int z) {
        Chunk chunk = new Chunk(x, y, z);
        Logger.log(Logger.LogLevel.DEBUG, "Adding new chunk " + new Point3d(x,y,z).toString());
        chunk.setup(glCapabilities);
        return chunk;
    }


}
