// Stats.java, created by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Util.Util;

class Stats {
  private static long actualChecks = 0;
  private static long potentialChecks = 0;
  private static long memAreaLoads = 0;
  private static long numQuads = 0;
  private static long numQuadsOut = 0;
  private static long startAnalysis = 0;
  private static long startPointerAnalysis = 0;
  private static long startRealtime = 0;
  private static long totalTimeSpent = 0;
  private static long analysisTimeSpent = 0;
  private static long pointerAnalysisTimeSpent = 0;
  private static long blessedObjects = 0;
  private static long blessedArrayObjects = 0;

  static void addActualMemCheck() {
    actualChecks++;
  }

  static void addPotentialMemCheck() {
    potentialChecks++;
  }

  static void addMemAreaLoad() {
    memAreaLoads++;
  }

  static void addQuads(long quads) {
    numQuads += quads;
  }

  static void addQuadsOut(long quads) {
    numQuadsOut += quads;
  }

  static void addBlessedObject() {
    blessedObjects++;
  }

  static void addBlessedArrayObject() {
    blessedArrayObjects++;
  }
 
  static void analysisBegin() {
    Util.assert(startAnalysis == 0, "Two analysisBegin() without an analysisEnd()");
    startAnalysis = System.currentTimeMillis();
  }
  
  static void analysisEnd() {
    analysisTimeSpent += System.currentTimeMillis() - startAnalysis;
    startAnalysis = 0;
  }

  static void pointerAnalysisBegin() {
    Util.assert(startPointerAnalysis == 0, "Two pointerAnalysisBegin() without a pointerAnalysisEnd()");
    startPointerAnalysis = System.currentTimeMillis();
  }
 
  static void pointerAnalysisEnd() {
    pointerAnalysisTimeSpent += System.currentTimeMillis() - startPointerAnalysis;
    startPointerAnalysis = 0;
  }

  static void realtimeBegin() {
    Util.assert(startRealtime == 0, "Two realtimeBegin() without a realtimeEnd()");
    startRealtime = System.currentTimeMillis();
  }

  static void realtimeEnd() {
    totalTimeSpent += System.currentTimeMillis() - startRealtime;
    startRealtime = 0;
  }

  static void print() {
    System.out.println("-----------------------------------------------------");
    System.out.println("Realtime Java static analysis statistics:");
    System.out.println("                          Before    After   % Savings");
    System.out.print("  Memory access checks: "+(actualChecks+potentialChecks)+
                       "   "+actualChecks+"   ");
    if (actualChecks+potentialChecks>0) {
      System.out.print((potentialChecks/(actualChecks+potentialChecks))*100.0);
    }
    System.out.println();
    System.out.println("  memArea loads: "+memAreaLoads);
    System.out.println("  blessed objects: "+blessedObjects);
    System.out.println("  blessed array objects: "+blessedArrayObjects);
    System.out.println();
    System.out.println("Total time spent adding Realtime support: "+(totalTimeSpent/1000.0)+" s");
    System.out.println("  Time spent in analysis to remove checks: "+(analysisTimeSpent/1000.0)+" s");
    System.out.println("  Time spent in PointerAnalysis: "+(pointerAnalysisTimeSpent/1000.0)+" s");
    System.out.println();
    System.out.print("Number of quads in: "+numQuads+" out: "+numQuadsOut);
    System.out.print(" out-in: "+(numQuadsOut-numQuads));
    if (numQuads>0) {
      System.out.println(" %bloat: "+((((numQuadsOut-numQuads)*1.0)/(numQuads*1.0))*100.0));
    }
    System.out.println("-----------------------------------------------------");
  }


}
