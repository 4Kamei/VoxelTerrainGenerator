package ak.physSim.voxel;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Created by Aleksander on 25/06/2016.
 */
public enum  VoxelType {
    STONE (0.6f, 0.6f, 0.6f),
    DARK_STONE (0.8f, 0.8f, 0.8f),
    GRASS (0, 0.4f, 0),
    AIR(0, 0, 0),
    CLOUD(1, 1, 1);

    private final float[] colour = new float[3];

    VoxelType(float red, float green, float blue){
        colour[0] = red;
        colour[1] = green;
        colour[2] = blue;
    }

    public float[] getColour() {
        return colour;
    }
}
