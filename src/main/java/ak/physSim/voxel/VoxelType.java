package ak.physSim.voxel;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Created by Aleksander on 25/06/2016.
 */
public enum  VoxelType {

    //TODO: Separate each type into its own class maybe?
    STONE (0.6f, 0.6f, 0.6f, 0),
    DARK_STONE (0.8f, 0.8f, 0.8f, 0),
    GRASS (0, 0.4f, 0, 0),
    AIR(0, 0, 0, 0),
    LIGHT(1,1,0, 15);

    private final float[] colour = new float[3];
    private final int lightLevel;
    VoxelType(float red, float green, float blue, int lightLevel){
        this.lightLevel = lightLevel;
        colour[0] = red;
        colour[1] = green;
        colour[2] = blue;
    }

    public float[] getColour() {
        return colour;
    }

    public boolean isLighting() {
        return lightLevel > 0;
    }

    public int getLightingLevel() {
        return lightLevel;
    }
}
