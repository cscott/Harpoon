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
import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.DefaultAllocationInformation;
import harpoon.Analysis.PointerAnalysis.AllocationStatistics;
import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Backend.Generic.Frame;
import harpoon.Temp.Temp;

/**
 * <code>PreallocOpt</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PreallocOpt.java,v 1.9 2002-12-05 00:04:16 salcianu Exp $
 */
public abstract class PreallocOpt {

    /** If <code>true</code>, the compiler uses the static memory
	pre-allocation optimization (via
	<code>IncompatibilityAnalysis</code>.  Default is
	<code>false</code>. */
    public static boolean PREALLOC_OPT = false;

    /** Map used by the optimization: assigns to each field the
        allocation sites that can reuse the pre-allocated memory chunk
        that will be pointed to (at runtime) by the static field. */
    private static Map prealloc_field2classes;

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

	@return QuadSSI code factory; it produces <code>Code</code>s
	where allocation sites that can be pre-allocated have the
	attached <code>PreallocAllocationStrategy</code>. */
    public static HCodeFactory preallocAnalysis
	(Linker linker, HCodeFactory hcf,
	 ClassHierarchy ch, HMethod mainM, Set roots,
	 AllocationStatistics as) {

	System.out.println("preallocAnalysis: " + hcf.getCodeName());

	CachingCodeFactory hcf_nossa = getCachingQuadNoSSA(hcf);

	boolean OLD_FLAG = QuadSSI.KEEP_QUAD_MAP_HACK;
        QuadSSI.KEEP_QUAD_MAP_HACK = true;
	CachingCodeFactory hcf_ssi = 
	    new CachingCodeFactory(QuadSSI.codeFactory(hcf_nossa), true);

	MetaCallGraphImpl.COLL_HACK = true;
	CallGraph cg = buildCallGraph(linker, hcf_nossa, ch, roots);

	// execute Ovy's analysis
	IncompatibilityAnalysis ia = 
	    new IncompatibilityAnalysis(mainM, hcf_ssi, cg);

	if(as != null) {
	    IAStatistics.printStatistics
		(ia, as,
		 cg.callableMethods(),
		 hcf_nossa,
		 /*
		 AllocationStatistics.getAllocs(cg.callableMethods(),
						hcf_nossa),
		 */
		 linker);
	}

	// restore flag (the backend crashes without this ...)
	QuadSSI.KEEP_QUAD_MAP_HACK = OLD_FLAG;

	prealloc_field2classes = new HashMap();
	addFields(linker, ia, hcf_nossa, PreallocOpt.prealloc_field2classes);

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


    // essentially a CachingCodeFactory that ignores all calls to clear()
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
	(Linker linker, IncompatibilityAnalysis ia,
	 HCodeFactory hcf_nossa, Map field2classes) {

	HClass pam = linker.forName(PREALLOC_MEM_CLASS_NAME);
	HClassMutator mutator = pam.getMutator();
	// this can be improved by directly creating an HField
	HField pattern = pam.getDeclaredField("field_pattern");
	int k = 0;

	for(Iterator it = ia.getCompatibleClasses().iterator();
	    it.hasNext(); k++) {
	    Collection coll = (Collection) it.next();
	    HField f = mutator.addDeclaredField(FIELD_ROOT_NAME + k, pattern);
	    field2classes.put(f, allocatedClasses(coll));
	    for(Iterator it_new = coll.iterator(); it_new.hasNext(); )
		setAllocationProperties((NEW) it_new.next(), f);
	}

	System.out.println("PreallocOpt: " + k + " static field(s) generated");
    }


    // Compute the collection of classes that are allocated by the NEW
    // instructions from coll.
    private static Collection allocatedClasses(Collection coll) {
	List classes = new LinkedList();
	for(Iterator it_new = coll.iterator(); it_new.hasNext(); )
	    classes.add(((NEW) it_new.next()).hclass());
	return classes;
    }


    private static void setAllocationProperties(NEW qn, HField f) {
	Code code = qn.getFactory().getParent();
	AllocationInformationMap aim = 
	    (AllocationInformationMap) code.getAllocationInformation();
	if(aim == null) {
	    aim = new MyAllocationInformationMap();
	    code.setAllocationInformation(aim);
	}

	AllocationInformation.AllocationProperties formerAP = aim.query(qn);
	if(formerAP == null)
	    formerAP = DefaultAllocationInformation.SINGLETON.query(qn);

	aim.associate((HCodeElement) qn, new PreallocAP(f, formerAP));

	/*
	System.out.println
	    ("setAllocationProperty for " + 
	     harpoon.Analysis.PointerAnalysis.Debug.code2str(qn));
	*/
    }


    private static class MyAllocationInformationMap
	extends AllocationInformationMap {
	
	public AllocationProperties query(HCodeElement allocationSite) {
	    AllocationProperties ap = super.query(allocationSite);
	    if(ap == null)
		ap = DefaultAllocationInformation.SINGLETON.query
		    (allocationSite);
	    return ap;
	}
    }


    private static class PreallocAP 
	implements AllocationInformation.AllocationProperties {
	
	public PreallocAP
	    (HField hfield,
	     AllocationInformation.AllocationProperties formerAP) {
	    this.hfield           = hfield;
	    this.actualClass      = formerAP.actualClass();
	    this.hasInteriorPointers = formerAP.hasInteriorPointers();
	    this.noSync           = formerAP.noSync();
	    this.setDynamicWBFlag = formerAP.setDynamicWBFlag();
	}

	private HClass  actualClass;
	private boolean hasInteriorPointers;
	private boolean noSync;
	private boolean setDynamicWBFlag;

	public  HClass  actualClass() { return actualClass; }
	public  Temp    allocationHeap() { return null; }
	public  boolean canBeStackAllocated() { return false; }
	public  boolean canBeThreadAllocated() { return false; }
	public  boolean hasInteriorPointers() { return hasInteriorPointers; }
	public  boolean makeHeap() { return false; }
	public  boolean noSync() { return noSync; }
	public  boolean setDynamicWBFlag() { return setDynamicWBFlag; }

	public  HField  getMemoryChunkField() { return hfield; }
	private HField  hfield;
    }


    // adds the code that preallocates memory and initializes the fields
    public static HCodeFactory addMemoryPreallocation
	(Linker linker, HCodeFactory hcf, Frame frame) {
	return
	    Canonicalize.codeFactory
	    (new AddMemoryPreallocation
	     (hcf, getInitMethod(linker),
	      PreallocOpt.prealloc_field2classes, frame));
    }
}
