// Stats.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>Stats</code> keeps track of Runtime statistics of how many
 *  objects are created, access checks done from which MemoryArea to 
 *  which MemoryArea, etc.  Stats.print() allows you to print out 
 *  this information and must be present right before termination of
 *  any program which you want to collect these statistics from.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public final class Stats {
    
    /** Count how many access checks are done in total. */
    private static long accessChecks = 0;

    /** Count how many new objects are created. */
    private static long newObjects = 0;

    /** Count how many array objects are created. */
    private static long newArrayObjects = 0;
    
    /** How many HeapMemory -> HeapMemory assignments? */
    private static long HEAP_TO_HEAP = 0;

    /** How many HeapMemory -> ScopedMemory assignments? */
    private static long HEAP_TO_SCOPE = 0;

    /** How many HeapMemory -> ImmortalMemory assignments? */
    private static long HEAP_TO_IMMORTAL = 0;

    /** How many ScopedMemory -> HeapMemory assignments? */
    private static long SCOPE_TO_HEAP = 0;

    /** How many ScopedMemory -> ScopedMemory assignments? */
    private static long SCOPE_TO_SCOPE = 0;

    /** How many ScopedMemory -> ImmortalMemory assignments? */
    private static long SCOPE_TO_IMMORTAL = 0;

    /** How many ImmortalMemory -> HeapMemory assignments? */
    private static long IMMORTAL_TO_HEAP = 0;

    /** How many ImmortalMemory -> ScopedMemory assignments? */
    private static long IMMORTAL_TO_SCOPE = 0;

    /** How many ImmortalMemory -> ImmortalMemory assignments? */
    private static long IMMORTAL_TO_IMMORTAL = 0;
    
    /** How many objects were allocated out of HeapMemory? */
    private static long NEW_HEAP = 0;
    
    /** How many objects were allocated out of ScopedMemory? */
    private static long NEW_SCOPE = 0;

    /** How many objects were allocated out of ImmortalMemory? */
    private static long NEW_IMMORTAL = 0;

    /** How many array objects were allocated out of HeapMemory? */
    private static long NEW_ARRAY_HEAP = 0;

    /** How many array objects were allocated out of ScopedMemory? */
    private static long NEW_ARRAY_SCOPE = 0;

    /** How many array objects were allocated out of ImmortalMemory? */
    private static long NEW_ARRAY_IMMORTAL = 0;

    /** How many checks to the heap were there? */
    public static long heapChecks = 0;
    
    /** How many pointers to the heap were dereferenced? */
    public static long heapRefs = 0;

    /** Has anything been added?  If not, it's possible that the user
     *  did not compile with the _STATS compiler option to add Runtime
     *  statistics to the compiled output. 
     */
    private static boolean touched = false;
    
    /** Add an access check from one MemoryArea to another MemoryArea. 
     */
    public final static void addCheck(MemoryArea from,
				      MemoryArea to) {
	touched = true;
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

    /** Add a new object to the statistics for that MemoryArea. 
     */
    public final static void addNewObject(MemoryArea to) {
	touched = true;
	if (to.heap) {
	    NEW_HEAP++;
	} else if (to.scoped) {
	    NEW_SCOPE++;
	} else {
	    NEW_IMMORTAL++;
	}
	newObjects++;
    }
    
    /** Add a new array object to the statistics for that MemoryArea. 
     */
    public final static void addNewArrayObject(MemoryArea to) {
	touched = true;
	if (to.heap) {
	    NEW_ARRAY_HEAP++;
	} else if (to.scoped) {
	    NEW_ARRAY_SCOPE++;
	} else {
	    NEW_ARRAY_IMMORTAL++;
	}
	newArrayObjects++;
    }
    
    /** Print out the statistics for this program.  You must place
     *  Stats.print() at the end of your programs if you want to
     *  display the Runtime statistics of your program.  You also
     *  need to compile with the _STATS option to tell the compiler 
     *  to output calls to the above methods in order to collect these
     *  statistics.  Note that when _STATS is on, you cannot say 
     *  anything about the runtime of the program, because it takes
     *  significant time to collect information about your program.
     */

    public final static void print() {
	if (touched) {
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
	    if ((heapChecks!=0)||(heapRefs!=0)) {
		System.err.println();
		System.err.println("Heap checks: " + heapChecks);
		System.err.println("Heap references: " + heapRefs);
	    }

	    System.err.println("-------------------------------------");
	    
	} else {
	    System.err.println();
	    System.err.println("Did you forget to compile with the -t STATS option?");
	}
    }
}
