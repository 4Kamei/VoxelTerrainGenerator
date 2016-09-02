package ak.physSim.render.meshes;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Aleksander on 31/08/2016.
 */
public interface Mesh {

    int vaoID = 0;

    public abstract void cleanup();

    public default int getVaoID() {
        return vaoID;
    }

    public int getVertexCount();

}
