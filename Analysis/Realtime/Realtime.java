// Realtime.java, created by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import harpoon.Analysis.Quads.ArrayInitRemover;
import harpoon.Analysis.ClassHierarchy;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;

import harpoon.Util.Util;

/**
 * <code>Realtime</code> is the top-level access point for the rest of the 
 * Harpoon compiler to provide support for the Realtime Java MemoryArea 
 * extensions described in the 
 * <a href=
 *  "http://java.sun.com/aboutJava/communityprocess/first/jsr001/rtj.pdf">
 * Realtime Java Specification</a>.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Realtime {
    /** Is Realtime JAVA support turned on? */
    public static boolean REALTIME_JAVA = false;
    
    /** Determine which analysis method to use. */
    public static int ANALYSIS_METHOD = 0;
    /** Very conservative analysis method - keep all checks */
    public static final int SIMPLE = 0;
    /** Pointer analysis to determine whether checks can be removed */
    public static final int POINTER_ANALYSIS = 1;


    /** Creates a field memoryArea on <code>java.lang.Object</code>.
     *  Since primitive arrays inherit from <code>java.lang.Object</code>, 
     *  this catches them as well. 
     */
    
    public static void setupObject(Linker linker) {
	Stats.realtimeBegin();
	// Adds realtime.MemoryArea java.lang.Object.memoryArea
	linker.forName("java.lang.Object").getMutator()
	    .addDeclaredField("memoryArea", 
			      linker.forName("realtime.MemoryArea"));
	Stats.realtimeEnd();
    }
    
    /** Adds all methods required for RTJ in the rootset, including:
     *  <ul>
     *  <li><code>realtime.RealtimeThread(java.lang.ThreadGroup, 
     *                                    java.lang.Runnable,
     *                                    java.lang.String)</code></li>
     *  <li><code>realtime.RealtimeThread</code>.getMemoryArea()</li>
     *  <li><code>realtime.RealtimeThread</code>.currentRealtimeThread()</li>
     *  <li><code>java.lang.Thread</code>.setPriority(int)</li>
     *  <li><code>realtime.MemoryArea</code>.checkAccess(java.lang.Object)</li>
     *  <li><code>realtime.MemoryArea</code>.bless(java.lang.Object)</li>
     *  <li><code>realtime.MemoryArea</code>.bless(java.lang.Object, int[])
     *  </li></ul>
     */

    public static Collection getRoots(Linker linker) {
	Collection roots = new Vector();
	HClass realtimeThread = linker.forName("realtime.RealtimeThread");
	roots.add(realtimeThread.getConstructor(new HClass[] {
	    linker.forName("java.lang.ThreadGroup"),
	    linker.forName("java.lang.Runnable"),
	    linker.forName("java.lang.String")}));
	roots.add(realtimeThread
		  .getMethod("getMemoryArea", new HClass[] {}));
	roots.add(realtimeThread
		  .getMethod("currentRealtimeThread", new HClass[] {}));
	
	roots.add(linker.forName("java.lang.Thread")
		  .getMethod("setPriority", 
			     new HClass[] { HClass.Int }));
	
	HClass memoryArea = linker.forName("realtime.MemoryArea");
	HClass object = linker.forName("java.lang.Object");
	roots.add(memoryArea
		  .getMethod("checkAccess", new HClass[] { object }));
	roots.add(memoryArea
		  .getMethod("bless", new HClass[] { object }));
	roots.add(memoryArea
		  .getMethod("bless", new HClass[] { object, 
						     linker.forName("[I") }));
	return roots;
    }
    
    /** Adds realtime support to a block of code using an 
     *  <code>harpoon.ClassFile.HCodeFactory</code>.  This includes
     *  all of the pre-analysis modifications:
     *  <ul>
     *  <li> Makes sure that all classes that inherited from 
     *       <code>java.lang.Thread</code> now inherit from 
     *       <code>realtime.RealtimeThread</code>. </li>
     *  <li> Makes every new <code>java.lang.Thread</code> into a new 
     *       <code>realtime.RealtimeThread</code>. </li>
     *  </ul>
     */

    public static HCodeFactory setupCode(final Linker linker, 
					 final ClassHierarchy ch,
					 final HCodeFactory parent) {
	Stats.realtimeBegin();
	HCodeFactory hcf = Stats.trackQuadsIn(parent);
	ThreadToRealtimeThread.updateClassHierarchy(linker, ch);
	hcf = (new ThreadToRealtimeThread(hcf)).codeFactory();
	hcf = new ArrayInitRemover(hcf).codeFactory();
	hcf = new CachingCodeFactory(hcf);
	Stats.realtimeEnd();
	return hcf;
    }

    /** Adds realtime support to a block of code using an 
     *  <code>harpoon.ClassFile.HCodeFactory</code>.  This includes the
     *  analysis and all of the post-analysis modifications.
     *  <ul>
     *  <li> Attaches the current memory area to every new array and 
     *       new object (sets the field <code>Object.memoryArea</code>).</li>
     *  <li> Wraps checks around <code>harpoon.IR.Quads.SET</code>s and 
     *       <code>harpoon.IR.Quads.ASET</code>s. </li>
     *  </ul>
     */
    
    public static HCodeFactory addChecks(Linker linker, 
					 ClassHierarchy ch, 
					 HCodeFactory parent,
					 Set roots) {
	Stats.realtimeBegin();
	CheckRemoval cr = null;
	Stats.analysisBegin();
	switch (ANALYSIS_METHOD) {
	case SIMPLE: { 
	    cr = new SimpleCheckRemoval();
	    break; 
	}
	case POINTER_ANALYSIS: {
	    cr = new PACheckRemoval(linker, ch, parent, roots);
	    break;
	}
	default: {
	    Util.assert(false, "No RTJ analysis method specified.");
	}
	}
	Stats.analysisEnd();
	HCodeFactory hcf = (new CheckAdder(cr, parent)).codeFactory();
	hcf = Stats.trackQuadsOut(hcf);
	Stats.realtimeEnd();
	return hcf;
    }
    

    /** Print statistics about the static analysis and addition of 
     *  Realtime support. */
    
    public static void printStats() {
	Stats.print();
    }
}




