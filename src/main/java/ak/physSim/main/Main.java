package ak.physSim.main;

import ak.physSim.entity.Player;
import ak.physSim.input.Console;
import ak.physSim.input.GameAction;
import ak.physSim.input.KeyManager;
import ak.physSim.map.ChunkManager;
import ak.physSim.map.WorldManager;
import ak.physSim.map.chunk.Chunk;
import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import ak.physSim.util.Reference;
import ak.physSim.util.ShaderProgram;
import ak.physSim.util.Utils;
import ak.physSim.voxel.VoxelType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // The window handle
    private long window;

    //Size of window
    private int HEIGHT = 600,
                WIDTH  = 800;

    //Projection Matrix parameters
    private static final float fov  = (float) (Math.PI/4); //60 degrees
    private static final float zNear = 0.01f;
    private static final float zFar  = 1000f;
    private float aspectRatio = (float) WIDTH/HEIGHT;

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix = new Matrix4f();

    //Shaders for lighting and projection
    private ShaderProgram projectionShader;
    private ShaderProgram depthShaderProgram;

    private Renderer renderer;

    //Game map
    private WorldManager map;

    private int mouseX, mouseY;

    private Player player;
    //Timing variable for update method
    private long updateDelta;
    //Update thread
    private Thread updateThread;
    //Console instance
    private Console console = new Console();

    //DEBUG VARIABLE for light drawing
    private boolean drawLight = false;

    private ChunkManager chunkManager;

    public void run() {

        Logger.log(Logger.LogLevel.ALL,"Running LWJGL Version" + Version.getVersion());

        try {
            init();
            initGL();
            initObjects();
            loop();

            glfwDestroyWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            map.cleanup();
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() throws Exception {

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFWErrorCallback.createPrint(System.err).set();

        // Configure our window
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        glfwWindowHint(GLFW_SAMPLES, 4);

        window = glfwCreateWindow(WIDTH, HEIGHT, "TBD", NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 4, (vidmode.height() - HEIGHT) / 2);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync TODO : FIX THIS
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void initGL() throws Exception {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL13.GL_MULTISAMPLE);

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

    }

    private void initObjects() throws Exception {
//        player = new Player(new Vector3f(0, 0, 0), (float) Math.PI, (float) (Math.PI/2));
        player = new Player(new Vector3f(-416, 251, -100), (float) (0.57 * Math.PI), (float) (0.66 * Math.PI));
        chunkManager = new ChunkManager(GL.getCapabilities());
        map = new WorldManager(player, chunkManager);
        renderer = new Renderer(projectionShader, depthShaderProgram , projectionMatrix, chunkManager);
        renderer.setResolution(WIDTH, HEIGHT);
    }

    private void loop() throws Exception {
        updateThread = new Thread(() -> {
            long startT = 0;
            float updateRate = 0, ups = 0;
            long timeS = System.currentTimeMillis();
            while (!glfwWindowShouldClose(window)) {
                if (60 > updateRate) {
                    startT = System.nanoTime();
                    update(updateRate);
                    ups = updateRate;
                }

                if (timeS + 1000 <= System.currentTimeMillis()) {
                    Logger.log(Logger.LogLevel.ALL, updateDelta + " delta");
                    Logger.log(Logger.LogLevel.ALL, ups + " ups");
                    timeS = System.currentTimeMillis();
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

        //SCROLL LISTENER TODO NEW CLASS ?
        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long l, double v, double v1) {
                renderer.addLight(v1);
            }
        });

        //Set cursor pos listener TODO NEW CLASS
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long l, double x, double y) {
                player.updateMouse((float) (x / WIDTH), (float) (y / HEIGHT));
            }
        });

        //Give console a pointer to map
        console.setManager(map);

        //Set mouse callback TODO NEW CLASS
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long l, int button, int pressing, int i2) {
                System.out.println("l = [" + l + "], button = [" + button + "], i1 = [" + pressing + "], i2 = [" + i2 + "]");
                //if (button == 1)
                //map.generateChunk(player.getPosition().x / Reference.CHUNK_SIZE, player.getPosition().y / Reference.CHUNK_SIZE, player.getPosition().z / Reference.CHUNK_SIZE);

                if (button == 0)
                    map.addVoxel((int) (player.getPosition().x - 0.5), (int) (player.getPosition().y - 2.5), (int) (player.getPosition().z - 0.5), VoxelType.GRASS);
                //renderer.setLight(new Light(10, player.getPosition(), player.getPitch(), player.getAzimuth()));
                //if (button == 2) {
/*                  areaLight = new AreaLight(player.getPosition());
                    projectionShader.bind();
                    projectionShader.setUniform("light.position", areaLight.getPosition());
                    projectionShader.unbind();*/
            }
        });

        //Start the update thread
        updateThread.start();

        //Give renderer the instance of player
        renderer.setPlayer(player);

        while ( !glfwWindowShouldClose(window) ) {

            //Pull renderables from map
            renderer.addRenderables(map.getObjectsToRender(Reference.MAX_RENDER));

            //Draw with viewmatrix = player view matrix
            renderer.render(viewMatrix);

            //Swap buffers
            glfwSwapBuffers(window);

            //Update events
            glfwPollEvents();
        }

        Logger.log(Logger.LogLevel.ALL, "Closing");
    }

    private void update(float ups) {

        viewMatrix.identity()
                .rotateX(player.getPitch())
                .rotateY(player.getAzimuth())
                .translate(player.getTransform());

        float delta = 1/ups;

        player.update(delta);
    }

    public static void main(String[] args) {
        new Main().run();
    }

}

