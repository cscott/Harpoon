// ScopedCycleException.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** Never thrown! 
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class ScopedCycleException extends Exception {

    /** A constructor for <code>ScopedCycleException</code>. */
    public ScopedCycleException() {
	super();
    }

    /** A descriptive constructor for <code>ScopedCycleException</code>.
     *
     *  @param s Description of the error.
     */
    public ScopedCycleException(String s) {
	super(s);
    }
}
