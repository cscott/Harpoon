// Stats.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>Stats</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

final class Stats {
    
    /** */

    private static long accessChecks = 0;

    /** */

    private static long newObjects = 0;

    /** */

    private static long newArrayObjects = 0;
    
    /** */

    final static void addCheck() {
	accessChecks++;
    }

    /** */

    final static void addNewObject() {
	newObjects++;
    }
    
    /** */

    final static void addNewArrayObjects() {
	newArrayObjects++;
    }
    
    /** */

    final static void print() {
	System.out.println("-------------------------------------");
	System.out.println("Dynamic statistics for Realtime Java:");
	System.out.println("Number of access checks: "+accessChecks);
	System.out.println("Number of objects blessed: "+newObjects);
	System.out.println("Number of array objects blessed: "+newArrayObjects);
	System.out.println("-------------------------------------");
    }
}
