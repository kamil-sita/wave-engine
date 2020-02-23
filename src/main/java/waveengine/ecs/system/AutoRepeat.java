package waveengine.ecs.system;

public class AutoRepeat {

    ///called
    public final static AutoRepeat AFTER_EVERY_DRAWN_FRAME = new AutoRepeat();
    public final static AutoRepeat BEFORE_EVERY_DRAWN_FRAME = new AutoRepeat();
    public final static AutoRepeat TICK_DEFAULT = new AutoRepeat();
    public final static AutoRepeat LIGHT_TICK_DEFAULT = new AutoRepeat();
    public final static AutoRepeat DO_NOT_REPEAT = new AutoRepeat();
    public final static AutoRepeat ALWAYS = new AutoRepeat();

    public AutoRepeat() {

    }
}
