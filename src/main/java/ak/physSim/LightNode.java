package ak.physSim;

import ak.physSim.chunk.Chunk;

/**
 * Created by Aleksander on 26/07/2016.
 */
public class LightNode {

    public final Chunk chunk;
    public final byte x;
    public final byte y;
    public final byte z;

    public final byte level;

    public LightNode(int x, int y, int z, byte level, Chunk c) {
        this.chunk = c;
        this.x = (byte) x;
        this.y = (byte) y;
        this.z = (byte) z;
        this.level = level;
    }


}
