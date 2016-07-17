package ak.physSim.main;

import ak.physSim.render.light.AreaLight;
import ak.physSim.entity.Player;
import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import ak.physSim.util.Utils;
import ak.physSim.voxel.Voxel;
import ak.physSim.voxel.VoxelType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // The window handle
    private long window;

    private int HEIGHT = 1200,
                WIDTH  = 1600;
    AreaLight areaLight;
    //Projection Matrix stuff;
    private static final float fov  = (float) (Math.PI/4); //60 degrees
    private static final float zNear = 0.01f;
    private static final float zFar= 1000.f;

    private Matrix4f projectionMatrix;

    private Matrix4f viewMatrix = new Matrix4f();

    private ShaderProgram shaderProgram;

    //Try decouple later on if possible
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

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
            player.setKeys(key, action);
        });
        

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

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("res/GLSL/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("res/GLSL/fragment.fs"));
        shaderProgram.link();

        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("view");

        shaderProgram.createUniform("model");

        shaderProgram.createLightUniform("light");

        shaderProgram.bind();
        shaderProgram.setUniform("light.colIntensities", new Vector3f(1));
        shaderProgram.unbind();
        projectionMatrix = new Matrix4f().perspective(fov, aspectRatio, zNear, zFar);


        renderer = new Renderer(shaderProgram, projectionMatrix);
    }

    private void initObjects(){
        player = new Player(new Vector3f(-4, 30, 0), (float) Math.PI, (float) (Math.PI/2));
        map = new WorldManager(player, /*LOAD MAP HERE OR SOMETHING*/GL.getCapabilities());
        areaLight = new AreaLight(player.getPosition());
    }
    private void loop() throws Exception {
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
                    map.addVoxel((int) player.getPosition().x, (int) player.getPosition().y, (int) player.getPosition().z, new Voxel(VoxelType.GRASS));
                if (button == 0)
                    map.addVoxel((int) player.getPosition().x, (int) player.getPosition().y, (int) player.getPosition().z, new Voxel(VoxelType.AIR));
                if (button == 2) {
                    if (areaLight != null)
                        map.addVoxel(areaLight.getPosition(), new Voxel(VoxelType.AIR));
                    areaLight = new AreaLight(player.getPosition());
                    map.addVoxel(areaLight.getPosition(), new Voxel(VoxelType.LIGHT));
                    shaderProgram.bind();
                    shaderProgram.setUniform("light.position", areaLight.getPosition());
                    shaderProgram.unbind();
                }
            }
        });

        while ( !glfwWindowShouldClose(window) ) {
            update();
            renderer.addRenderables(map.getObjectsToRender());
            renderer.render(viewMatrix);
            glfwSwapBuffers(window); // swap the color buffers

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