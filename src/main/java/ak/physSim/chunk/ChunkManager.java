package ak.physSim.chunk;

import ak.physSim.LightNode;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3i;
import org.lwjgl.opengl.GLCapabilities;

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

    private Queue<LightNode> lightPropagationQueue;

    public ChunkManager(GLCapabilities glCapabilities) {
        this.glCapabilities = glCapabilities;
        this.chunkMap = new HashMap<>();
        needsMeshUpdate = new LinkedList<>();
        lightPropagationQueue = new LinkedList<>();
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

    //Pass chucks to renderer
    public ArrayList<Renderable> getChunks() {
        if(needsMeshUpdate.size() > 0){
            System.out.println("computing");
            computeAllMeshUpdates();
        }
        return new ArrayList<>(chunkMap.values());
    }

    //Cleanup all chunks
    public void cleanup() {
        for (Chunk chunk : chunkMap.values()) {
            chunk.cleanup();
        }
    }


    //Caluclate lighting updates. Calculated every tick instead every light update to maybe save memory?

    public void calculateLightingUpdates() {
        chunkMap.values().stream().filter(Chunk::lightingNeedsUpdating).forEach(chunk -> {
            for (LightNode lightNode : chunk.getLightingSources()) {
                lightPropagationQueue.add(lightNode);
            }
        });
        while (lightPropagationQueue.size() > 0) {
            LightNode node = lightPropagationQueue.poll();
            int x, y, z;
            x = node.x;
            y = node.y;
            z = node.z;
            checkLighting(x - 1, y, z, node);
            checkLighting(x + 1, y, z, node);
            checkLighting(x, y - 1, z, node);
            checkLighting(x, y + 1, z, node);
            checkLighting(x, y, z - 1, node);
            checkLighting(x, y, z + 1, node);
        }
    }

    private void checkLighting(int x, int y, int z, LightNode node) {
        boolean newChunk = false;
        Vector3i chunkPos = node.chunk.getPosition();
        if (outOfBounds(x)) {
            int addCX = x >> CHUNK_SIZE_POW2;
            chunkPos.x += addCX;
            if (addCX == 1) {
                x = 0;
            } else {
                x = 15;
            }
            newChunk = true;
        }
        if (outOfBounds(y)) {
            int addCY = y >> CHUNK_SIZE_POW2;
            chunkPos.y += addCY;
            if (addCY == 1) {
                y = 0;
            } else {
                y = 15;
            }
            newChunk = true;
        }
        if (outOfBounds(z)) {
            int addCZ = z >> CHUNK_SIZE_POW2;
            chunkPos.y += addCZ;
            if (addCZ == 1) {
                z = 0;
            } else {
                z = 15;
            }
            newChunk = true;
        }
        LightNode newNode;
        if (!newChunk) {
            newNode = node.chunk.checkLighting(x, y, z, node);
        } else {
            newNode = chunkMap.get(new Point3d(chunkPos.x, chunkPos.y, chunkPos.z)).checkLighting(x, y, z, node);
        }
        if (newNode != null)
            lightPropagationQueue.add(newNode);
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

    public void setLighting(int x, int y, int z, int val) {
        Point3d p = new Point3d(getChunkPos(x), getChunkPos(y), getChunkPos(z));
        if (!chunkMap.containsKey(p)) {
            Logger.log(Logger.LogLevel.ERROR, "CHUNK NOT CONTAINED");
        }
        if (val < 0)
            chunkMap.values().iterator().next().setLight(x);
        chunkMap.values().iterator().next().setLight(x, y, z, val);
    }
}
