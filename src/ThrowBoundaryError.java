// ThrowBoundaryError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** The error thrown by <code>MeoryArea.enter(Runnable)</code> when a
 *  <code>java.lang.Throwable</code> is allocated from memory that is
 *  not usable in the surrounding scope tries to propagate out of the
 *  scope of the <code>MemoryArea.enter(Runnable)</code>.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class ThrowBoundaryError extends Error {

    /** A constructor for <code>ThrowBoundaryError</code>. */
    public ThrowBoundaryError() {
	super();
    }

    /** A descriptive constructor for <code>ThrowBoundaryError</code>.
     *
     *  @param s Description of the error.
     */
    public ThrowBoundaryError(String s) {
	super(s);
    }
}
