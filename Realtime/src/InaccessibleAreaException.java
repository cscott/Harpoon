// InaccessibleAreaException.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** The specified memory area is not above the current
 *  allocation context on the current thread scope stack.
 */
public class InaccessibleAreaException extends Exception {

    /** A constructor for <code>InaccessibleAreaException</code>. */
    public InaccessibleAreaException() {
	super();
    }

    /** A descriptive constructor for <codeInaccessibleAreaException</code>.
     *
     *  @param s Description of the error.
     */
    public InaccessibleAreaException(String s) {
	super(s);
    }
}
