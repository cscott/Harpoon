// Runtime.java, created Wed Sep  8 14:30:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.ClassDepthMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.Runtime1.ObjectBuilder.RootOracle;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Util.ParseUtil;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>Runtime1.Runtime</code> is a no-frills implementation of the runtime
 * abstract class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Runtime.java,v 1.10 2003-10-03 16:59:24 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Generic.Runtime
    implements java.io.Serializable {

    // The package and subclasses should be able to access these fields. WSB
    final protected Frame frame; 
    final protected HMethod main;
    protected ClassHierarchy ch;
    protected CallGraph cg;
    final protected AllocationStrategy as;
    final protected ObjectBuilder ob;
    protected List staticInitializers;
    private harpoon.Backend.Runtime1.TreeBuilder treeBuilder;
    private NameMap nameMap;
    
    /** Creates a new <code>Runtime1.Runtime</code>. */
    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main, boolean prependUnderscore) {
	this(frame, as, main, prependUnderscore, null);
    }

    /** Creates a new <code>Runtime1.Runtime</code>. */
    public Runtime(Frame frame, AllocationStrategy as,
		   HMethod main,
		   boolean prependUnderscore, RootOracle rootOracle) {
	this.frame = frame;
	this.main = main;
	this.as = as;
	this.nameMap =
	    new harpoon.Backend.Maps.DefaultNameMap(prependUnderscore);
	this.ob = initObjectBuilder(rootOracle);
	this.treeBuilder = initTreeBuilder();
    }
    protected ObjectBuilder initObjectBuilder(RootOracle ro) {
	if (ro==null)
	    return new harpoon.Backend.Runtime1.ObjectBuilder(this);
	return new harpoon.Backend.Runtime1.ObjectBuilder(this, ro);
    }
    public harpoon.Backend.Runtime1.TreeBuilder
	getTreeBuilder() { return treeBuilder; }
    public NameMap getNameMap() { return nameMap; }
    public String resourcePath(String basename) {
	if (Boolean.getBoolean("harpoon.runtime1.sunjdk")) {
	    basename="sunjdk."+basename;
	    configurationSet.add("check_with_sunjdk_needed");
	} else if (Boolean.getBoolean("harpoon.runtime1.minilib")) {
	    basename="minilib."+basename;
	    // minilib uses the classpath native library support.
	    configurationSet.add("check_with_classpath_needed");
	} else
	    configurationSet.add("check_with_classpath_needed");
	return "harpoon/Backend/Runtime1/"+basename;
    }

    public void setCallGraph(CallGraph cg) { this.cg = cg; }
    // this method must be called before (certain methods in) the tree
    // builder are used.
    public void setClassHierarchy(ClassHierarchy ch) {
	this.ch = ch;
	// class and field information may have changed; reset caches.
	treeBuilder = initTreeBuilder();
	// set the treebuilder's class hierarchy.
	treeBuilder.setClassHierarchy(ch);
    }
    protected  harpoon.Backend.Runtime1.TreeBuilder initTreeBuilder() {
	int align = Integer.parseInt
	    (System.getProperty("harpoon.runtime1.pointer.alignment","0"));
	// config-checking --- this property shouldn't change!
	if (align!=0)
	    configurationSet.add("check_with_masked_pointers_needed");
	// more config-checking -- using this tree builder means no
	// claz compression support and no hashlock compression support.
	configurationSet.add("check_with_claz_shrink_not_needed");
	configurationSet.add("check_with_hashlock_shrink_not_needed");
	return new harpoon.Backend.Runtime1.TreeBuilder
	    (this, frame.getLinker(), as, frame.pointersAreLong(), align);
    }

    public HCodeFactory nativeTreeCodeFactory(final HCodeFactory hcf) {
	final HMethod HMobjAclone =
	    frame.getLinker().forDescriptor("[Ljava/lang/Object;")
	    .getMethod("clone", new HClass[0]);
	assert hcf.getCodeName().endsWith("tree");
	return new HCodeFactory() {
	    public String getCodeName() { return hcf.getCodeName(); }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public HCode convert(HMethod m) {
		HCode c = hcf.convert(m);
		// substitute stub for native methods.
		if (c==null && Modifier.isNative(m.getModifiers()))
		    c = new StubCode(m, frame);
		return c;
	    }
	};
    }

    public Collection runtimeCallableMethods() {
	final Linker linker = frame.getLinker();
	String methodResourceName = resourcePath("method-root.properties");
	String classResourceName = resourcePath("class-root.properties");

	// XXX this is a little crufty, as backwards compatibility
	// made this a weird little interface.

	// read in root methods and classes from properties files.
	try {
	    final List result = new ArrayList();
	    // read in method roots
	    ParseUtil.readResource
		(methodResourceName,
		 new ParseUtil.StringParser() {
			 public void parseString(String s)
			     throws ParseUtil.BadLineException {
			     result.add(ParseUtil.parseMethod(linker, s));
			 }
		     });
	    // read in class roots.
	    ParseUtil.readResource
		(classResourceName,
		 new ParseUtil.StringParser() {
			 public void parseString(String s)
			     throws ParseUtil.BadLineException {
			     result.add(linker.forDescriptor(s));
			 }
		     });
	    // done!
	    return result;
	} catch (java.io.IOException ioex) {
	    // this includes BadLineExceptions.  We could just skip the
	    // rest of the file, but the importance of these sets
	    // justifies halting compilation with a RuntimeException.
	    throw new RuntimeException("Can't read roots: "+ioex);
	}
    }

    private synchronized void freeze() {
	if (frozen) return; else frozen = true;

	// finalize static initializers.
	if (Boolean.getBoolean("harpoon.runtime1.order-initializers"))
	    this.staticInitializers =
		new harpoon.Backend.Analysis.InitializerOrdering(ch, cg)
		.sorted;
	else { // when using InitializerTransform, can order randomly:
	    this.staticInitializers = new java.util.ArrayList();
	    for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
		HMethod hm = ((HClass)it.next()).getClassInitializer();
		if (hm!=null) staticInitializers.add(hm);
	    }
	}
    }
    private boolean frozen=false;

    public List<HData> classData(HClass hc) {
	freeze();

	// i don't particularly like this solution to generating
	// the needed string constants, but it works.
	treeBuilder.stringSet.removeAll(stringsSeen);
	stringsSeen.addAll(treeBuilder.stringSet);
	Set<String> newStrings = new HashSet<String>(treeBuilder.stringSet);
	treeBuilder.stringSet.clear();

	List<HData> r = new java.util.ArrayList<HData>(20);
	r.addAll(Arrays.asList(new HData[] {
	    new DataClaz(frame, hc, ch, getExtraClazInfo()),
	    new DataConfigChecker(frame, hc),
	    new DataInterfaceList(frame, hc, ch),
	    new DataStaticFields(frame, hc),
	    new DataStrings(frame, hc, newStrings),
	    new DataInitializers(frame, hc, staticInitializers),
	    new DataJavaMain(frame, hc, main),
	    new DataReflection1(frame, hc, ch),
	    new DataReflection2(frame, hc, ch, frame.pointersAreLong()),
	    new DataReflectionMemberList(frame, hc, ch),
	}));
	if (frame.getGCInfo() != null)
	    r.add(new DataGC(frame, hc));
	return r;
    }
    final Set<String> stringsSeen = new HashSet<String>();

    /** provides a means for a subclass to add extra fields to
     * DataClaz.  A little bit cleaner than trying to subclass
     * DataClaz and hack TreeBuilder to make this work. */
    protected static class ExtraClazInfo {
	/** Return the number of bytes of fields you're planning on sticking
	 *  into the claz structure. */
	public int fields_size() { return 0; }
	/** Here's your chance to insert your stuff. */
	public Stm emit(TreeFactory tf, Frame f, HClass hc, ClassHierarchy ch){
	    return null;
	}
    }
    /** A means for a subclass to set this. */
    protected ExtraClazInfo getExtraClazInfo() { return new ExtraClazInfo(); }
}
