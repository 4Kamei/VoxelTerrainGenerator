package ak.physSim.main;

import ak.physSim.input.Console;
import ak.physSim.input.GameAction;
import ak.physSim.input.KeyManager;
import ak.physSim.render.light.AreaLight;
import ak.physSim.entity.Player;
import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import ak.physSim.util.Reference;
import ak.physSim.util.ShaderProgram;
import ak.physSim.util.Utils;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import com.sun.corba.se.impl.logging.UtilSystemException;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // The window handle
    private long window;

    private int HEIGHT = 600,
                WIDTH  = 800;
    AreaLight areaLight;
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
        GLFWErrorCallback.createPrint(System.out).set();
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

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



        //Create GLContext
    }
    private void initGL() throws Exception {
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);

        ShaderProgram orthoShader = new ShaderProgram();

        orthoShader.createVertexShader(Utils.loadResource("res/GLSL/orthoVertex.vs"));
        orthoShader.createFragmentShader(Utils.loadResource("res/GLSL/orthoFragment.fs"));

        orthoShader.createUniform("projection");
        orthoShader.createUniform("view");
        orthoShader.createUniform("model");

        projectionShader = new ShaderProgram();
        projectionShader.createVertexShader(Utils.loadResource("res/GLSL/vertex.vs"));
        projectionShader.createFragmentShader(Utils.loadResource("res/GLSL/fragment.fs"));
        projectionShader.link();

        projectionShader.createUniform("projection");
        projectionShader.createUniform("view");

        projectionShader.createUniform("model");
        projectionShader.createUniform("voxelLight");

        projectionShader.createLightUniform("light");

        projectionShader.bind();
        projectionShader.setUniform("light.colIntensities", new Vector3f(1, 1, 1));
        projectionShader.unbind();

        projectionMatrix = new Matrix4f().perspective(fov, aspectRatio, zNear, zFar);
        orthographicMatrix = new Matrix4f().ortho2D(0, WIDTH, 0, HEIGHT);

        renderer = new Renderer(projectionShader, null , projectionMatrix, orthographicMatrix);
        renderer.setConsole(console);
    }

    private void initObjects(){
        player = new Player(new Vector3f(0, 0, 0), (float) Math.PI, (float) (Math.PI/2));
        map = new WorldManager(player, /*TODO: LOAD MAP HERE OR SOMETHING*/GL.getCapabilities());
        areaLight = new AreaLight(player.getPosition());
    }
    private void loop() throws Exception {


        KeyManager callback = new KeyManager(console);
        callback.registerAction(GameAction.GLFW_EXIT, (up) -> glfwSetWindowShouldClose(window, true));
        callback.registerPlayerActions(player);

        glfwSetKeyCallback(window, callback);

        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long l, double x, double y) {
                player.updateMouse((float) (x / WIDTH), (float) (y / HEIGHT));
            }
        });

        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long l, int button, int pressing, int i2) {
                System.out.println("l = [" + l + "], button = [" + button + "], i1 = [" + pressing + "], i2 = [" + i2 + "]");
                if (button == 1)
                    map.generateChunk(player.getPosition().x / Reference.CHUNK_SIZE, player.getPosition().y / Reference.CHUNK_SIZE, player.getPosition().z / Reference.CHUNK_SIZE);
                if (button == 0)
                    map.addVoxel((int) player.getPosition().x, (int) player.getPosition().y, (int) player.getPosition().z, new Voxel(VoxelType.LIGHT));
                if (button == 2) {
/*                    areaLight = new AreaLight(player.getPosition());
                    projectionShader.bind();
                    projectionShader.setUniform("light.position", areaLight.getPosition());
                    projectionShader.unbind();*/
                }
            }
        });

        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double v, double dir) {
                areaLight.addPower(dir);
                projectionShader.bind();
                projectionShader.setUniform("light.power", areaLight.power());
                projectionShader.unbind();
            }
        });
        while ( !glfwWindowShouldClose(window) ) {
            update();
            renderer.addRenderables(map.getObjectsToRender());  //Pull renderables from map and render (or don't render) them.
            renderer.render(viewMatrix);
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        map.cleanup();
        Logger.log(Logger.LogLevel.ALL, "Closing");
    }

    private void update() {
        viewMatrix.identity()
                .rotateX(player.getPitch())
                .rotateY(player.getAzimuth());
        viewMatrix.translate(player.getTransform());
        projectionShader.setUniform("light.position", player.getPosition());
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.update(/*DELTAS*/ 16);
        if (player.isActiveUpdated()){
            if (player.lookActive())
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            else
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

}

