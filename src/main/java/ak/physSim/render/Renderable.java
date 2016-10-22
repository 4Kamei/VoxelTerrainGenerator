package ak.physSim.render;

import ak.physSim.render.meshes.Mesh;

/**
 * Created by Aleksander on 23/06/2016.
 */
public abstract class Renderable {

    protected Mesh mesh;

    protected Transformation transformation;

    public Transformation getTransformation() {
        return transformation;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void cleanup() {
        mesh.cleanup();
    }

}
