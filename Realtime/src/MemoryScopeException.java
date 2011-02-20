// MemoryScopeException.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Thrown if construction of any of the wait-free queues is
 *  attempted with the ends of the queues in incompatible memory areas.
 */
public class MemoryScopeException extends Exception {

    /** A constructor for <code>MemoryScopeException</code>. */
    public MemoryScopeException() {
	super();
    }

    /** A descriptive constructor for <code>MemoryScopeException</code>.
     *
     *  @param s A description of the exception.
     */
    public MemoryScopeException(String s) {
	super(s);
    }
}
