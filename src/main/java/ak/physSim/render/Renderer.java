package ak.physSim.render;

import ak.physSim.entity.Light;
import ak.physSim.entity.Player;
import ak.physSim.map.chunk.Chunk;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;

import static ak.physSim.util.Reference.CHUNK_SIZE;

/**
 * Created by Aleksander on 23/06/2016.
 */
public class Renderer {

    int frameCount;
    private long totalTime;

    //Shader for rendering the scene
    private ShaderProgram sceneRenderProgram;
    //All of the scene objects. Subject to change.
    private ArrayList<Chunk> renderables;
    //Scene projection matrix
    private Matrix4f projectionMatrix;

    //Shader for rendering the console/ui
    private ShaderProgram depthRenderProgram;

    //Orthogonal projection matrix. For UI
    private Matrix4f orthMatrix;

    //Create shadow shadowMap
    private ShadowMap shadowMap;

    //Scene light
    private Light areaLight;

    //Size of the window
    private int width = 0, height = 0;

    //Timer for one second
    private long startTime = 0;

    private Player player;

    private FrustumIntersection playerViewFrustum = new FrustumIntersection();
    private Matrix4f projectionViewMatrix = new Matrix4f();

    private Matrix4f depthViewMatrix = new Matrix4f();
    private Matrix4f depthProjectionMatrix = new Matrix4f();
    private Vector3f depthViewTranslate = new Vector3f();

    private Chunk chunk;
    private Vector3i pos;
    private boolean renderLight;

    public void setResolution(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public Renderer(ShaderProgram defaultProgram, ShaderProgram depthRender, Matrix4f projectionMatrix) throws Exception {
        this.sceneRenderProgram = defaultProgram;
        this.depthRenderProgram = depthRender;
        this.projectionMatrix = projectionMatrix;
        renderables = new ArrayList<>();
        shadowMap = new ShadowMap();
        areaLight = new Light(20, new Vector3f(0), (float) (0.75 * Math.PI), (float) (0.75 * Math.PI));
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    public void render(Matrix4f viewMatrix) throws Exception {
        //start counting time (nanoseconds)
        if (startTime == 0)
            startTime = System.currentTimeMillis();

        projectionViewMatrix.identity().mul(projectionMatrix).mul(viewMatrix);

        //GL11.glCullFace(GL11.GL_FRONT);

        //Render the scene from perspective of the light
        renderShadowDepthMap();

        //GL11.glCullFace(GL11.GL_BACK);

        //Clear color and depth buffers
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glViewport(0, 0, width, height);

        sceneRenderProgram.bind();

        sceneRenderProgram.setUniform("view", viewMatrix);
        sceneRenderProgram.setUniform("projection", projectionMatrix);

        sceneRenderProgram.setUniform("shadowMap", 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getId());
        GL33.glBindSampler(0, GL11.GL_DEPTH);

        playerViewFrustum.set(projectionViewMatrix);
        render(playerViewFrustum, sceneRenderProgram);

        sceneRenderProgram.unbind();

        frameCount++;
        //add to total time
        if (startTime + 1000 < System.currentTimeMillis()) {
            long timeTaken = System.currentTimeMillis() - startTime;
            double fps = frameCount / (double) timeTaken;
            Logger.log(Logger.LogLevel.DEBUG, String.format("FPS = %.3f", fps * 1000));
            Logger.log(Logger.LogLevel.DEBUG, String.format("Average time for one frame = %.3fms", 1/fps));
            frameCount = 0;
            startTime = 0;
        }
    }

    private void renderShadowDepthMap() {
        //Light Pos == playerPos
        //Light view = camera direction

        depthViewMatrix.identity();
        depthViewTranslate.set(0);

        depthViewMatrix.rotate(areaLight.getPitch(), 1, 0, 0)
                .rotate(areaLight.getAzimuth(), 0, 1, 0);

        if (player != null) {
            depthViewTranslate.set(areaLight.getLookVector()).mul(areaLight.getLightPower()).add(player.getTransform().x, 0, player.getTransform().z);
            depthViewMatrix.translate(depthViewTranslate);
        }
        //viewMatrix.identity().lookAt(new Vector3f(areaLight.getLookVector()).mul(areaLight.getLightPower()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        float power = 600;
        depthProjectionMatrix.identity().ortho(-power, power, -power, power, -power, power);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        GL11.glViewport(0, 0, shadowMap.getDepthMapTexture().getWidth(), shadowMap.getDepthMapTexture().getHeight());

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        depthRenderProgram.bind();

        depthRenderProgram.setUniform("projection", depthProjectionMatrix);
        depthRenderProgram.setUniform("view", depthViewMatrix);

        //render(new FrustumIntersection(new Matrix4f(orthoProjection).mul(viewMatrix)), depthRenderProgram);
        //TODO FIX SO THAT FULL SCENE IS NOT RENDERED ON LIGHT CALCULATION
        render(null, depthRenderProgram);
        depthRenderProgram.unbind();

        sceneRenderProgram.bind();

        sceneRenderProgram.setUniform("l_projection", depthProjectionMatrix);
        sceneRenderProgram.setUniform("l_view", depthViewMatrix);
        sceneRenderProgram.setUniform("shadowMap", shadowMap.getDepthMapTexture().getId());

        sceneRenderProgram.unbind();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
    }

    private void render(FrustumIntersection intersection, ShaderProgram p) {
        for (Chunk renderable : renderables) {
            if (renderable == null)
                continue;
            chunk = (Chunk) renderable;
            pos = chunk.getPosition();
            //Render if null (render full scene)
            //Render if in intersection (not null, so don't render full scene)
            if (intersection == null || intersection.testAab(pos.x, pos.y, pos.z, pos.x + CHUNK_SIZE, pos.y + CHUNK_SIZE, pos.z + CHUNK_SIZE)) {
                p.setUniform("model", renderable.getTransformation().getTranslationMatrix());
                chunk.render();
            }
        }
    }

    public void setLight(Light l) {
        this.areaLight = l;
    }

    public void addRenderables(ArrayList<Chunk> renderable){
        renderables = renderable;
    }

    public void addLight(double v) {
        areaLight.addLook(0, (float) v / 100f);
    }

    public void setRenderLight(boolean renderLight) {
        this.renderLight = renderLight;
    }
    public float getLightPitch() {
        return areaLight.getPitch();
    }

}
