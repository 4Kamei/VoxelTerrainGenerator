package ak.physSim.main;

import ak.physSim.entity.Light;
import ak.physSim.input.Console;
import ak.physSim.input.GameAction;
import ak.physSim.input.KeyManager;
import ak.physSim.entity.Player;
import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import ak.physSim.util.Reference;
import ak.physSim.util.ShaderProgram;
import ak.physSim.util.Utils;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import sun.rmi.runtime.Log;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // The window handle
    private long window;

    private int HEIGHT = 600,
                WIDTH  = 800;
    //Projection Matrix stuff;
    private static final float fov  = (float) (Math.PI/4); //60 degrees
    private static final float zNear = 0.01f;
    private static final float zFar= 1000.f;

    private Matrix4f projectionMatrix;

    private Matrix4f viewMatrix = new Matrix4f();

    private ShaderProgram projectionShader;

    private Matrix4f orthographicMatrix;
    private Console console = new Console();

    //Game map
    private WorldManager map;

    private float aspectRatio = (float) WIDTH/HEIGHT;

    private int mouseX, mouseY;
    //Game renderer, TODO: Run in different thread?
    private Renderer renderer;

    private Player player;

    private Thread updateThread;
    private long updateDelta;

    private ShaderProgram depthShaderProgram;

    private boolean drawLight = false;

    public void run() {
        Logger.log(Logger.LogLevel.ALL,"Running LWJGL Version" + Version.getVersion());

        try {


            init();
            initGL();
            initObjects();
            loop();

            // Free the window callbacks and destroy the window

            glfwDestroyWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() throws Exception {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int i, long l) {
                System.out.println(i + " | " + l);
            }
        });

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 4,
                (vidmode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);


        // Make the window visible
        glfwShowWindow(window);

    }

    private void initGL() throws Exception {
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        GLUtil.setupDebugMessageCallback(System.err);

        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createFragmentShader(Utils.loadResource("res/GLSL/depth.fs"));
        depthShaderProgram.createVertexShader(Utils.loadResource("res/GLSL/depth.vs"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("projection");
        depthShaderProgram.createUniform("view");
        depthShaderProgram.createUniform("model");

        projectionShader = new ShaderProgram();
        projectionShader.createFragmentShader(Utils.loadResource("res/GLSL/fragment.fs"));
        projectionShader.createVertexShader(Utils.loadResource("res/GLSL/vertex.vs"));
        projectionShader.link();

        projectionShader.createUniform("projection");
        projectionShader.createUniform("view");
        projectionShader.createUniform("model");
        projectionShader.createUniform("l_projection");
        projectionShader.createUniform("l_view");
        projectionShader.createUniform("shadowMap");
        projectionShader.createUniform("drawLight");

        projectionMatrix = new Matrix4f().perspective(fov, aspectRatio, zNear, zFar);
        orthographicMatrix = new Matrix4f().ortho2D(0, WIDTH, 0, HEIGHT);

        renderer = new Renderer(projectionShader, depthShaderProgram , projectionMatrix);
        renderer.setConsole(console);
        renderer.setResolution(WIDTH, HEIGHT);
    }

    private void initObjects(){
//        player = new Player(new Vector3f(0, 0, 0), (float) Math.PI, (float) (Math.PI/2));
        player = new Player(new Vector3f(-416, 251, -100), (float) (0.57 * Math.PI), (float) (0.66 * Math.PI));
        renderer.setTarget(player);
        map = new WorldManager(player, /*TODO: LOAD MAP HERE OR SOMETHING*/GL.getCapabilities());
    }

    private void loop() throws Exception {

        updateThread = new Thread(() -> {
            long startT = 0;
            float updateRate = 0, ups = 0;
            long timeS = 0;
            int upCnt = 0;
            while (!glfwWindowShouldClose(window)) {
                if (timeS == 0) {
                    timeS = System.currentTimeMillis();
                    upCnt = 0;
                }
                if (60 > updateRate) {
                    startT = System.nanoTime();
                    update(updateRate);
                    upCnt++;
                    ups = updateRate;
                }

                if (timeS + 1000 <= System.currentTimeMillis()) {
                    System.out.println(updateDelta + " delta");
                    System.out.println(ups + " ups");
                    timeS = System.currentTimeMillis();
                    upCnt = 0;
                }

                updateDelta = (System.nanoTime() - startT);
                if (updateDelta == 0)
                    updateDelta = 1;
                updateRate = 1000000000f/updateDelta;
            }
        });

        KeyManager callback = new KeyManager(console);
        callback.registerAction(GameAction.GLFW_EXIT, (up) -> glfwSetWindowShouldClose(window, true));
        callback.registerAction(GameAction.SET_LIGHT, (up) -> {
            if (up) {
                Logger.log(Logger.LogLevel.DEBUG, "Q pressed");
                projectionShader.bind();
                projectionShader.setUniform("drawLight", (drawLight = !drawLight) ? 0.0f : 1f);
                projectionShader.unbind();
            }
        });
        callback.registerPlayerActions(player);
        glfwSetKeyCallback(window, callback);

        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long l, double v, double v1) {
                renderer.addLight(v1);
            }
        });

        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long l, double x, double y) {
                player.updateMouse((float) (x / WIDTH), (float) (y / HEIGHT));
            }
        });

        console.setManager(map);
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long l, int button, int pressing, int i2) {
                System.out.println("l = [" + l + "], button = [" + button + "], i1 = [" + pressing + "], i2 = [" + i2 + "]");
                //if (button == 1)
                    //map.generateChunk(player.getPosition().x / Reference.CHUNK_SIZE, player.getPosition().y / Reference.CHUNK_SIZE, player.getPosition().z / Reference.CHUNK_SIZE);

                if (button == 0)
                    map.addVoxel((int) player.getPosition().x, (int) player.getPosition().y, (int) player.getPosition().z, new Voxel(VoxelType.GRASS));
                    //renderer.setLight(new Light(10, player.getPosition(), player.getPitch(), player.getAzimuth()));
                //if (button == 2) {
/*                  areaLight = new AreaLight(player.getPosition());
                    projectionShader.bind();
                    projectionShader.setUniform("light.position", areaLight.getPosition());
                    projectionShader.unbind();*/
                }
            });

        updateThread.start();

        while ( !glfwWindowShouldClose(window) ) {

            Matrix4f viewMat = new Matrix4f().identity()
                    .rotateX(player.getPitch())
                    .rotateY(player.getAzimuth())
                    .translate(player.getTransform());
            viewMatrix = new Matrix4f(viewMat);

            renderer.addRenderables(map.getObjectsToRender());  //Pull renderables from map and render (or don't render) them.
            renderer.render(viewMatrix);
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        map.cleanup();
        Logger.log(Logger.LogLevel.ALL, "Closing");
    }

    private void update(float ups) {
        float delta = 1/ups;
        try {
        } catch (Exception e) {
            e.printStackTrace();
            map.updatePosition();
        }
        player.update(delta);
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("/shaders/depth_vertex.vs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("/shaders/depth_fragment.fs"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("orthoProjectionMatrix");
        depthShaderProgram.createUniform("modelLightViewMatrix");
    }

    public static void main(String[] args) {
        new Main().run();
    }

}

