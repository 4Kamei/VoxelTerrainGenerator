package ak.physSim.main;

import ak.physSim.entity.Player;
import ak.physSim.chunk.Chunk;
import ak.physSim.chunk.ChunkManager;
import ak.physSim.render.RenderableBase;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class WorldManager {
    private ChunkManager manager;
    private Player player;
    private GLCapabilities capabilities;
    public WorldManager(Player player, GLCapabilities capabilities){
        this.player = player;
        this.capabilities = capabilities;
        Chunk chunk;
        manager = new ChunkManager();
        int y = 1;
        int limit = 5;
        for (int x = -limit; x <= limit; x++) {
            for (int z = -limit; z <= limit; z++) {
                chunk = new Chunk(x, y, z);
                chunk.setup(this.capabilities);
                for (int cX = 0; cX < 16; cX++) {
                    for (int cZ = 0; cZ < 16; cZ++) {
                        for (int cY = 0; cY < 16; cY++) {
                            chunk.setVoxel(cX, cY, cZ, new Voxel(VoxelType.GRASS));
                        }
                    }
                }
                manager.addChunk(new Point3d(x, y, z), chunk);
            }
        }
        y = 0;
        for (int x = -limit; x <= limit; x++) {
            for (int z = -limit; z <= limit; z++) {
                chunk = new Chunk(x, y, z);
                chunk.setup(this.capabilities);
                if (x != 0 && z != 0){
                    for (int cX = 0; cX < 16; cX++) {
                        for (int cZ = 0; cZ < 16; cZ++) {
                            for (int cY = 0; cY < 16; cY++) {
                                chunk.setVoxel(cX, cY, cZ, new Voxel(VoxelType.STONE));
                            }
                        }
                    }
                    manager.addChunk(new Point3d(x, y, z), chunk);
                }
            }
        }
        y = -1;
        for (int x = -limit; x <= limit; x++) {
            for (int z = -limit; z <= limit; z++) {
                chunk = new Chunk(x, y, z);
                chunk.setup(this.capabilities);
                for (int cX = 0; cX < 16; cX++) {
                    for (int cZ = 0; cZ < 16; cZ++) {
                        for (int cY = 0; cY < 16; cY++) {
                            chunk.setVoxel(cX, cY, cZ, new Voxel(VoxelType.DARK_STONE));
                        }
                    }
                }
                manager.addChunk(new Point3d(x, y, z), chunk);
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

    public ArrayList<RenderableBase> getObjectsToRender() {
        return manager.getVisibleChunks(player.getTransform(), player.getLookVector());
    }

    public void cleanup() {
        manager.cleanup();
    }
}
