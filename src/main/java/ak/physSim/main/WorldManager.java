package ak.physSim.main;

import ak.physSim.chunk.ChunkManager;
import ak.physSim.entity.Player;
import ak.physSim.render.Renderable;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import com.flowpowered.noise.Noise;
import com.flowpowered.noise.NoiseQuality;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class WorldManager {
    private ChunkManager manager;
    private Player player;
    private GLCapabilities capabilities;
    private int blockCounter = 0;

    public WorldManager(Player player, GLCapabilities capabilities){
        this.player = player;
        this.capabilities = capabilities;

        generate();

    }

    private void generate() {
        manager = new ChunkManager(capabilities);
        for (int x = -100; x < 100; x++) {
            for (int z = -100; z < 100; z++) {
                int height = 5 + (int) ((Noise.gradientCoherentNoise3D(x/16f, 0, z/16f, 23423, NoiseQuality.FAST) + 1)/2 * 30);
                for (int y = 0; y < height; y++) {
                    if (y < 10)
                        addPoint(x, y, z, new Voxel(VoxelType.DARK_STONE));
                    else
                        addPoint(x, y, z, new Voxel(VoxelType.STONE));
                }
                addPoint(x, height, z, new Voxel(VoxelType.GRASS));
            }
        }
        manager.comupteAll();
    }

    public void addPoint(int x, int y, int z, Voxel voxel){
        manager.addPoint(x, y, z, voxel);
    }

    public ArrayList<Renderable> getObjectsToRender() {
        return manager.getVisibleChunks(player.getTransform(), player.getLookVector());
    }

    public void cleanup() {
        manager.cleanup();
    }
}
