// Stats.java, created Sat Jan 20 23:39:00 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.Quads.QuadCounter;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCodeFactory;

import harpoon.Util.Timer;
import harpoon.Util.Util;

/**
 * <code>Stats</code> calculates statistics for the static analysis 
 * and compilation of RTJ extensions.
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 */

class Stats {
    private static long actualChecks = 0;
    private static long removedChecks = 0;
    private static long memAreaLoads = 0;
    private static long newObjects = 0;
    private static long newArrayObjects = 0;
    private static long newInstanceChecks = 0;

    private static Timer realtimeTimer = new Timer();
    private static Timer analysisTimer = new Timer();

    private static QuadCounter quadsIn = null;
    private static QuadCounter quadsOut = null;

    /** Tallies an actual check placed around an [A]SET. */
      
    static void addActualMemCheck() {
	actualChecks++;
    }
    
    /** Tallies a check that was removed from around an [A]SET. */

    static void addRemovedMemCheck() {
	removedChecks++;
    }
    
    /** Tallies a load of the current MemoryArea. */

    static void addMemAreaLoad() {
	memAreaLoads++;
    }

    /** Tracks the number of <code>Quad</code>s passing through this 
     *  <code>HCodeFactory</code>. 
     */

    static HCodeFactory trackQuadsIn(HCodeFactory parent) {
	return new CachingCodeFactory(quadsIn = new QuadCounter(parent));
    }

    /** Tracks the number of <code>Quad</code>s passing through this 
     *  <code>HCodeFactory</code>. 
     */

    static HCodeFactory trackQuadsOut(HCodeFactory parent) {
	return new CachingCodeFactory(quadsOut = new QuadCounter(parent));
    }

    /** Adds an object that was just <code>NEW</code>'d. */

    static void addNewObject() {
	newObjects++;
    }

    /** Adds an array that was just <code>NEW</code>'d. */
    
    static void addNewArrayObject() {
	newArrayObjects++;
    }
    
    /** Adds a Memory.newInstance() call */

    static void addNewInstance() {
	newInstanceChecks++;
    }

    /** Starts the timer for analysis. */

    static void analysisBegin() {
	analysisTimer.start();
    }
    
    /** Stops the timer for analysis. */

    static void analysisEnd() {
	analysisTimer.stop();
    }
    
    /** Starts the timer for adding RTJ extensions. */

    static void realtimeBegin() {
	realtimeTimer.start();
    }

    /** Stops the timer for adding RTJ extensions. */
    
    static void realtimeEnd() {
	realtimeTimer.stop();
    }
    
    /** Prints out statistics for addition of RTJ extensions. */

    static void print() {
	Util.assert(!analysisTimer.running(), 
		    "AnalysisTimer is still running!");
	Util.assert(!realtimeTimer.running(),
		    "RealtimeTimer is still running!");
	System.out.println("-----------------------------------" +
			   "------------------");
	System.out.println("Realtime Java static analysis statistics:");
	System.out.println("                          " +
			   "Before    After   % Savings");
	System.out.print("  Memory access checks: " +
			 (actualChecks + removedChecks) + "   "
			 + actualChecks + "   ");
	if (actualChecks+removedChecks > 0) {
	    System.out.print((removedChecks/
			      (actualChecks+removedChecks)) * 100.0);
	}
	System.out.println();
	System.out.println("  new instance checks: " + newInstanceChecks);
	System.out.println("  memArea loads: " + memAreaLoads);
	System.out.println("  new objects: " + newObjects);
	System.out.println("  new array objects: " + newArrayObjects);
	System.out.println();
	System.out.println("Total time spent adding Realtime support: "+
			   (realtimeTimer.timeElapsed() / 1000.0) + " s");
	System.out.println("  Time spent in analysis to remove checks: "+
			   (analysisTimer.timeElapsed() / 1000.0) + " s");
	System.out.println();
	if ((quadsIn != null) || (quadsOut != null)) {
	    System.out.print("Number of quads");
	}
	if (quadsIn != null) {
	    System.out.print(" in: " + quadsIn.count());
	}
	if (quadsOut != null) {
	    System.out.print(" out: " + quadsOut.count());
	}
	if ((quadsIn != null) && (quadsOut != null)) {
	    System.out.print(" out-in: " + (quadsOut.count() - 
					    quadsIn.count()));
	    if (quadsIn.count() > 0) {
		System.out.print(" %bloat: " + ((((quadsOut.count() -
						   quadsIn.count()) * 1.0) /
					       (quadsIn.count() * 1.0))
						* 100.0));
	    }
	}
	if ((quadsIn != null) || (quadsOut != null)) {
	    System.out.println();
	}
	System.out.println("-----------------------------------"+
			   "------------------");
    }
}
