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

    /** How many checks on READ of a base pointer were there? */
    public static long READ_CHECKS = 0;

    /** How many base pointers on READ were actually pointing to the heap? */
    public static long READ_REFS = 0;
    
    /** How many checks on WRITE to a location were there? */
    public static long WRITE_CHECKS = 0;
    
    /** How many checks on WRITE to a location previously pointing to the heap?
     */
    public static long WRITE_REFS = 0;
    
    /** How many checks on NATIVECALL returns were there? */
    public static long NATIVECALL_CHECKS = 0;
    
    /** How many NATIVECALL's returned something that pointed to the heap? */
    public static long NATIVECALL_REFS = 0;

    /** How many checks on CALL returns were there? */
    public static long CALL_CHECKS = 0;

    /** How many CALL's returned something that pointed to the heap? */
    public static long CALL_REFS = 0;

    /** How many checks on METHOD calls were there? */
    public static long METHOD_CHECKS = 0;

    /** How many parameters passed into METHODs were actually pointing to 
     *  the heap? 
     */
    public static long METHOD_REFS = 0;

    /** Should we still be collecting heap reference statistics? */
    public static long COLLECT_HEAP_STATS = 1;

    /** Has anything been added?  If not, it's possible that the user
     *  did not compile with the _STATS compiler option to add Runtime
     *  statistics to the compiled output. 
     */
    private static boolean touched = false;
    
    /** Add an access check from one MemoryArea to another MemoryArea. 
     */
    public synchronized final static void addCheck(MemoryArea from,
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
    public synchronized final static void addNewObject(MemoryArea to) {
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
    public synchronized final static void addNewArrayObject(MemoryArea to) {
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

    public synchronized final static void print() {
        COLLECT_HEAP_STATS = 0;
	if (touched) {
	    NoHeapRealtimeThread.print("-------------------------------------\n");
	    NoHeapRealtimeThread.print("Dynamic statistics for Realtime Java:\n");
	    NoHeapRealtimeThread.print("Number of access checks: ");
	    NoHeapRealtimeThread.print(accessChecks);
	    NoHeapRealtimeThread.print("\nNumber of objects created: ");
	    NoHeapRealtimeThread.print(newObjects);
	    NoHeapRealtimeThread.print("\nNumber of array objects created: ");
	    NoHeapRealtimeThread.print(newArrayObjects);
	    NoHeapRealtimeThread.print("\n-------------------------------------\n");
	    
	    NoHeapRealtimeThread.print("Checks:\n  Heap     -> Heap:    ");
	    NoHeapRealtimeThread.print(HEAP_TO_HEAP);
	    NoHeapRealtimeThread.print("\n  Heap     -> Scope:   ");
	    NoHeapRealtimeThread.print(HEAP_TO_SCOPE);
	    NoHeapRealtimeThread.print("\n  Heap     -> Immortal:");
	    NoHeapRealtimeThread.print(HEAP_TO_IMMORTAL);
	    NoHeapRealtimeThread.print("\n  Scope    -> Heap:    ");
	    NoHeapRealtimeThread.print(SCOPE_TO_HEAP);
	    NoHeapRealtimeThread.print("\n  Scope    -> Scope:   ");
	    NoHeapRealtimeThread.print(SCOPE_TO_SCOPE);
	    NoHeapRealtimeThread.print("\n  Scope    -> Immortal:");
	    NoHeapRealtimeThread.print(SCOPE_TO_IMMORTAL);
	    NoHeapRealtimeThread.print("\n  Immortal -> Heap:    ");
	    NoHeapRealtimeThread.print(IMMORTAL_TO_HEAP);
	    NoHeapRealtimeThread.print("\n  Immortal -> Scope:   ");
	    NoHeapRealtimeThread.print(IMMORTAL_TO_SCOPE);
	    NoHeapRealtimeThread.print("\n  Immortal -> Immortal:");
	    NoHeapRealtimeThread.print(IMMORTAL_TO_IMMORTAL);
	    NoHeapRealtimeThread.print("\n\nNew objects: \n  in heap:    ");
	    NoHeapRealtimeThread.print(NEW_HEAP);
	    NoHeapRealtimeThread.print("\n  in scope:   ");
	    NoHeapRealtimeThread.print(NEW_SCOPE);
	    NoHeapRealtimeThread.print("\n  in immortal:");
	    NoHeapRealtimeThread.print(NEW_IMMORTAL);
	    NoHeapRealtimeThread.print("\n\nNew arrays: ");
	    NoHeapRealtimeThread.print("\n  in heap:    ");
	    NoHeapRealtimeThread.print(NEW_ARRAY_HEAP);
	    NoHeapRealtimeThread.print("\n  in scope:   ");
	    NoHeapRealtimeThread.print(NEW_ARRAY_SCOPE);
	    NoHeapRealtimeThread.print("\n  in immortal:");
	    NoHeapRealtimeThread.print(NEW_ARRAY_IMMORTAL);
	}
	if ((heapChecks!=0)||(heapRefs!=0)) {
	    touched = true;
	    NoHeapRealtimeThread.print("\n-------------------------------------");
	    NoHeapRealtimeThread.print("\nHeap checks: ");
	    NoHeapRealtimeThread.print(heapChecks);
	    NoHeapRealtimeThread.print(" refs: ");
	    NoHeapRealtimeThread.print(heapRefs);
	    NoHeapRealtimeThread.print("\n  Write checks: ");
	    NoHeapRealtimeThread.print(WRITE_CHECKS);
	    NoHeapRealtimeThread.print(" refs: ");
	    NoHeapRealtimeThread.print(WRITE_REFS);
	    NoHeapRealtimeThread.print("\n  Read checks: ");
	    NoHeapRealtimeThread.print(READ_CHECKS);
	    NoHeapRealtimeThread.print(" refs: ");
	    NoHeapRealtimeThread.print(READ_REFS);
	    NoHeapRealtimeThread.print("\n  NATIVECALL checks: ");
	    NoHeapRealtimeThread.print(NATIVECALL_CHECKS);
	    NoHeapRealtimeThread.print(" refs: ");
	    NoHeapRealtimeThread.print(NATIVECALL_REFS);
	    NoHeapRealtimeThread.print("\n  CALL checks: ");
	    NoHeapRealtimeThread.print(CALL_CHECKS);
	    NoHeapRealtimeThread.print(" refs: ");
	    NoHeapRealtimeThread.print(CALL_REFS);
	    NoHeapRealtimeThread.print("\n  METHOD checks: ");
	    NoHeapRealtimeThread.print(METHOD_CHECKS);
	    NoHeapRealtimeThread.print(" refs: ");
	    NoHeapRealtimeThread.print(METHOD_REFS);
	}
	if (touched) {
	    NoHeapRealtimeThread.print("\n-------------------------------------\n");
	    if ((HEAP_TO_HEAP+HEAP_TO_SCOPE+HEAP_TO_IMMORTAL+SCOPE_TO_HEAP+
		 SCOPE_TO_SCOPE+SCOPE_TO_IMMORTAL+IMMORTAL_TO_HEAP+
		 IMMORTAL_TO_SCOPE+IMMORTAL_TO_IMMORTAL)!=accessChecks) {
		NoHeapRealtimeThread.print("Access checks don't add up!\n");
	    }
	    if ((NEW_HEAP+NEW_SCOPE+NEW_IMMORTAL)!=newObjects) {
		NoHeapRealtimeThread.print("New object's don't add up!\n");
	    }
	    if ((NEW_ARRAY_HEAP+NEW_ARRAY_SCOPE+
		 NEW_ARRAY_IMMORTAL)!=newArrayObjects) {
		NoHeapRealtimeThread.print("New array objects don't add up!\n");
	    }
	    if ((WRITE_CHECKS+READ_CHECKS+NATIVECALL_CHECKS+CALL_CHECKS+
		 METHOD_CHECKS)!=heapChecks) {
		NoHeapRealtimeThread.print("Heap checks just don't add up!\n");
	    }
	    if ((WRITE_REFS+READ_REFS+NATIVECALL_REFS+CALL_REFS+
		 METHOD_REFS)!=heapRefs) {
		NoHeapRealtimeThread.print("Heap refs just don't add up!\n");
	    }	    
	} else {
	    NoHeapRealtimeThread.print("Did you forget to compile with the STATS option?\n");
	}
    }
}
