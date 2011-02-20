// DefaultPhysicalMemoryFactory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>DefaultPhysicalMemoryFactory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class DefaultPhysicalMemoryFactory {
    private static PhysicalMemoryFactory pmf = null;

    /** */

    public static PhysicalMemoryFactory instance() {
	if (pmf == null) {
	    pmf = new PhysicalMemoryFactory();
	}
	return pmf;
    }
}
