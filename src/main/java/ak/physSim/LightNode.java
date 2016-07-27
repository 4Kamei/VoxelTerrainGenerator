package ak.physSim;

import static ak.physSim.util.Reference.CHUNK_SIZE_POW2;

/**
 * Created by Aleksander on 26/07/2016.
 */
public class LightNode {
    int index;
    short lightIntensity;

    public LightNode(int x, int y, int z, int r, int g, int b) {
        int val = z << CHUNK_SIZE_POW2;
        val = (val | y) << CHUNK_SIZE_POW2;
        index =  val | x;
        this.lightIntensity = (short) (b << 8 | g << 4 | r);
    }

    public LightNode(int x, int y, int z, short intensity){
        int val = z << CHUNK_SIZE_POW2;
        val = (val | y) << CHUNK_SIZE_POW2;
        index =  val | x;
        lightIntensity = intensity;
    }
}
