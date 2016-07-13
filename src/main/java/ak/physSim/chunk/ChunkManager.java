package ak.physSim.chunk;

import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.util.Reference;
import ak.physSim.voxel.Voxel;
import org.joml.FrustumIntersection;
import org.joml.FrustumRayBuilder;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLCapabilities;

import java.awt.*;
import java.util.*;

import static ak.physSim.util.Reference.CHUNK_SIZE;

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
            ChunkMesher mesher = new ChunkMesher(chunk, this);
            mesher.run();
            try {
                chunk.setMesh(mesher.getMesh());
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.ERROR, e.getMessage());
            }
            Logger.log(Logger.LogLevel.DEBUG, "Chunk Loop is " + i);
            Logger.log(Logger.LogLevel.DEBUG, "Mesh Update " + needsMeshUpdate.size());
        }

    }
    public void computeUpdates() {
        for (int i = 0; i < 5 && needsMeshUpdate.peek() != null; i++) {
            Chunk chunk = chunkMap.get(needsMeshUpdate.poll());
            ChunkMesher mesher = new ChunkMesher(chunk, this);
            mesher.run();
            try {
                chunk.setMesh(mesher.getMesh());
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.ERROR, e.getMessage());
            }
        }
    }

    public ArrayList<Renderable> getChunks(){
        return new ArrayList<>(chunkMap.values());
    }



    public void cleanup() {
        for (Chunk chunk : chunkMap.values()) {
            chunk.cleanup();
        }
    }


    public void addPoint(int x, int y, int z, Voxel voxel) {
        Point3d chunkPoint =  new Point3d(getChunkPos(x), getChunkPos(y), getChunkPos(z));
        if (!chunkMap.containsKey(chunkPoint))
            chunkMap.put(chunkPoint, createNewChunk(chunkPoint));
        Chunk c = chunkMap.get(chunkPoint);
        c.setVoxel(x - chunkPoint.getX()*CHUNK_SIZE, y - chunkPoint.getY()*CHUNK_SIZE, z - chunkPoint.getZ()*CHUNK_SIZE, voxel);
        setNeedsMeshUpdate(chunkPoint);
    }

    private Chunk createNewChunk(Point3d point3d){
        return createNewChunk(point3d.getX(), point3d.getY(), point3d.getZ());
    }

    private Chunk createNewChunk(int x, int y, int z) {
        Chunk chunk = new Chunk(x, y, z);
        Logger.log(Logger.LogLevel.DEBUG, "Adding new chunk " + new Point3d(x,y,z).toString());
        chunk.setup(glCapabilities);
        return chunk;
    }


    public Voxel getVoxel(int x, int y, int z) {
        Point3d point = new Point3d(getChunkPos(x), getChunkPos(y), getChunkPos(z));
        if (chunkMap.containsKey(point)){
            return chunkMap.get(point).getVoxel(x - point.getX()*CHUNK_SIZE, y - point.getY()*CHUNK_SIZE, z - point.getZ()*CHUNK_SIZE);
        }
        return null;
    }

    private int getChunkPos(int pos) {
        return (int) Math.floor(pos/(double) CHUNK_SIZE);
    }
}
