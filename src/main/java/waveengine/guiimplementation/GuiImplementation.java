package waveengine.guiimplementation;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import waveengine.core.Logger;
import waveengine.core.WaveEngineRunning;
import waveengine.core.WaveEngineSystemEvents;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.Callbacks.*;

public final class GuiImplementation {

    private WaveEngineRunning waveEngineRunning;
    private Interactions interactions;
    private WaveCanvasImpl waveCanvas;
    private LWJGLWindow lwjglWindow;
    private boolean initialized = false;
    private boolean shuttingDown = false;

    // heavily based on https://www.lwjgl.org/guide

    public GuiImplementation(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public Interactions getInteractions() {
        return interactions;
    }

    public void initialize() {
        lwjglWindow = new LWJGLWindow();
        waveCanvas = new WaveCanvasImpl(waveEngineRunning, waveEngineRunning.getRenderer());
        initialized = true;
    }

    public void updateWindow(WaveEngineRunning waveEngineRunning, double delta) {

        GL.createCapabilities();


        if (lwjglWindow.windowShouldClose()) {
            waveEngineRunning.getNotifyingService().notifyListeners(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, "");
        }
        if (shuttingDown) {
            lwjglWindow.shutdown();
        }
        Color c = waveEngineRunning.getWaveEngineRuntimeSettings().repaintColor();
        glClearColor(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        waveEngineRunning.getRenderingSystem().updateAndRelease(waveCanvas, delta);

        glfwSwapBuffers(lwjglWindow.getHandle());
        glfwPollEvents();

    }

    public void shutdown() {
        shuttingDown = true;
    }

    private class LWJGLWindow {
        private long window;

        public LWJGLWindow() {
            Logger.getLogger().log("Using LWJGL, version:" + Version.getVersion());
            init();
        }

        public boolean windowShouldClose() {
            return glfwWindowShouldClose(window);
        }

        public long getHandle() {
            return window;
        }

        private void init() {
            // Setup an error callback. The default implementation
            // will print the error message in System.err.
            GLFWErrorCallback.createPrint(System.err).set();

            // Initialize GLFW. Most GLFW functions will not work before doing this.
            if ( !glfwInit() )
                throw new IllegalStateException("Unable to initialize GLFW");

            // Configure GLFW
            glfwDefaultWindowHints(); // optional, the current window hints are already the default
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

            // Create the window
            window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
            if ( window == NULL )
                throw new RuntimeException("Failed to create the GLFW window");

            // Setup a key callback. It will be called every time a key is pressed, repeated or released.
            glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            });

            // Get the thread stack and push a new frame
            try ( MemoryStack stack = stackPush() ) {
                IntBuffer pWidth = stack.mallocInt(1); // int*
                IntBuffer pHeight = stack.mallocInt(1); // int*

                // Get the window size passed to glfwCreateWindow
                glfwGetWindowSize(window, pWidth, pHeight);

                // Get the resolution of the primary monitor
                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

                // Center the window
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            } // the stack frame is popped automatically

            // Make the OpenGL context current
            glfwMakeContextCurrent(window);
            // Enable v-sync
            glfwSwapInterval(1);


            // Make the window visible
            glfwShowWindow(window);
        }

        public void shutdown() {

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }

    }

}
