package ak.physSim.main;

import ak.physSim.Player;
import ak.physSim.chunk.Chunk;
import ak.physSim.chunk.ChunkManager;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLCapabilities;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class WorldManager {
    private ChunkManager manager;
    private Player player; //TODO PLAYER
    private GLCapabilities capabilities;
    public WorldManager(GLCapabilities capabilities){
        this.capabilities = capabilities;
        Chunk chunk;
        manager = new ChunkManager();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                chunk = new Chunk(x, -1, z);
                chunk.setup(this.capabilities);
                for (int cX = 0; cX < 16; cX++) {
                    for (int cY = 0; cY < 16; cY++) {
                        for (int cZ = 0; cZ < 16; cZ++) {
                            if (x *z % 2 == 0)
                                chunk.setVoxel(cX, cY, cZ, new Voxel(VoxelType.STONE));
                            else
                                chunk.setVoxel(cX, cY, cZ, new Voxel(VoxelType.GRASS));
                        }
                    }
                }

                manager.addChunk(new Point3d(x, 1, z), chunk);
            }
        }

        try {
            Logger.log(Logger.LogLevel.ALL, "Gen of terrain meshes started");
            long time = System.currentTimeMillis();
            manager.comupteAll();
            Logger.log(Logger.LogLevel.ALL, "Gen of terrain meshes finished in " + (System.currentTimeMillis() - time) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Renderable> getObjectsToRender() {
        return manager.getVisibleChunks(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }

    public void cleanup() {
        manager.cleanup();
    }
}
