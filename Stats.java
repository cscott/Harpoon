// Stats.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>Stats</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public final class Stats {
    
    /** */

    private static long accessChecks = 0;

    /** */

    private static long newObjects = 0;

    /** */

    private static long newArrayObjects = 0;
    
    /** */

    private static MemAreaStack checkFroms = new MemAreaStack();

    /** */

    private static MemAreaStack checkTos = new MemAreaStack();

    /** */

    private static MemAreaStack newObjectAreas = new MemAreaStack();

    /** */

    private static MemAreaStack newArrayObjectAreas = new MemAreaStack();

    /** */

    public final static void addCheck(MemoryArea from,
				      MemoryArea to) {
	checkFroms = new MemAreaStack(from, checkFroms);
	checkTos = new MemAreaStack(to, checkTos);
	accessChecks++;	
    }

    /** */

    public final static void addNewObject(MemoryArea to) {
	newObjectAreas = new MemAreaStack(to, newObjectAreas);
	newObjects++;
    }
    
    /** */

    public final static void addNewArrayObject(MemoryArea to) {
	newArrayObjectAreas = new MemAreaStack(to, newArrayObjectAreas);
	newArrayObjects++;
    }
    
    /** */

    public final static void print() {
	System.err.println("-------------------------------------");
	System.err.println("Dynamic statistics for Realtime Java:");
	System.err.println("Number of access checks: " + accessChecks);
	System.err.println("Number of objects blessed: " + newObjects);
	System.err.println("Number of array objects blessed: " + 
			   newArrayObjects);
	System.err.println("-------------------------------------");

	MemAreaStack froms = checkFroms;
	MemAreaStack tos = checkTos;
	while (froms.next != null) {
	    System.err.println("Check from: " + froms.entry.toString() +
			       "to: " + tos.entry.toString());
	    froms = froms.next;
	    tos = tos.next;
	}

	froms = newObjectAreas;
	while (froms.next != null) {
	    System.err.println("New object from: " + 
			       froms.entry.toString());
	    froms = froms.next;
	}

	froms = newArrayObjectAreas;
	while (froms.next != null) {
	    System.err.println("New array object from: " + 
			       froms.entry.toString());
	    froms = froms.next;
	}
    }
}
