package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public final class GuiImplementation {

    private Window window;
    private WaveEngineRunning waveEngineRunning;
    private Interactions interactions;

    public GuiImplementation(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public void initialize() {
        window = new Window();
        window.initialize();
    }

    public Interactions getInteractions() {
        return interactions;
    }

    public void updateRenderingSystem(WaveEngineRunning waveEngineRunning, double delta) {
        var bufferStrategy = window.canvas.getBufferStrategy();
        var graphics = (Graphics2D) bufferStrategy.getDrawGraphics();
        window.superPaintGraphics(graphics);
        if (waveEngineRunning.getWaveEngineParameters().useRepaint()) {
            graphics.setPaint(Color.WHITE);
            var runtimeParameters = waveEngineRunning.getWaveEngineRuntimeSettings();
            graphics.fillRect(0, 0, runtimeParameters.width(), runtimeParameters.height());
        }
        var waveCanvas = new WaveCanvas(graphics);
        waveEngineRunning.getRenderingSystem().update(waveCanvas, delta);
        graphics.dispose();
        bufferStrategy.show();
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
