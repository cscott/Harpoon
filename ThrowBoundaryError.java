// ThrowBoundaryError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ThrowBoundaryError</code> is thrown by
 *  <code>public void enter(java.lang.Runnable logic)</code>
 *  when a <code>java.lang.Throwable</code> allocated from memory
 *  that is not usable in the surrounding scope tries to propagate
 *  out of the scope of the <code>public void enter(java.lang.Runnable)</code>.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ThrowBoundaryError extends java.lang.Error {

    /** A constructor for <code>ThrowBoundaryError</code>. 
     */
    public ThrowBoundaryError() {
	super();
    }

    /** A descriptive constructor for <code>ThrowBoundaryError</code>.
     */
    public ThrowBoundaryError(String description) {
	super(description);
    }
};
