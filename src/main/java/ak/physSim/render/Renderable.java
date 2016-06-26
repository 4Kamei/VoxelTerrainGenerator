package ak.physSim.render;

import org.lwjgl.opengl.GLCapabilities;

/**
 * Created by Aleksander on 23/06/2016.
 */
public abstract class Renderable {
    protected Mesh mesh;
    protected Transformation transformation;
    protected boolean toRender;
    public Mesh getMesh() {
        return mesh;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public boolean isToRender() {
        return toRender;
    }

    public void setToRender(boolean toRender) {
        this.toRender = toRender;
    }

    public void render() {}

    public void setup(GLCapabilities capabilities){};

    public void cleanup() {
    }
}
