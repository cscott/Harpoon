
// PreallocOpt.java, created Tue Nov 26 16:19:50 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Modifier;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.SerializableCodeFactory;

import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.Code;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Analysis.ChainedAllocationProperties;
import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.DefaultAllocationInformationMap;
import harpoon.Analysis.DefaultAllocationInformation;
import harpoon.Instrumentation.AllocationStatistics.AllocationStatistics;
import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import harpoon.Util.Util;

/**
 * <code>PreallocOpt</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PreallocOpt.java,v 1.18 2003-03-16 16:38:42 salcianu Exp $
 */
public abstract class PreallocOpt {

    /** If <code>true</code>, the compiler uses the static memory
	pre-allocation optimization (via
	<code>IncompatibilityAnalysis</code>.  Default is
	<code>false</code>. */
    public static boolean PREALLOC_OPT = false;

    /** Use the incompatibility analysis only to remove
        syncronizations on the objects that can be preallocated (but
        don't actually preallocate them).
	
	Useful when we want to measure the reduction of the memory
	management overhead: the optimized program has to remove syncs
	on the preallocated objects (for complicated reasons,
	inflating them would make it crsah).  Therefore we want the
	normal version to run without syncs on the preallocatable
	objects, too. */
    public static boolean ONLY_SYNC_REMOVAL = false;

    /** If true, we do not preallocate allocation sites whose unique
        identifier is not between <code>lowBound</cde> and
        <code>highBound</code>.  (The unique id is obtained from a
        <code>AllocationStatistics</code> object. */
    public static boolean RANGE_DEBUG = false;
    // used for debugging code
    public static int lowBound  = 0;
    public static int highBound = 200000;

    /** Set this to true only if you use the hacked BDW GC version
        that has "GC_malloc_prealloc".  */
    public static boolean HACKED_GC = true;

    /** Map used by the optimization: assigns to each label the size
	of the pre-allocated memory chunk that that label points to
	(at runtime). */
    private static Map/*<Label,Integer>*/ label2size;
    /** Label for the beginning of the static data that holds the
        pointers to preallocated memory. */
    private static Label beginLabel = new Label("ptr2preallocmem_BEGIN");
    /** Label for the end of the static data that holds the pointers
        to preallocated memory. */
    private static Label endLabel   = new Label("ptr2preallocmem_END");

    /** Root for the names of the labels corresponding to locations
	that point (at runtime) to the pre-allocated memory chunks. */
    private static final String LABEL_ROOT_NAME = "ptr2preallocmem_";

    /** Name of the method that preallocates the memory chunks and
        initializes the static fields to point to them. */
    private static final String INIT_FIELDS_METHOD_NAME = "initFields";


    /** Adds to the set of roots the classes/methods that are called
	by the runtime when the preallocation optimization is used.

	@param roots set of roots
	@param linker linker used to get classes */
    public static void updateRoots(Set roots, Linker linker) {

	System.out.println("\n\nUPDATE ROOTS CALLED!\n");

	initMethod = 
	    linker.forName("java.lang.Object").getMutator().addDeclaredMethod
	    (INIT_FIELDS_METHOD_NAME, "()V");
	initMethod.getMutator().setModifiers
	    (java.lang.reflect.Modifier.STATIC |
	     java.lang.reflect.Modifier.PUBLIC |
	     java.lang.reflect.Modifier.FINAL);

	System.out.println("Added initMethod = \"" + initMethod + "\"");

	roots.add(initMethod);
    }
    // method called at runtime to initialize the preallocated memory
    private static HMethod initMethod = null;


    private static class HCFWithEmptyInitMethod implements HCodeFactory {
	public HCFWithEmptyInitMethod(HCodeFactory parent_hcf) {
	    this.parent_hcf = parent_hcf;
	}
	private final HCodeFactory parent_hcf;
	public void clear(HMethod hm) { parent_hcf.clear(hm); }
	public String getCodeName() { return parent_hcf.getCodeName(); }
	public HCode convert(HMethod hm) {
	    if((initMethod == null) || (hm != initMethod))
		return parent_hcf.convert(hm);

	    /* based on the implementation from InitializerTransform */
	    return new QuadWithTry(initMethod, null) {
		/* constructor */ {
		    Quad q0 = new HEADER(qf, null);
		    Quad q1 = new METHOD(qf, null, new Temp[]{}, 1);
		    Quad q3 = new RETURN(qf, null, null); // no retval
		    Quad q4 = new FOOTER(qf, null, 2);
		    Quad.addEdge(q0, 0, q4, 0);
		    Quad.addEdge(q0, 1, q1, 0);
		    Quad.addEdge(q1, 0, q3, 0);
		    Quad.addEdge(q3, 0, q4, 1);
		    this.quads = q0;
		}
	    };
	}
    };

    /** Returns a code factory that provides an intermediate
	representation for the empty body of the preallocated memory
	initialization.

	@param hcf Code factory providing the code for the rest of the code
	(currently, it has to be quad-with-try).

	@return Code factory identical to the <code>hcf</code>
	parameter, except that it also provides a default, empty body
	for the preallocated memory initilaization. */
    public static HCodeFactory getHCFWithEmptyInitCode(HCodeFactory hcf) {
	assert
	    hcf.getCodeName().equals("quad-with-try") :
	    "hcf has to be quad-with-try, not " + hcf.getCodeName() ;
	return new HCFWithEmptyInitMethod(hcf);
    }

    /** Executes the <code>IncompatibilityAnalysis</code> and creates
	a (QuadSSI) code factory that produces code with the
	allocation properties set to reflect the fact that some
	allocation sites can use pre-allocated memory space.  In
	addition, it adds static fields to the class named by
	<code>PreallocOpt.PREALLOC_MEM_CLASS_NAME</code>; at runtime,
	these fields will point to pre-allocated chunks of memory, one
	for each compatibility class found by the analysis.

	@param linker linker used to get classes

	@param hcf initial code factory; it has to be convertible to a
	QuadSSI code factory.

	@param ch class hierarchy for the program

	@param mainM main method of the program

	@param roots set of roots

	@param as allocation (dynamic) statistics; if
	non-<code>null</code>, the method prints some static and
	dynamic statistics.

	@param frame Backend specific information for compilation
	(data size and data layout details).  Used only for
	statistics; in particular, unused if <code>as</code> is
	<code>null</code>.

	@return QuadSSI code factory; it produces <code>Code</code>s
	where allocation sites that can be pre-allocated have the
	attached <code>PreallocAllocationStrategy</code>. */
    public static HCodeFactory preallocAnalysis
	(Linker linker, HCodeFactory hcf, ClassHierarchy ch, HMethod mainM,
	 Set roots, AllocationStatistics as, Frame frame) {

	if (HACKED_GC) System.out.println("HACKED_GC on");
	System.out.println("preallocAnalysis: " + hcf.getCodeName());
	if(RANGE_DEBUG) {
	    assert as != null : "RANGE_DEBUG requires non-null as.";
	    System.out.println("RANGE = [" + lowBound + "," + highBound + "]");
	}
	
	// The whole mumbo-jumbo with the QuadNoSSA/QuandSSI is due to
	// the fact that some analyses (e.g., smart call graph
	// constructor) work only with QuadNoSSA, while other works
	// with SSI (e.g., incompatibility analysis).  In addition, we
	// really need the intermediate representation (IR) to stay
	// still during the analysis, so no IR cache clearing is
	// allowed.
	CachingCodeFactory hcf_nossa = getCachingQuadNoSSA(hcf);

	boolean OLD_FLAG = QuadSSI.KEEP_QUAD_MAP_HACK;
        QuadSSI.KEEP_QUAD_MAP_HACK = true;

	CachingCodeFactory hcf_ssi = 
	    new CachingCodeFactory(QuadSSI.codeFactory(hcf_nossa), true);

	MetaCallGraphImpl.COLL_HACK = true;
	CallGraph cg = buildCallGraph(linker, hcf_nossa, ch, roots);

	// execute Ovy's analysis
	IncompatibilityAnalysis ia = 
	    new IncompatibilityAnalysis(mainM, hcf_ssi, cg, linker);

	if(as != null)
	    IAStatistics.printStatistics(ia, as, hcf_nossa, linker, frame);

	label2size = new HashMap();
	setAllocationProperties(linker, ia, frame, as);

	// restore flag (the backend crashes without this ...)
	QuadSSI.KEEP_QUAD_MAP_HACK = OLD_FLAG;

	return hcf_ssi;
    }


    private static CachingCodeFactory getCachingQuadNoSSA(HCodeFactory hcf) {
	HCodeFactory hcf_nossa = 
	    hcf.getCodeName().equals(QuadNoSSA.codename) ?
	    hcf : QuadNoSSA.codeFactory(hcf);

	return 
	    ((hcf_nossa instanceof SafeCachingCodeFactory) ?
	     (SafeCachingCodeFactory) hcf_nossa :
	     new SafeCachingCodeFactory(hcf_nossa, true));
    }


    /** A <code>CachingCodeFactory</code> that ignores all calls to
        <code>clear</code> (hence the name). */
    public static class SafeCachingCodeFactory extends CachingCodeFactory {
	public SafeCachingCodeFactory(HCodeFactory hcf, boolean saveCode) {
	    super(hcf, saveCode);
	}
	public void clear(HMethod m)    { /* ignore */ }
    }


    // build a (smart) call graph
    private static CallGraph buildCallGraph
	(Linker linker, CachingCodeFactory hcf_nossa,
	 ClassHierarchy ch, Set roots) {
	Set mroots = new HashSet();
	// filter out things that are not hmethods
        for (Iterator it = roots.iterator(); it.hasNext(); ) {
            Object root = it.next();
            if(root instanceof HMethod) mroots.add(root);
        }

	// now add static initializers;
        for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
            HClass hclass = (HClass) it.next();
            HMethod hm = hclass.getClassInitializer();
            if (hm != null)
                mroots.add(hm);
        }

	MetaCallGraphImpl.COLL_HACK = false;
        return new SmartCallGraph(hcf_nossa, linker, ch, mroots);
    }


    private static void setAllocationProperties
	(Linker linker, IncompatibilityAnalysis ia, Frame frame,
	 AllocationStatistics as) {

	// BDW allocates many objects of the same size.  Therefore, if
	// we allocate several objects of different sizes, the space
	// is several times bigger than if we allocate same size
	// objects.  So, we allocate the same amount of space
	// (maxSize) for all preallocated memory chunks.
	int maxSize = 0;
	for(Iterator it = ia.getCompatibleClasses().iterator();
	    it.hasNext(); ) {
	    Collection cc = (Collection) it.next();
	    int thisSize = sizeForCompatClass(frame, cc);	    
	    maxSize = Math.max(thisSize, maxSize);
	}

	int nbLabels = 0;

	for(Iterator it = ia.getCompatibleClasses().iterator();
	    it.hasNext(); nbLabels++) {
	    Collection cc = (Collection) it.next();

	    Label label = new Label(LABEL_ROOT_NAME + nbLabels);
	    // debug: I think we really want the second line
	    label2size.put(label, new Integer(maxSize));
	    //label2size.put(label, new Integer(sizeForCompatClass(frame,cc)));

	    for(Iterator it_new = cc.iterator(); it_new.hasNext(); ) {
		NEW site = (NEW) it_new.next();
		QuadSSI codeSSI = (QuadSSI) site.getFactory().getParent();
		NEW siteNoSSA = (NEW) codeSSI.getQuadMapSSI2NoSSA().get(site);

		if(hasFinalizer(site)) {
		    System.out.println
			("NO PREALLOC for\t" + Util.code2str(site) +
			 "\tallocates object with finalizer");
		    continue;
		}

		if(!extraCond(site)) {
		    System.out.println
			("\nNO PREALLOC for\t" + Util.code2str(site) +
			 "\textraCond");
		    continue;
		}

		if(RANGE_DEBUG) {
		    int id = as.allocID(siteNoSSA);
		    if((id < lowBound) || (id > highBound)) {
			System.out.println("Skipping prealloc for " + id);
			continue;
		    }
		    System.out.println
			("\nPREALLOCATE: " + id + " \"" + label + "\" " + 
			 Util.code2str(site));
		}

		setAllocationProperties(site, label);
	    }
	}

	System.out.println("PreallocOpt: " + nbLabels + 
			   " label(s) generated; maxSize = " + maxSize);
    }


    // hack to go around some missing things in Ovy's
    // IncompatibilityAnalysis: IA analyzes only the program that
    // is rooted in the main method (no initialization code
    // considered; that code happen to allocate a PrintStream, and
    // some connected objects with it ...)
    // TODO: properly implement Ovy's stuff
    static boolean extraCond(Quad site) {
	String className = ((NEW) site).hclass().getName();
	if(className.equals("java.io.BufferedWriter") ||
	   className.equals("java.io.OutputStreamWriter")) {
	    HMethod enclosing_method = site.getFactory().getMethod();
	    HClass hdeclc = enclosing_method.getDeclaringClass();
	    return ! hdeclc.getName().equals("java.io.PrintStream");
	}
	return true;
    }

    // compute the size of the preallocated chunk of memory that will
    // be used by the allocation sites from the compatibility class cc
    private static int sizeForCompatClass(Frame frame, Collection cc) {
	Runtime runtime = frame.getRuntime();
	int max = -1;
	for(Iterator it_new = cc.iterator(); it_new.hasNext(); ) {
	    HClass hc = ((NEW) it_new.next()).hclass();
	    int size = sizeForClass(runtime, hc);
	    if(size > max) max = size;
	}
	return max;
    }

    // compute the total size occupied by an object of class hclass
    static int sizeForClass(Runtime runtime, HClass hclass) {
	Runtime.TreeBuilder tree_builder = runtime.getTreeBuilder();
	int size =
	    tree_builder.objectSize(hclass) +
	    tree_builder.headerSize(hclass);
	// we allocate only multiples of 4 bytes
	if((size % 4) != 0)
	    size += 4 - (size % 4);
	return size;
    }

    static int sizeForClass(Frame frame, HClass hclass) {
	return sizeForClass(frame.getRuntime(), hclass);
    }

    private static void setAllocationProperties(NEW qn, Label l) {
	Code code = qn.getFactory().getParent();
	AllocationInformationMap aim = 
	    (AllocationInformationMap) code.getAllocationInformation();
	// Make sure there is an AllocationInfomationMap for "code"
	if(aim == null) {
	    aim = new DefaultAllocationInformationMap();
	    code.setAllocationInformation(aim);
	}

	AllocationInformation.AllocationProperties formerAP = aim.query(qn);
	if(formerAP == null)
	    formerAP = DefaultAllocationInformation.SINGLETON.query(qn);

	AllocationInformation.AllocationProperties ap = 
	    new PreallocAP(l, formerAP);
	aim.associate((HCodeElement) qn, ap);
    }


    private static class PreallocAP extends ChainedAllocationProperties {
	public PreallocAP(Label label, AllocationProperties formerAP) {
	    super(formerAP);
	    this.label = label;
	}
	private Label label;
	public  Temp    allocationHeap()       { return null;  }
	public  boolean canBeStackAllocated()  { return false; }
	public  boolean canBeThreadAllocated() { return false; }
	public  boolean makeHeap()             { return false; }
	// pre-allocated data is thread local (see paper) -> no sync!
	public  boolean noSync()               { return true; }
	public  Label   getLabelOfPtrToMemoryChunk() { return label; }
    }


    public static HData getData(HClass hclass, Frame frame) {
	return 
	    new PreallocData(hclass, frame, label2size.keySet(),
			     beginLabel, endLabel);
    }


    /** Add the code that preallocates memory and initializes the
	static fields that point to the preallocated memory chunks.
	
	@param linker Linker for loading classes

	@param hcf    Code factory for the current vesrion of the program
	(without the memory preallocation code).

	@param frame  Backend specific information for compilation
	(data size and data layout details).

	@return A code factory that produces code that already contains the
	memory preallocation code.  */
    public static HCodeFactory addMemoryPreallocation
	(Linker linker, HCodeFactory hcf, Frame frame) {

	assert initMethod != null : 
	    "initMethod is null; forgot to call PreallocOpt.updateRoots?";
 
	return
	    Canonicalize.codeFactory
	    (new AddMemoryPreallocation
	     (hcf, initMethod, label2size, frame, beginLabel, endLabel));
    }

    /** Returns a <code>CodeFactory</code> with the same structure as
        the one returned by <code>addMemoryPreallocation</code>, but
        without any actual memory preallocation.  Useful for timing
        purposes, when we want to insulate ourselves against
        differences due to different chains of code factories. */
    public static HCodeFactory BOGUSaddMemoryPreallocation
	(Linker linker, HCodeFactory hcf, Frame frame) {
	return
	    Canonicalize.codeFactory(hcf);
    }


    /** Checks whether the class allocated by <code>site</code> has a
        finalizer. */
    static boolean hasFinalizer(Quad site) {
	HClass hc = ((NEW) site).hclass();
	HMethod[] methods = hc.getMethods();
	for(int i = 0; i < methods.length; i++) {
	    if(isFinalizer(methods[i]))
		return true;
	}
	return false;
    }

    /** Checks whether the method <code>hm</code> is a finalizer. */
    private static boolean isFinalizer(HMethod hm) {
	return
	    !Modifier.isAbstract(hm.getModifiers()) &&
	    !hm.isStatic() &&
	    (hm.getParameterTypes().length == 0) &&
	    (hm.getReturnType().equals(HClass.Void)) &&
	    hm.getName().equals("finalize") &&
	    // java.lang.Object.finalize() is not a real finalizer
	    !hm.getDeclaringClass().getName().equals("java.lang.Object");
    }
}
