// RealtimeSecurity.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RealtimeSecurity</code> sets the security policy for real-time
 *  specific issues.  Primarily used to control access to physical memory. 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RealtimeSecurity {
    /** Security policy object for real-time specific issues. Primarily
     *  used to control access to physical memory.
     */

    private static boolean accessPhysical = true;
    private static boolean setFactory = false;
    private static boolean setScheduler = false;
    private static boolean setFilter = true;

    /** Constructor. */
    public RealtimeSecurity() {

    }

    /** Check whether the application is allowed to access physical memory. */
    public void checkAccessPhysical() throws SecurityException
    {
	if (!accessPhysical) {
	    throw new SecurityException("Not allowed to access physical memory.");
	}
    }
    
    /** Check whether the application is allowed to access physical 
     *  memory within the specified range.
     */
    public void checkAccessPhysicalRange(long base, long size) 
	throws SecurityException
    {
	throw new SecurityException("Not allowed to access " +
				    base + ":" + size+ ".");
    }

    public void checkSetFilter() throws SecurityException {
	if (!setFilter) {
	    throw new SecurityException("Not allowed to set filter objects.");
	}
    }
    
    /** Check whether the application is allowed to set the scheduler. */
    public void checkSetScheduler()
	throws SecurityException
    {
	if (!setScheduler) {
	    throw new SecurityException("Not allowed to set the scheduler.");
	}
    }

    // NOT IN SPECS
    /** Check whether the application is allowed to set factory objects. */
    public void checkSetFactory()
	throws SecurityException
    {
	if (!setFactory) {
	    throw new SecurityException("Not allowed to set factory objects.");
	}
    }
}
