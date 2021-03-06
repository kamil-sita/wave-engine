package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;

import javax.swing.*;
import java.awt.*;

public final class GuiImplementation {

    private Window window;
    private WaveEngineRunning waveEngineRunning;
    private Interactions interactions;
    private GraphicsCache graphicsCache;
    private WaveCanvasImpl waveCanvas;

    public GuiImplementation(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public void initialize() {
        window = new Window();
        window.initialize();
        graphicsCache = waveEngineRunning.getWaveEngineParameters().getGraphicsCache();
    }

    public Interactions getInteractions() {
        return interactions;
    }

    public void updateRenderingSystem(WaveEngineRunning waveEngineRunning, double delta) {
        var bufferStrategy = window.canvas.getBufferStrategy();
        var graphics = (Graphics2D) bufferStrategy.getDrawGraphics();
        window.superPaintGraphics(graphics);
        if (waveEngineRunning.getWaveEngineParameters().useRepaint()) {
            var runtimeParameters = waveEngineRunning.getWaveEngineRuntimeSettings();
            graphics.setPaint(runtimeParameters.repaintColor());
            graphics.fillRect(0, 0, runtimeParameters.width(), runtimeParameters.height());
        }
        long time = System.nanoTime();
        if (waveCanvas == null) {
            waveCanvas = new WaveCanvasImpl(graphics, waveEngineRunning, waveEngineRunning.getRenderer(), graphicsCache);
        }
        waveCanvas.setGraphics(graphics);
        waveEngineRunning.getRenderingSystem().updateAndRelease(waveCanvas, delta);
        graphics.dispose();
        bufferStrategy.show();
    }

    public void shutdown() {
        window.dispose();
    }

    private class Window extends JFrame {

        private JPanel jPanel = new JPanel();
        private Canvas canvas = new Canvas();


        public Window() {
            var wers = waveEngineRunning.getWaveEngineRuntimeSettings();
            setTitle(wers.windowName());

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            setResizable(false);

            jPanel.add(canvas);
            jPanel.setPreferredSize(new Dimension(wers.width(), wers.height()));
            getContentPane().add(jPanel);
            pack();

            interactions = new Interactions(waveEngineRunning);
            canvas.addKeyListener(interactions.getListener());
            canvas.addMouseListener(interactions.getListener());
            canvas.addMouseMotionListener(interactions.getListener());
            this.addWindowListener(interactions.getListener());

            canvas.setSize(wers.width(), wers.height());

            setLocationRelativeTo(null);
        }

        public void initialize() {
            setVisible(true);
            canvas.createBufferStrategy(3);
        }

        public void superPaintGraphics(Graphics2D graphics2D) {
            super.paint(graphics2D);
        }
    }

}
