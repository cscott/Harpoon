// Realtime.java, created Sun Nov 12 14:42:50 2000 by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import harpoon.Analysis.Quads.ArrayInitRemover;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Tree.Canonicalize;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;

import harpoon.IR.Quads.QuadNoSSA;

import harpoon.Util.HClassUtil;
import harpoon.Util.ParseUtil;
import harpoon.Util.Util;

import harpoon.Backend.Generic.Frame;
/**
 * <code>Realtime</code> is the top-level access point for the rest of the 
 * Harpoon compiler to provide support for the Realtime Java MemoryArea 
 * extensions described in the 
 * <a href=
 *  "http://java.sun.com/aboutJava/communityprocess/first/jsr001/rtj.pdf">
 * Realtime Java Specification</a> and there's also a
 * <a href="http://tao.doc.wustl.edu/rtj/api/index.html">JavaDoc version</a>.
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 * @version $Id: Realtime.java,v 1.8 2002-06-27 20:30:04 wbeebee Exp $
 */

public class Realtime {
	/** Is Realtime JAVA support turned on? */
	public static boolean REALTIME_JAVA = false;
    
	/** Remove tagging when you remove all checks? */
	public static boolean REMOVE_TAGS = true;

	/** Add support for realtime threads */
	public static boolean REALTIME_THREADS = false;

	/** Determine which analysis method to use. */
	public static int ANALYSIS_METHOD = 0;
	/** Very conservative analysis method - keep all checks */
	public static final int SIMPLE = 0;
	/** Pointer analysis to determine whether checks can be removed */
	public static final int CHEESY_POINTER_ANALYSIS = 1;
	/** More complicated pointer analysis to determine which checks to 
	 *  remove. 
	 */
	public static final int REAL_POINTER_ANALYSIS = 2;
	/** Overly aggressive (and wrong!) check removal that removes all checks. 
	 */
	public static final int ALL = 3;

	/** Add code to the executable to enable gathering of runtime statistics. 
	 */
	public static boolean COLLECT_RUNTIME_STATS = false;

	/** Add checks to determine if a <code>NoHeapRealtimeThread</code> is
	 *  touching the heap.
	 */
	public static boolean NOHEAP_CHECKS = false;

	/** Add masking support, but no checks */
	public static boolean NOHEAP_MASK = false;

	/** Add additional information on calls to RTJ_malloc to store information
	 *  about the def. points of all objects which are allocated.
	 */
	public static boolean DEBUG_REF = false;

	/** Configure Realtime Java based on the following command-line options. 
	 */
	public static void configure(String options) {
		String opts = options.toLowerCase();
		System.out.print("RTJ: on, noheap: ");
		REALTIME_JAVA = true;
		if (opts.indexOf("noheap_checks")!=-1) {
			NOHEAP_CHECKS = true;
			System.out.print("checks");
		} else if (opts.indexOf("noheap")!=-1) {
			NOHEAP_MASK = true;
			System.out.print("no checks");
		} else {
			System.out.print("no support");
		}
		System.out.print(", DEBUG_REF: ");
		if (opts.indexOf("debug_ref")!=-1) {
			DEBUG_REF = true;
			System.out.print("yes");
		} else {
			System.out.print("no");
		}		
		System.out.print(", Collect Statistics: ");
		if (opts.indexOf("stats")!=-1) {
	    System.out.print("yes");
	    COLLECT_RUNTIME_STATS = true;
		} else {
	    System.out.print("no");
		}
		System.out.print(", Analysis Method: ");
		if (opts.indexOf("simple")!=-1) {
	    ANALYSIS_METHOD = SIMPLE;
	    System.out.print("simple");
		} else if (opts.indexOf("cheesy")!=-1) {
	    ANALYSIS_METHOD = CHEESY_POINTER_ANALYSIS;
	    System.out.print("cheesy");
		} else if (opts.indexOf("realpa")!=-1) {
	    ANALYSIS_METHOD = REAL_POINTER_ANALYSIS;
	    System.out.print("realpa");
		} else if (opts.indexOf("all")!=-1) {
	    System.out.print("all, Remove Tags: ");
	    if (opts.indexOf("keeptags")!=-1) {
				System.out.print("no");
				REMOVE_TAGS = false;
	    } else {
				System.out.print("yes");
	    }
	    ANALYSIS_METHOD = ALL;
		} else {
			System.out.println();
	    assert false : "Please specify an analysis method.";
		}
		System.out.print(", Realtime threads: ");
		if (opts.indexOf("threads")!=-1) {
			REALTIME_THREADS = true;
			System.out.println("yes");
		} else {
			System.out.println("no");
		}
	}		

	/** Creates a field memoryArea on <code>java.lang.Object</code>.
	 *  Since primitive arrays inherit from <code>java.lang.Object</code>, 
	 *  this catches them as well. 
	 */
	
	public static void setupObject(Linker linker) {
		// special hack to make setupObject idempotent
		// find something better
		if(setupObject_WAS_CALLED) {
	    System.out.println("Warning: setupObject called again -> ignored");
	    return;
		}
		setupObject_WAS_CALLED = true;
		Stats.realtimeBegin();
		// Adds javax.realtime.MemoryArea java.lang.Object.memoryArea
		linker.forName("java.lang.Object").getMutator()
	    .addDeclaredField("memoryArea", 
												linker.forName("javax.realtime.MemoryArea"));
		Stats.realtimeEnd();
	}
	private static boolean setupObject_WAS_CALLED = false;
    
	/** Adds all methods required for RTJ in the rootset, including:
	 *  <ul>
	 *  <li>javax.realtime.RealtimeThread(java.lang.ThreadGroup, 
	 *                                          java.lang.Runnable,
	 *                                          java.lang.String)</li>
	 *  <li>javax.realtime.RealtimeThread.getMemoryArea()</li>
	 *  <li>javax.realtime.RealtimeThread.currentRealtimeThread()</li>
	 *  <li>java.lang.Thread.setPriority(int)</li>
	 *  <li>javax.realtime.MemoryArea.checkAccess(java.lang.Object)</li>
	 *  <li>javax.realtime.MemoryArea.bless(java.lang.Object)</li>
	 *  <li>javax.realtime.MemoryArea.bless(java.lang.Object, int[])</li>
	 *  </ul>
	 */

	private static String resourcePath(String fileName) {
		return "harpoon/Analysis/Realtime/"+fileName+".properties";
	}

	public static Collection getRoots(final Linker linker) {
		final List roots = new ArrayList();
		
		try {
			// Read in all method roots for RTJ base
			ParseUtil.readResource(resourcePath("realtime-method"),
														 new ParseUtil.StringParser() {
				public void parseString(String s) 
					throws ParseUtil.BadLineException {
					roots.add(ParseUtil.parseMethod(linker, s));
				}
			});
			
			// Read in all class roots for RTJ base
			ParseUtil.readResource(resourcePath("realtime-class"),
														 new ParseUtil.StringParser() {
				public void parseString(String s)
					throws ParseUtil.BadLineException {
					roots.add(linker.forDescriptor(s));
				}
			});
			
			
			if (COLLECT_RUNTIME_STATS) {
				// Read in all method roots for calculating object memalloc statistics
				ParseUtil.readResource(resourcePath("stats-method"),
															 new ParseUtil.StringParser() {
					public void parseString(String s) 
						throws ParseUtil.BadLineException {
						roots.add(ParseUtil.parseMethod(linker, s));
					}
				});
			}
			
			if(REALTIME_THREADS) {
				// Read in all method roots for RTJ thread support
				ParseUtil.readResource(resourcePath("thread-method"),
															 new ParseUtil.StringParser() {
					public void parseString(String s) 
						throws ParseUtil.BadLineException {
						roots.add(ParseUtil.parseMethod(linker, s));
					}
				});
			}
		} catch (java.io.IOException e) {
			assert false : "Properties file for RTJ error: "+e;
		}

		return roots;
	}    

	/** Adds realtime support to a block of code using an 
	 *  <code>harpoon.ClassFile.HCodeFactory</code>.  This includes
	 *  all of the pre-analysis modifications:
	 *  <ul>
	 *  <li> Makes sure that all classes that inherited from 
	 *       <code>java.lang.Thread</code> now inherit from 
	 *       <code>javax.realtime.RealtimeThread</code>. </li>
	 *  <li> Makes every new <code>java.lang.Thread</code> into a new 
	 *       <code>javax.realtime.RealtimeThread</code>. </li>
	 *  </ul>
	 */

	public static HCodeFactory setupCode(final Linker linker, 
																			 final ClassHierarchy ch,
																			 final HCodeFactory parent) {
		Stats.realtimeBegin();
		HCodeFactory hcf = Stats.trackQuadsIn(parent);
		ThreadToRealtimeThread.updateClassHierarchy(linker, ch);
		hcf = (new ThreadToRealtimeThread(hcf, linker)).codeFactory();
		if ((!REMOVE_TAGS)||(ANALYSIS_METHOD!=ALL)) {
	    hcf = new ArrayInitRemover(hcf).codeFactory();
		}
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
		case CHEESY_POINTER_ANALYSIS: {
			CachingCodeFactory ccf = 
				new CachingCodeFactory(QuadNoSSA.codeFactory(parent));
			cr = new CheesyPACheckRemoval(linker, ch, ccf, roots);
			break;
		}
		case REAL_POINTER_ANALYSIS: {
			cr = new PACheckRemoval(linker, ch, parent, roots);
			break;
		}
		case ALL: {
			cr = new AllCheckRemoval();
			break;
		}
		default: {	
		    assert false : "No RTJ analysis method specified.";
		}
		}
		Stats.analysisEnd();
		HCodeFactory hcf;
		if (REMOVE_TAGS&&(ANALYSIS_METHOD==ALL)) {
	    hcf = parent;
		} else {
	    hcf = CheckAdder.codeFactory(cr, new AllCheckRemoval(), parent);
		}
		hcf = Stats.trackQuadsOut(hcf);
		Stats.realtimeEnd();
		return hcf;
	}

	/** Add checks to see if a <code>NoHeapRealtimeThread</code>
	 *  illegally touches the heap.
	 */
	public static HCodeFactory addNoHeapChecks(HCodeFactory hcf) {
		if (NOHEAP_CHECKS) {
	    return (new HeapCheckAdder()).codeFactory(hcf);
		}
		return hcf;
	}

	/** Add code to check to see if a realtime thread quanta 
	 *  has passed to determine whether to do a user-thread-level 
	 *  context switch. 
	 */

	public static HCodeFactory addQuantaChecker(HCodeFactory hcf) {
		if (REALTIME_THREADS) {
		    return Canonicalize.codeFactory((new QuantaChecker(hcf))
						    .codeFactory());
		}
		return hcf;
	}


	/** Print statistics about the static analysis and addition of 
	 *  Realtime support. */
    
	public static void printStats() {
		Stats.print();
	}
}
