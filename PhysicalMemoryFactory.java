// PhysicalMemoryFactory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>PhysicalMemoryFactory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class PhysicalMemoryFactory {

    /** */

    public synchronized Object create(String memoryType,
				      boolean foo, long base, 
				      long size) {
	if (memoryType.equals("scoped")) {
	    return new ScopedPhysicalMemory(base, size);
	} else if (memoryType.equals("immortal")) {
	    return new ImmortalPhysicalMemory(base, size);
	}
	return null;
    }

    /** */

    public Object create(String memoryType, boolean foo, long size) {
	return create(memoryType, foo, 0, size);
    }
}
