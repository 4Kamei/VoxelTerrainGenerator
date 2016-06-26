package ak.physSim.voxel;

/**
 * Created by Aleksander on 25/06/2016.
 */
public class Voxel {
    private boolean isVisible;
    private VoxelType type;
    //OTHER VERY VERY NECESSARY DATA;

    public Voxel(VoxelType type) {
        this.type = type;
    }

    public boolean getIsVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public VoxelType getType() {
        return type;
    }
}

