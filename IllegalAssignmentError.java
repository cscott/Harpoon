// IllegalAssignmentError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** The exception thrown on an attempt to make an illegal assignment.
 *  For example, this will be thrown if logic attempts to assign a
 *  reference to an object in <code>ScopedMemory</code> to a field in
 *  an object in <code>ImmortalMemory</code>.
 */
public class IllegalAssignmentError extends Error
    implements java.io.Serializable {

    /** A constructor for <code>IllegalAssignmentError</code>. */
    public IllegalAssignmentError() {
	super();
    }

    /** A descriptive constructor for <code>IllegalAssignmentError</code>. */
    public IllegalAssignmentError(String description) {
	super(description);
    }
};
