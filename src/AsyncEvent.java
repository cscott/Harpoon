// AsyncEvent.java, created by Harvey Jones, documented by Dumitru Daniliuc
// Copyright (C) 2003 Harvey Jones, Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
    public static final int MAX_HANDLERS = 1000;
    protected AsyncEventHandler []handlers = new AsyncEventHandler[MAX_HANDLERS];
    protected boolean []handlerMap = new boolean[MAX_HANDLERS];
    protected int free = 0;

    /** Create a new <code>AsyncEvent</code> object. */
    public AsyncEvent() {}

    /** Add a handler to the set of handlers associated with this
     *  event. An <code>AsyncEvent</code> may have more than one
     *  associated handler.
     *
     *  @param handler The new handler to add to the list of handlers
     *                 already associated with <code>this</code>. If
     *                 <code>handler</code> is null then nothing happens.
     */
    public void addHandler(AsyncEventHandler handler) {
	if(free == MAX_HANDLERS){
	    NoHeapRealtimeThread.print("You have tried to exceed the maximum amount of handlers. Shame on you.");
	    System.exit(-1);
	}
	handlers[free] = handler;
	handlerMap[free] = true;
	/* Find the next free node. It will always be after free, because removeHandler 
	   will set free to the first free node, always. */
	for(int i = free; i<MAX_HANDLERS; i++){ 
	    if(! handlerMap[i]){
		free = i;
	    }
	}
    }

    /** Binds this to an external event, a <i>happening</i>. The meaningful
     *  values of <code>happening</code> are implementation dependent. This
     *  instance of <code>AsyncEvent</code> is considered to have occured
     *  whenever the happening occurs.
     *
     *  @param hapening An implementation dependent value that binds this
     *                  instance of <code>AsyncEvent</code> to a happening.
     *  @throws UnknownHappeningException If the string value is not supported
     *                                    by the implementation.
     */

    /* wbeebee- "If we need an external event, I'll make it a language primitive and wield it onto the VM"*/
    public void bindTo(String happening) throws UnknownHappeningException {}

    /** Create a <code>ReleaseParameters</code> block appropriate
     *  to the timing characteristics of the event. The default is
     *  the most pessimistic: <code>AperiodicParameters</code>.
     *  This is typically called by code that is setting up a
     *  handler for this event that will fill in the parts of the
     *  release parameters for which it has values, e.g., cost.
     *
     *  @return A new <code>ReleaseParameters</code> object.
     */
    public ReleaseParameters createReleaseParameters() {
	return new AperiodicParameters(null, null, null, null);
    }

    /** Fire this instance of <code>AsyncEvent</code>. The <code>run()</code>
     *  methods of intances of <code>AsyncEventHandler</code> associated with
     *  this event will be made raedy to run.
     */
    public void fire() {
	for(int i = 0; i < MAX_HANDLERS; i++){
	    if(handlerMap[i]){
		//		handlers[i].fire();
		// TODO: There's more to do here, or at least check if fire() is what we want to do.  
	    }
	}
    }

    /** Returns true if and only if the handler given as the parameter is
     *  associated with <code>this</code>.
     *
     *  @param handler The handler to be tested to determine if it is
     *                 associated with <code>this</code>.
     *  @return True if the parameter is associated with <code>this</code>.
     *          False, if <code>target</code> is null or the parameter is
     *          not associated with <code>this</code>.
     */
    public boolean handledBy(AsyncEventHandler handler) {
	if(handler == null) return false;
	for(int i = 0; i < MAX_HANDLERS; i++){
	    if(handler.equals(handlers[i]) && handlerMap[i]){
		return true;
	    }
	}
	return false;
    }

    /** Remove a handler from the set associated with this event.
     *
     *  @param handler The handler to be disassociated from <code>this</code>.
     *                 If null nothing happens. If not already associated with
     *                 this then nothing happens.
     */
    public void removeHandler(AsyncEventHandler handler) {
	if(handler == null) return;
	for(int i = 0; i < MAX_HANDLERS; i++){
	    if(handler.equals(handlers[i]) && handlerMap[i]){
		handlerMap[i] = false;
		handlers[i] = null;
	    }
	}
    }

    /** Associate a new handler with this event, removing all existing handlers.
     *
     *  @param handler The new instance of <code>AsyncEventHandler</code> to be
     *                 associated with this. If <code>handler</code> is null then
     *                 no handler will be associated with this (i.e., remove all
     *                 handlers).
     */
    public void setHandler(AsyncEventHandler handler) {
	for(int i = 0; i < MAX_HANDLERS; i++){
	    handlerMap[i] = false;
	    handlers[i] = null;
	}
	handlers[0] = handler;
    }

    /** Removes a binding to an external event, a <i>happening</i>. The meaningful
     *  values of <code>happening</code> are implementation dependent.
     *
     *  @param happening An implementation dependent value representing some external
     *                   event to which this instance of <code>AsyncEvent</code>
     *                   is bound.
     *  @throws UnknownHappeningException If this intance of <code>AsyncEvent</code>
     *                                    is not bound to the given <code>happening</code>
     *                                    or the given <code>java.lang.String</code> value
     *                                    is not supported by the implementation.
     */

    /* wbeebee- "If we need an external event, I'll make it a language primitive and wield it onto the VM"*/
    public void unbindTo(String happening)
	throws UnknownHappeningException {}
}
