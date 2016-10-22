package ak.physSim.entity;

import ak.physSim.map.chunk.Chunk;
import ak.physSim.render.Renderable;
import ak.physSim.render.meshes.Mesh;
import ak.physSim.util.Logger;
import ak.physSim.util.Reference;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

/**
 * Created by Aleksander on 21/10/2016.
 */
public class CollisionChecker {
    private ArrayList<Renderable> renderable;

    public CollisionChecker() {
        this.renderable = new ArrayList<>();
    }

    public void clearMeshes() {
        renderable.clear();
    }

    public void addMesh(Renderable mesh) {
        renderable.add(mesh);
    }

    public boolean checkCollision(Vector3f position, Vector3f lower, Vector3f higher) {
        if (renderable.isEmpty())
            return false;
        for (Renderable rend : renderable) {
            if (rend instanceof Chunk) {
                Chunk c = (Chunk) rend;
                if (new Vector3f(c.getPosition().x, c.getPosition().y, c.getPosition().z).add(Reference.CHUNK_SIZE >> 1, Reference.CHUNK_SIZE >> 1, Reference.CHUNK_SIZE >> 1).distance((int) position.x, (int) position.y, (int) position.z) > Reference.CHUNK_SIZE) {
                    continue;
                }
                if (c.checkCollision(position, lower, higher)) {
                    return true;
                }
            } else {
                Logger.log(Logger.LogLevel.ERROR, "UNKNOWN TYPE " + rend.toString().replaceAll("^[a-zA-Z]", ""));
            }
        }
        return false;
    }
}
