// MITViolationException.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** The exception is thrown when minimum interarrival time is violated. */
public class MITViolationException extends Exception {

    /** A constructor for <code>MITViolationException</code>. */
    public MITViolationException() {
	super();
    }

    /** A descriptive constructor for <code>MITViolationException</code>.
     *
     *  @param s Description of the error.
     */
    public MITViolationException(String s) {
	super(s);
    }
}
