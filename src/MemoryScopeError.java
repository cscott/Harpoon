// MemoryScopeError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>MemoryScopeError</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class MemoryScopeError extends Error {

    /** Constructor, no description. */

    public MemoryScopeError() {
	super();
    }

    /** Constructor with description. */

    public MemoryScopeError(String description) {
	super(description);
    }
}
