// IllegalAssignmentError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>IllegalAssignmentError</code> is thrown when there is an
 *  assignment that could create a dangling reference.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class IllegalAssignmentError extends Error {

    /** Constructor, no description. */

    public IllegalAssignmentError() {
	super();
    }

    /** Constructor with description. */

    public IllegalAssignmentError(String description) {
	super(description);
    }
};
