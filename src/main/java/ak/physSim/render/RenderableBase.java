package ak.physSim.render;

/**
 * Created by Aleksander on 23/06/2016.
 */
public abstract class RenderableBase {

    protected Transformation transformation;

    public Transformation getTransformation() {
        return transformation;
    }

    public void cleanup() {
    }
}
