package javax.realtime;

import java.util.LinkedList;

public class AsyncEvent {
    /** Asynchronous event */

    protected LinkedList handlersList = null;

    public AsyncEvent() {
	// TODO
    }

    public void addHandler(AsyncEventHandler handler) {
	handlersList.add(handler);
    }

    public void bindTo(String happening)
	throws UnknownHappeningException {
	// TODO
    }

    public ReleaseParameters createReleaseParameters() {
	// TODO

	return null;
    }

    public void fire() {
	// TODO
    }

    public boolean handledBy(AsyncEventHandler handler) {
	return (handlersList.contains(handler));
    }

    public void removeHandler(AsyncEventHandler handler) {
	handlersList.remove(handler);
    }

    public void setHandler(AsyncEventHandler handler) {
	handlersList.clear();
	handlersList.add(handler);
    }

    public void unbindTo(String happening)
	throws UnknownHappeningException {
	// TODO
    }
}
