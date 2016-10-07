package ak.physSim.render;

import ak.physSim.chunk.Chunk;
import ak.physSim.entity.Light;
import ak.physSim.entity.Player;
import ak.physSim.input.Console;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;

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
    //Console object pointer.
    private Console console;
    //Create shadow map
    private ShadowMap map;
    //Scene light
    private Light areaLight;
    //
    private int width = 0, height = 0;
    private Player player;

    public void setResolution(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public Renderer(ShaderProgram defaultProgram, ShaderProgram depthRender, Matrix4f projectionMatrix) throws Exception {
        this.sceneRenderProgram = defaultProgram;
        this.depthRenderProgram = depthRender;
        this.projectionMatrix = projectionMatrix;
        renderables = new ArrayList<>();
        map = new ShadowMap();
        areaLight = new Light(20, new Vector3f(0), (float) (0.75 * Math.PI), (float) (0.75 * Math.PI));
    }

    public void setConsole(Console c) {
        this.console = c;
    }

    public void render(Matrix4f viewMatrix) throws Exception {
        long startTime = System.currentTimeMillis();

        GL11.glCullFace(GL11.GL_FRONT);
        renderShadowDepthMap();

        GL11.glCullFace(GL11.GL_BACK);
        //Resize window handling

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glViewport(0, 0, width, height);

        sceneRenderProgram.bind();

        sceneRenderProgram.setUniform("view", viewMatrix);
        sceneRenderProgram.setUniform("projection", projectionMatrix);

        FrustumIntersection intersection = new FrustumIntersection(new Matrix4f(projectionMatrix).mul(viewMatrix));
        render(intersection, sceneRenderProgram);

        sceneRenderProgram.unbind();

        renderables.clear();

        frameCount++;
        totalTime += System.currentTimeMillis() - startTime;
        if (frameCount == 59) {
            frameCount = 0;
            Logger.log(Logger.LogLevel.DEBUG, "FPS = " + String.valueOf(1000 * 60f / totalTime));
            Logger.log(Logger.LogLevel.DEBUG, "Time elapsed for 60 frames = " + totalTime + "ms");
            totalTime = 0;/*
            Logger.log(Logger.LogLevel.DEBUG, "Lighting : " + areaLight);
            Logger.log(Logger.LogLevel.DEBUG, areaLight.getLookVector().toString());*/
        }
    }

    private void render(FrustumIntersection intersection, ShaderProgram p) {
        renderables.stream().filter(renderable -> renderable != null).forEach(renderable -> {
            Chunk chunk = (Chunk) renderable;
            //TODO FIX if (intersection.testAab(pos.x, pos.y, pos.z, pos.x + 16, pos.y + 16, pos.z + 16)) {
                p.setUniform("model", renderable.getTransformation().getTranslationMatrix());
                chunk.render();
            //}
        });
    }

    private void renderShadowDepthMap() {
        //Light Pos == playerPos
        //Light view = camera direction

        Matrix4f viewMatrix = new Matrix4f().identity();

        viewMatrix.rotate(areaLight.getPitch(), new Vector3f(1, 0, 0))
                  .rotate(areaLight.getAzimuth(), new Vector3f(0, 1, 0));

        viewMatrix.translate(new Vector3f(areaLight.getLookVector()).mul(areaLight.getLightPower())/*.add(player.getTransform())*/);

        //viewMatrix.identity().lookAt(new Vector3f(areaLight.getLookVector()).mul(areaLight.getLightPower()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

        Matrix4f orthoProjection = new Matrix4f().ortho(-400.0f, 400.0f, -400.0f, 400.0f, -400, 400);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, map.getDepthMapFBO());
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        GL11.glViewport(0, 0, map.getDepthMapTexture().getWidth(), map.getDepthMapTexture().getHeight());

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        depthRenderProgram.bind();

        depthRenderProgram.setUniform("projection", orthoProjection);
        depthRenderProgram.setUniform("view", viewMatrix);

        render(new FrustumIntersection(new Matrix4f(orthoProjection).mul(viewMatrix)), depthRenderProgram);
        //render(new FrustumIntersection(new Matrix4f(projectionMatrix).mul(playerViewMatrix)));
        depthRenderProgram.unbind();

        sceneRenderProgram.bind();

        sceneRenderProgram.setUniform("l_projection", orthoProjection);
        sceneRenderProgram.setUniform("l_view", viewMatrix);
        sceneRenderProgram.setUniform("shadowMap", map.getDepthMapTexture().getId());

        sceneRenderProgram.unbind();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        //GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, map.getDepthMapFBO());
    }

    public void setLight(Light l) {
        this.areaLight = l;
    }

    public void addRenderables(ArrayList<Chunk> renderable){
        renderables.addAll(renderable);
    }

    public void addLight(double v) {
        areaLight.addLook(0, (float) v / 100f);
    }

    public float getLightPitch() {
        return areaLight.getPitch();
    }

    public void setTarget(Player target) {
        this.player = target;
    }
}
