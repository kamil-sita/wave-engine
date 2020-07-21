package waveengine.guiimplementation.graphicalobject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class TextGraphicalObject implements GraphicalObject {

    private static final Map<TgoKey, BufferedImage> map = new HashMap<>();

    public TextGraphicalObject(String text, int size, Color color) {
        this.tgoKey = new TgoKey(color, text, size);
    }

    private TgoKey tgoKey;

    BufferedImage getlazy() {
        if (map.containsKey(tgoKey)) {
            return map.get(tgoKey);
        }
        BufferedImage image = create();
        map.put(tgoKey, image);
        return image;
    }

    private BufferedImage create() {
        return null;
    }

    private class TgoKey {
        private Color color;
        private String text;
        private int size;

        public TgoKey(Color color, String text, int size) {
            this.color = color;
            this.text = text;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TgoKey tgoKey = (TgoKey) o;

            if (size != tgoKey.size) return false;
            if (!color.equals(tgoKey.color)) return false;
            return text.equals(tgoKey.text);
        }

        @Override
        public int hashCode() {
            int result = color.hashCode();
            result = 31 * result + text.hashCode();
            result = 31 * result + size;
            return result;
        }
    }
}
