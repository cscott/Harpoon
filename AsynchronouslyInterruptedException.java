package javax.realtime;

public class AsynchronouslyInterruptedException
    extends InterruptedException {
    /** A special exception that is thrown in response to an attempt to
     *  asynchronously transfer the locus of control of a
     *  <code>RealtimeThread</code>.
     */

    private boolean enabled = true;

    public AsynchronouslyInterruptedException() {
	// TODO
    }

    public boolean disable() {
	enabled = false;
	// TODO

	return false;
    }

    public boolean doInterruptible(Interruptible logic) {
	// TODO

	return false;
    }

    public boolean enable() {
	enabled = true;
	// TODO

	return false;
    }

    public boolean fire() {
	// TODO

	return false;
    }

    public static AsynchronouslyInterruptedException getGeneric() {
	// TODO

	return null;
    }

    public boolean happened(boolean propagate) {
	// TODO

	return false;
    }

    public boolean isEnabled() {
	return enabled;
    }

    public static void propagate() {
	// TODO
    }
}
