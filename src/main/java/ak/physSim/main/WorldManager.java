package ak.physSim.main;

import ak.physSim.entity.Player;
import ak.physSim.chunk.Chunk;
import ak.physSim.chunk.ChunkManager;
import ak.physSim.render.RenderableBase;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.SimplexNoise;
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
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                chunk = new Chunk(x, -1, z);
                chunk.setup(this.capabilities);
                for (int cX = 0; cX < 16; cX++) {
                    for (int cZ = 0; cZ < 16; cZ++) {
                        for (int cY = 0; cY < 16; cY++) {
                            chunk.setVoxel(cX, cY, cZ, new Voxel(VoxelType.STONE));
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

    public ArrayList<RenderableBase> getObjectsToRender() {
        return manager.getVisibleChunks(player.getPosition(), player.getLookVector());
    }

    public void cleanup() {
        manager.cleanup();
    }
}
