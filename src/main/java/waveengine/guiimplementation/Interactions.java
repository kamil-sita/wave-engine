package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.core.WaveEngineSystemEvents;

import java.awt.event.*;

public class Interactions {
    private boolean[] keysPressed = new boolean[128];
    private Listener listener;
    private WaveEngineRunning waveEngineRunning;
    private int x;
    private int y;
    private boolean mousePressed = false;

    public Interactions(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
        listener = new Listener();
    }

    public int getMouseX() {
        return x;
    }

    public int getMouseY() {
        return y;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    Listener getListener() {
        return listener;
    }

    /**
     * Indexed by KeyEvent keycode
     */
    public boolean isKeyPressed(int id) {
        if (id < keysPressed.length) {
            return keysPressed[id];
        }
        return false;
    }

    class Listener implements KeyListener, MouseListener, MouseMotionListener, WindowListener  {

        private Listener() {

        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() < keysPressed.length) {
                keysPressed[e.getKeyCode()] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() < keysPressed.length) {
                keysPressed[e.getKeyCode()] = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePressed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mousePressed = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            x = e.getX();
            y = e.getY();
        }


        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            waveEngineRunning.getNotifyingService().notifyListeners(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, "WINDOW CLOSE REQUEST SENT BY GUI");
        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }

    }
}
