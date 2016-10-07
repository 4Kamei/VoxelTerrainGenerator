package ak.physSim.chunk;

import ak.physSim.LightNode;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3i;
import org.lwjgl.opengl.GLCapabilities;

import java.sql.ResultSet;
import java.util.*;

import static ak.physSim.util.Reference.CHUNK_SIZE;
import static ak.physSim.util.Reference.CHUNK_SIZE_POW2;

/**
 * Created by Aleksander on 26/06/2016.
 */
public class ChunkManager {
    private HashMap<Point3d, Chunk> chunkMap;
    private LinkedList<Point3d> needsMeshUpdate;
    private GLCapabilities glCapabilities;

    private ArrayList<Point3d> viewReadyChunks = new ArrayList<>();

    public ChunkManager(GLCapabilities glCapabilities) {
        this.glCapabilities = glCapabilities;
        this.chunkMap = new HashMap<>();
        needsMeshUpdate = new LinkedList<>();
    }

    public void addChunk(Point3d point, Chunk chunk) {
        if (!chunkMap.containsKey(point))
            chunkMap.put(point, chunk);
        setNeedsMeshUpdate(point);
        Logger.log(Logger.LogLevel.DEBUG, "added chunk " + point.toString());
    }

    public void setNeedsMeshUpdate(Point3d position) {
        if (!chunkMap.containsKey(position))
            Logger.log(Logger.LogLevel.ERROR, "CHUNK WITH UNKNOWN POSITION ADDED {" + position.toString());
        if (!needsMeshUpdate.contains(position))
            needsMeshUpdate.add(position);
        viewReadyChunks.remove(position);
    }

    public void computeAllMeshUpdates() {
        int i = 0;
        long startTime = System.currentTimeMillis();
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
        }
        long timeTaken = System.currentTimeMillis() - startTime;

        Logger.log(Logger.LogLevel.ALL, "Average meshing time for " + i + " chunks is " + (i != 0 ? (timeTaken/i) : "<1") + ". Total time is " + timeTaken);

    }

    public void computeMeshUpdates() {
        for (int i = 0; i < 5 && needsMeshUpdate.peek() != null; i++) {
            Point3d p = needsMeshUpdate.poll();
            Chunk chunk = chunkMap.get(p);
            ChunkMesher mesher = new ChunkMesher(chunk, this);
            mesher.run();
            try {
                chunk.setMesh(mesher.getMesh());
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.ERROR, e.getMessage());
            }
            viewReadyChunks.add(p);
        }
    }

    //Pass chucks to renderer
    public ArrayList<Chunk> getChunks() {
        if(needsMeshUpdate.size() > 0){
            System.out.println("computing");
            computeMeshUpdates();
        }
        ArrayList<Chunk> render = new ArrayList<>();
        for (Point3d viewReadyChunk : viewReadyChunks) {
            render.add(chunkMap.get(viewReadyChunk));
        }
        return render;
    }

    //Cleanup all chunks
    public void cleanup() {
        for (Chunk chunk : chunkMap.values()) {
            chunk.cleanup();
        }
    }

    public void addVoxel(int x, int y, int z, Voxel voxel) {
        Point3d chunkPoint =  new Point3d(getChunkPos(x), getChunkPos(y), getChunkPos(z));
        if (!chunkMap.containsKey(chunkPoint))
            chunkMap.put(chunkPoint, createNewChunk(chunkPoint));
        if(voxel.getType() != VoxelType.AIR) {
            chunkMap.get(chunkPoint).setVoxel(x - chunkPoint.getX() * CHUNK_SIZE, y - chunkPoint.getY() * CHUNK_SIZE, z - chunkPoint.getZ() * CHUNK_SIZE, voxel);
        } else {
            chunkMap.get(chunkPoint).setVoxel(x - chunkPoint.getX() * CHUNK_SIZE, y - chunkPoint.getY() * CHUNK_SIZE, z - chunkPoint.getZ() * CHUNK_SIZE, null);
        }
        setNeedsMeshUpdate(chunkPoint);
    }

    private Chunk createNewChunk(Point3d point3d) {
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
        if (chunkMap.containsKey(point)) {
            return chunkMap.get(point).getVoxel(x - point.getX()*CHUNK_SIZE, y - point.getY()*CHUNK_SIZE, z - point.getZ()*CHUNK_SIZE);
        }
        return null;
    }

    private int getChunkPos(int pos) {
        return (int) Math.floor(pos/(double) CHUNK_SIZE);
    }

    public void addChunk(int x, int y, int z, Chunk chunk) {
        addChunk(new Point3d(x, y, z), chunk);
    }

    private boolean outOfBounds(int value) {
        return value >= CHUNK_SIZE || value < 0;
    }

}
