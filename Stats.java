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

    private static long HEAP_TO_HEAP = 0;
    private static long HEAP_TO_SCOPE = 0;
    private static long HEAP_TO_IMMORTAL = 0;
    private static long SCOPE_TO_HEAP = 0;
    private static long SCOPE_TO_SCOPE = 0;
    private static long SCOPE_TO_IMMORTAL = 0;
    private static long IMMORTAL_TO_HEAP = 0;
    private static long IMMORTAL_TO_SCOPE = 0;
    private static long IMMORTAL_TO_IMMORTAL = 0;
    private static long NEW_HEAP = 0;
    private static long NEW_SCOPE = 0;
    private static long NEW_IMMORTAL = 0;
    private static long NEW_ARRAY_HEAP = 0;
    private static long NEW_ARRAY_SCOPE = 0;
    private static long NEW_ARRAY_IMMORTAL = 0;

    /** */

    public final static void addCheck(MemoryArea from,
				      MemoryArea to) {
	if (from.heap) {
	    if (to.heap) {
		HEAP_TO_HEAP++;
	    } else if (to.scoped) {
		HEAP_TO_SCOPE++;
	    } else {
		HEAP_TO_IMMORTAL++;
	    }
	} else if (from.scoped) {
	    if (to.heap) {
		SCOPE_TO_HEAP++;
	    } else if (to.scoped) {
		SCOPE_TO_SCOPE++;
	    } else {
		SCOPE_TO_IMMORTAL++;
	    }
	} else {
	    if (to.heap) {
		IMMORTAL_TO_HEAP++;
	    } else if (to.scoped) {
		IMMORTAL_TO_SCOPE++;
	    } else {
		IMMORTAL_TO_IMMORTAL++;
	    }
	}
	accessChecks++;	
    }

    /** */

    public final static void addNewObject(MemoryArea to) {
	if (to.heap) {
	    NEW_HEAP++;
	} else if (to.scoped) {
	    NEW_SCOPE++;
	} else {
	    NEW_IMMORTAL++;
	}
	newObjects++;
    }
    
    /** */

    public final static void addNewArrayObject(MemoryArea to) {
	if (to.heap) {
	    NEW_ARRAY_HEAP++;
	} else if (to.scoped) {
	    NEW_ARRAY_SCOPE++;
	} else {
	    NEW_ARRAY_IMMORTAL++;
	}
	newArrayObjects++;
    }
    
    /** */

    public final static void print() {
	System.err.println("-------------------------------------");
	System.err.println("Dynamic statistics for Realtime Java:");
	System.err.println("Number of access checks: " + accessChecks);
	System.err.println("Number of objects created: " + newObjects);
	System.err.println("Number of array objects created: " + 
			   newArrayObjects);
	System.err.println("-------------------------------------");
	
	System.err.println("Checks:");
	System.err.println("  Heap     -> Heap:    " + HEAP_TO_HEAP);	
	System.err.println("  Heap     -> Scope:   " + HEAP_TO_SCOPE);
	System.err.println("  Heap     -> Immortal:" + HEAP_TO_IMMORTAL);
	System.err.println("  Scope    -> Heap:    " + SCOPE_TO_HEAP);
	System.err.println("  Scope    -> Scope:   " + SCOPE_TO_SCOPE);
	System.err.println("  Scope    -> Immortal:" + SCOPE_TO_IMMORTAL);
	System.err.println("  Immortal -> Heap:    " + IMMORTAL_TO_HEAP);
	System.err.println("  Immortal -> Scope:   " + IMMORTAL_TO_SCOPE);
	System.err.println("  Immortal -> Immortal:" + IMMORTAL_TO_IMMORTAL);
	System.err.println();
	System.err.println("New objects: ");
	System.err.println("  in heap:    " + NEW_HEAP);
	System.err.println("  in scope:   " + NEW_SCOPE);
	System.err.println("  in immortal:" + NEW_IMMORTAL);
	System.err.println();
	System.err.println("New arrays: ");
	System.err.println("  in heap:    " + NEW_ARRAY_HEAP);
	System.err.println("  in scope:   " + NEW_ARRAY_SCOPE);
	System.err.println("  in immortal:" + NEW_ARRAY_IMMORTAL);
	System.err.println("-------------------------------------");
    }
}
