
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

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.SerializableCodeFactory;

import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.NEW;
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
import harpoon.Temp.Temp;

import harpoon.Util.Util;

/**
 * <code>PreallocOpt</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PreallocOpt.java,v 1.15 2003-02-24 15:32:26 wbeebee Exp $
 */
public abstract class PreallocOpt {

    /** If <code>true</code>, the compiler uses the static memory
	pre-allocation optimization (via
	<code>IncompatibilityAnalysis</code>.  Default is
	<code>false</code>. */
    public static boolean PREALLOC_OPT = false;

    /** Set this to true only if you use the hacked BDW GC version
        that has "GC_malloc_prealloc".  */
    public static boolean HACKED_GC = false;
    static {
	if (HACKED_GC) {
	    System.out.println("HACKED_GC on");
	}
    }

    /** Map used by the optimization: assigns to each static field the
	size of the pre-allocated memory chunk that that field points
	to (at runtime). */
    private static Map/*<HField,Integer>*/ prealloc_field2size;

    /** Name of the wrapper class for the static fields pointing to
	pre-allocated memory chunks. */
    private static final String PREALLOC_MEM_CLASS_NAME =
	"harpoon.Runtime.PreallocOpt.PreallocatedMemory";

    /** Root for the names of the static fields that point (at
	runtime) to the pre-allocated memory chunks. */
    private static final String FIELD_ROOT_NAME = "preallocmem_";

    /** Name of the method that preallocates the memory chunks and
        initializes the static fields to point to them. */
    private static final String INIT_FIELDS_METHOD_NAME = "initFields";


    /** Adds to the set of roots the classes/methods that are called
	by the runtime when the preallocation optimization is used.

	@param roots set of roots
	@param linker linker used to get classes */
    public static void updateRoots(Set roots, Linker linker) {
	roots.add(getInitMethod(linker));
    }

    // returns the handle of the method that preallocates memory
    private static HMethod getInitMethod(Linker linker) {
	return
	    linker.forName(PreallocOpt.PREALLOC_MEM_CLASS_NAME).
	    getMethod(PreallocOpt.INIT_FIELDS_METHOD_NAME,
		      new HClass[0]);
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
	non-<code>null</code>, the method executes the
	<code>Incompatibility Analysis</code>, prints some static and
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

	System.out.println("preallocAnalysis: " + hcf.getCodeName());

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

	PreallocOpt.prealloc_field2size = new HashMap();
	addFields(linker, ia, frame, 
		  PreallocOpt.prealloc_field2size, as);

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


    private static void addFields
	(Linker linker, IncompatibilityAnalysis ia, Frame frame,
	 Map field2size, AllocationStatistics as) {

	HClass pam = linker.forName(PREALLOC_MEM_CLASS_NAME);
	HClassMutator mutator = pam.getMutator();
	// This can be improved by directly creating an HField.
	// Instead, we use the easiest trick: we create fields by using
	// a predefined field as a pattern.
	HField pattern = pam.getDeclaredField("field_pattern");
	int k = 0;

	for(Iterator it = ia.getCompatibleClasses().iterator();
	    it.hasNext(); k++) {
	    Collection cc = (Collection) it.next();
	    HField f = mutator.addDeclaredField(FIELD_ROOT_NAME + k, pattern);
	    field2size.put(f, new Integer(sizeForCompatClass(frame, cc)));
	    for(Iterator it_new = cc.iterator(); it_new.hasNext(); ) {
		NEW site = (NEW) it_new.next();
		
		QuadSSI codeSSI = (QuadSSI) site.getFactory().getParent();
		NEW siteNoSSA = (NEW) codeSSI.getQuadMapSSI2NoSSA().get(site);

		setAllocationProperties(site, f, as.allocID(siteNoSSA));
	    }
	}

	System.out.println("PreallocOpt: " + k + " static field(s) generated");
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

    public static Map ap2id = new HashMap();

    private static void setAllocationProperties(NEW qn, HField f, int id) {
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
	    new PreallocAP(f, formerAP);
	aim.associate((HCodeElement) qn, ap);

	ap2id.put(ap, new Integer(id));
    }


    private static class PreallocAP extends ChainedAllocationProperties {
	public PreallocAP(HField hfield, AllocationProperties formerAP) {
	    super(formerAP);
	    this.hfield = hfield;
	}
	private HField  hfield;
	public  Temp    allocationHeap() { return null; }
	public  boolean canBeStackAllocated() { return false; }
	public  boolean canBeThreadAllocated() { return false; }
	public  boolean makeHeap() { return false; }
	public  HField  getMemoryChunkField() { return hfield; }
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
	return
	    Canonicalize.codeFactory
	    (new AddMemoryPreallocation
	     (hcf, getInitMethod(linker),
	      PreallocOpt.prealloc_field2size, frame));
    }
}
