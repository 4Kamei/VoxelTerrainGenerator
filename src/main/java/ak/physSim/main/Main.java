package ak.physSim.main;

import ak.physSim.render.Renderer;
import ak.physSim.util.Logger;
import ak.physSim.util.ShaderProgram;
import ak.physSim.util.Utils;
import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // The window handle
    private long window;

    private int HEIGHT = 600,
                WIDTH  = 800;

    //Projection Matrix stuff;
    private static final float fov  = (float) (Math.PI/6); //60 degrees
    private static final float zNear = 0.01f;
    private static final float zFar= 1000.f;

    private Matrix4f projectionMatrix;

    private ShaderProgram shaderProgram;

    //Try decouple later on if possible
    //Game map
    private WorldManager map;

    private float aspectRatio = (float) WIDTH/HEIGHT;
    private int mouseX, mouseY;

    //Game renderer, TODO: Run in different thread?
    private Renderer renderer;

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
        GLFWErrorCallback.createPrint(System.err).set();

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
        shaderProgram.createVertexShader(Utils.loadResource("src/main/GLSL/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("src/main/GLSL/fragment.fs"));
        shaderProgram.link();
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("worldMatrix");

        projectionMatrix = new Matrix4f().perspective(fov, aspectRatio, zNear, zFar);


        renderer = new Renderer(shaderProgram);
    }
    private void initObjects(){
        map = new WorldManager(/*LOAD MAP HERE OR SOMETHING*/GL.getCapabilities());
    }

    private void loop() throws Exception {
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long l, double x, double y) {
                projectionMatrix.identity()
                        .perspective(fov, aspectRatio, zNear, zFar)
                        .rotateX((float) -((y / (HEIGHT) - 0.5f) * Math.PI * 2))
                        .rotateY((float) -((x / (WIDTH)  - 0.5f) * Math.PI * 2));
            }
        });

        while ( !glfwWindowShouldClose(window) ) {
            renderer.addRenderables(map.getObjectsToRender());
            renderer.render(projectionMatrix);

            glfwSwapBuffers(window); // swap the color buffers

            glfwPollEvents();
        }

        map.cleanup();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}