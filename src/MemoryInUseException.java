// MemoryInUseException.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Thrown when an attempt is made to allocate a range of
 *  physical or virtual memory that is already in use.
 */
public class MemoryInUseException extends Exception {

    /** A constructor for <code>MemoryInUseException</code>. */
    public MemoryInUseException() {
	super();
    }

    /** A descriptive constructor for <code>MemoryInUseException</code>.
     *
     *  @param s Description of the error.
     */
    public MemoryInUseException(String s) {
	super(s);
    }
}
