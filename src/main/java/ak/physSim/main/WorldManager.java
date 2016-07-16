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

    public WorldManager(Player player, GLCapabilities capabilities){
        this.player = player;
        this.capabilities = capabilities;

        generate();

    }

    private void generate() {
        manager = new ChunkManager(capabilities);
        for (int x = -100; x < 100; x++) {
            for (int z = -100; z < 100; z++) {
                int height = 5 + (int) ((Noise.gradientCoherentNoise3D(x/32f, 0, z/32f, 23423, NoiseQuality.FAST) + 1)/2 * 60);
                for (int y = 0; y < height; y++) {
                    if (y < 10)
                        addVoxel(x, y, z, new Voxel(VoxelType.DARK_STONE));
                    else
                        addVoxel(x, y, z, new Voxel(VoxelType.STONE));
                }
                addVoxel(x, height, z, new Voxel(VoxelType.STONE));

            }
        }

        manager.comupteAll();
    }

    public void addVoxel(int x, int y, int z, Voxel voxel){
        manager.addVoxel(x, y, z, voxel);
    }

    public ArrayList<Renderable> getObjectsToRender() {
        return manager.getChunks();
    }

    public void cleanup() {
        manager.cleanup();
    }
}
