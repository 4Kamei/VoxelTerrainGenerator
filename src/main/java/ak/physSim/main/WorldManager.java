package ak.physSim.main;

import ak.physSim.Player;
import ak.physSim.chunk.Chunk;
import ak.physSim.chunk.ChunkManager;
import ak.physSim.render.Renderable;
import ak.physSim.util.Logger;
import ak.physSim.util.Point3d;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.SimplexNoise;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.stb.STBPerlin;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.HashMap;

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
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                chunk = new Chunk(x, -1, z);
                chunk.setup(this.capabilities);
                for (int cX = 0; cX < 16; cX++) {
                    for (int cZ = 0; cZ < 16; cZ++) {
                        for (int cY = 0; cY < SimplexNoise.noise(x * 16 + cX, z * 16 + cY); cY++) {
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

    public ArrayList<Renderable> getObjectsToRender() {
        return manager.getVisibleChunks(player.getPosition(), player.getLookVector());
    }

    public void cleanup() {
        manager.cleanup();
    }
}
