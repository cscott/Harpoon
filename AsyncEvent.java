package javax.realtime;

import java.util.LinkedList;
import java.util.Iterator;

/** An asynchronous event represents something that can happen,
 *  like a light turning red. It can have a set of handlers
 *  associated with it, and when the event occurs, the handler
 *  is scheduled by the scheduler to which it holds a reference.
 *  <p>
 *  A major motivator for this style of building events is that
 *  we expect to have lots of events and lots of event handlers.
 *  An event handler is logically very similar to a thread, but
 *  it is intended to have a much lower cost (in both time and
 *  space) -- assuming that a relatively small number of events
 *  are fired and in the process of being handled at once.
 *  <code>AsyncEvent.fire()</code> deffers from a method call
 *  because the handler has scheduling parameters and is executed
 *  asynchronously.
 */
public class AsyncEvent {

    protected LinkedList handlersList;

    public AsyncEvent() {
	handlersList = new LinkedList();
    }

    /** Add a handler to the set of handlers associated with this
     *  event. An <code>AsyncEvent</code> may have more than one
     *  associated handler.
     */
    public void addHandler(AsyncEventHandler handler) {
	handlersList.add(handler);
    }

    /** Binds this to an external event (a happening). The meaningful
     *  values of <code>happening</code> are implemetation dependent.
     *  This <code>AsyncEvent</code> is considered to have occured
     *  whenever the external event occurs.
     */
    public void bindTo(String happening)
	throws UnknownHappeningException {
	// TODO
    }

    /** Create a <code>ReleaseParameters</code> block appropriate
     *  to the timing characteristics of the event. The default is
     *  the most pessimistic: <code>AperiodicParameters</code>.
     *  This is typically called by code that is setting up a
     *  handler for this event that will fill in the parts of the
     *  release parameters that it knows the values for, like cost.
     */
    public ReleaseParameters createReleaseParameters() {
	return new AperiodicParameters(null, null, null, null);
    }

    /** Fire (schedule the <code>run()</code> methods of) the handlers
     *  associated with this event.
     */
    public void fire() {
	for (Iterator it = handlersList.iterator(); it.hasNext(); )
	    ((AsyncEventHandler)it.next()).run();
    }

    /** Returns true if and only if this event is handled by this handler. */
    public boolean handledBy(AsyncEventHandler handler) {
	return (handlersList.contains(handler));
    }

    /** Remove a handler from the set associated with this event. */
    public void removeHandler(AsyncEventHandler handler) {
	handlersList.remove(handler);
    }

    /** Associate a new handler with this event, removing all existing handlers. */
    public void setHandler(AsyncEventHandler handler) {
	handlersList.clear();
	handlersList.add(handler);
    }

    /** Removes a binding to an external event (a happening). The
     *  meaningful values of <code>happening</code> are implementation
     *  dependent.
     */
    public void unbindTo(String happening)
	throws UnknownHappeningException {
	// TODO
    }
}
