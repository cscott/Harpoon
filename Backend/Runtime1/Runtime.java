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
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;

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
 * @version $Id: Runtime.java,v 1.1.2.40 2001-09-20 01:48:26 cananian Exp $
 */
public class Runtime extends harpoon.Backend.Generic.Runtime {
    // The package and subclasses should be able to access these fields. WSB
    final protected Frame frame; 
    final protected HMethod main;
    protected ClassHierarchy ch;
    protected CallGraph cg;
    final protected AllocationStrategy as;
    final protected ObjectBuilder ob;
    protected List staticInitializers;
    private TreeBuilder treeBuilder;
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
	this.ob = (rootOracle == null) ?
	    new harpoon.Backend.Runtime1.ObjectBuilder(this) :
	    new harpoon.Backend.Runtime1.ObjectBuilder(this, rootOracle);
	this.treeBuilder = initTreeBuilder();
    }
    public TreeBuilder getTreeBuilder() { return treeBuilder; }
    public NameMap getNameMap() { return nameMap; }
    public String resourcePath(String basename) {
	return "harpoon/Backend/Runtime1/"+basename;
    }

    public void setCallGraph(CallGraph cg) { this.cg = cg; }
    // this method must be called before (certain methods in) the tree
    // builder are used.
    public void setClassHierarchy(ClassHierarchy ch) {
	this.ch = ch;
	((harpoon.Backend.Runtime1.TreeBuilder) treeBuilder)
	    .setClassHierarchy(ch);
    }
    protected TreeBuilder initTreeBuilder() {
	int align = Integer.parseInt
	    (System.getProperty("harpoon.runtime1.pointer.alignment","0"));
	return new harpoon.Backend.Runtime1.TreeBuilder
	    (this, frame.getLinker(), as, frame.pointersAreLong(), align);
    }

    public HCodeFactory nativeTreeCodeFactory(final HCodeFactory hcf) {
	final HMethod HMobjAclone =
	    frame.getLinker().forDescriptor("[Ljava/lang/Object;")
	    .getMethod("clone", new HClass[0]);
	Util.assert(hcf.getCodeName().endsWith("tree"));
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
	return runtimeCallableMethods(frame.getLinker());
    }
    /** @deprecated Use frame.getRuntime().runtimeCallableMethods() instead */
    public static Collection runtimeCallableMethods(Linker linker) {
	HClass HCobject = linker.forName("java.lang.Object");
	HClass HCsystem = linker.forName("java.lang.System");
	HClass HCstring = linker.forName("java.lang.String");
	HClass HCcharA  = linker.forDescriptor("[C");
	HClass HCthread = linker.forName("java.lang.Thread");
	HClass HCthreadGroup = linker.forName("java.lang.ThreadGroup");
	return Arrays.asList(new Object[] {
	    // implicitly called during startup/shutdown.
	    HCsystem.getMethod("initializeSystemClass", "()V"),
	    HCthread.getConstructor(new HClass[] {
		    HCthreadGroup,
		    linker.forName("java.lang.Runnable"),
		    linker.forName("java.lang.String"),
			}),
	    HCthread.getMethod("exit", new HClass[0]),
	    HCthread.getMethod("getThreadGroup", new HClass[0]),
	    HCthreadGroup.getConstructor(new HClass[0]),
	    HCthreadGroup.getMethod("uncaughtException", new HClass[] {
		    HCthread, linker.forName("java.lang.Throwable")
			}),
	    // this is the actual implementation used for any array
	    // clone method, so hack it into the hierarchy.
	    linker.forDescriptor("[Ljava/lang/Object;")
		.getMethod("clone", new HClass[0]),
	    // jni implementation uses these:
	    linker.forName("java.lang.NoClassDefFoundError")
		.getConstructor(new HClass[] { HCstring }),
	    linker.forName("java.lang.NoSuchMethodError")
		.getConstructor(new HClass[] { HCstring }),
	    linker.forName("java.lang.NoSuchFieldError")
		.getConstructor(new HClass[] { HCstring }),
	    HCstring.getConstructor(new HClass[] { HCcharA }),
	    HCstring.getMethod("length", "()I"),
	    HCstring.getMethod("toCharArray","()[C"),
	    linker.forName("java.lang.OutOfMemoryError")
		.getConstructor(new HClass[] { HCstring }),
	    // runtime's reflection implementation mentions these
	    // (and they are (staticly) instantiated, so reference the
	    //  constructors)
	    linker.forName("java.lang.Class")
		.getConstructor(new HClass[0]),
	    linker.forName("java.lang.reflect.Constructor")
		.getConstructor(new HClass[0]),
	    linker.forName("java.lang.reflect.Field")
		.getConstructor(new HClass[0]),
	    linker.forName("java.lang.reflect.Method")
		.getConstructor(new HClass[0]),
	    // reflection creates these (wrappers for primitive types)
	    // *THESE SHOULD REALLY ONLY BE MARKED CALLABLE*
	    //  when a call to Field.get() is found.  Eventually
	    //  I'll update QuadClassHierarchy to handle these
	    //  sorts of tricky dependencies.
	    linker.forName("java.lang.Boolean")
		.getConstructor(new HClass[] { HClass.Boolean }),
	    linker.forName("java.lang.Byte")
		.getConstructor(new HClass[] { HClass.Byte }),
	    linker.forName("java.lang.Character")
		.getConstructor(new HClass[] { HClass.Char }),
	    linker.forName("java.lang.Short")
		.getConstructor(new HClass[] { HClass.Short }),
	    linker.forName("java.lang.Integer")
		.getConstructor(new HClass[] { HClass.Int }),
	    linker.forName("java.lang.Long")
		.getConstructor(new HClass[] { HClass.Long }),
	    linker.forName("java.lang.Float")
		.getConstructor(new HClass[] { HClass.Float }),
	    linker.forName("java.lang.Double")
		.getConstructor(new HClass[] { HClass.Double }),
	    // FNI_ExceptionDescribe uses this
	    linker.forName("java.lang.Throwable")
		.getMethod("toString", new HClass[0]),

	    // in java.io implementations
	    linker.forName("java.io.IOException") 
		.getConstructor(new HClass[0]),
	    linker.forName("java.io.IOException")
		.getConstructor(new HClass[] { HCstring }),

	    // in java.lang implementations
	    linker.forName("java.lang.ArrayIndexOutOfBoundsException")
		.getConstructor(new HClass[] { HCstring }),
	    linker.forName("java.lang.ArrayStoreException")
		.getConstructor(new HClass[] { HCstring }),
	    linker.forName("java.lang.ClassNotFoundException")
		.getConstructor(new HClass[] { HCstring }),
	    linker.forName("java.lang.InstantiationException") // by Class
		.getConstructor(new HClass[] { HCstring }), // .newInstance()
		/* This is a JDK1.2-and-up method:
	    linker.forName("java.util.Properties")
		.getMethod("setProperty", new HClass[] { HCstring, HCstring }),
		** We use the JDK1.1-and-up version instead: */
	    linker.forName("java.util.Properties")
		.getMethod("put", new HClass[] { HCobject, HCobject }),
		// java.lang.Throwable.printStackTrace0 uses println([C)
		// of whatever object it is passed.  Let's assume that's
		// java.io.PrintStream (for System.out/System.err)
	    linker.forName("java.io.PrintStream")
		.getMethod("println", new HClass[] { HCcharA }),

	    // in java.net implementations
		// in static initializer of java.net.InetAddress:
	    linker.forName("java.net.InetAddressImpl")
		.getConstructor(new HClass[0]),

	    // referenced by name in static initializers for primitive type
	    // wrappers (java.lang.Integer, java.lang.Character, etc)
	    HClass.Boolean, HClass.Byte, HClass.Short, HClass.Int,
	    HClass.Long, HClass.Float, HClass.Double, HClass.Char,
	    HClass.Void, /* referenced by java.lang.Void! */
	    // passed to main()
	    linker.forDescriptor("[Ljava/lang/String;"),
	});
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

    public List classData(HClass hc) {
	freeze();

	// i don't particularly like this solution to generating
	// the needed string constants, but it works.
	harpoon.Backend.Runtime1.TreeBuilder tb =
	    (harpoon.Backend.Runtime1.TreeBuilder) treeBuilder;
	tb.stringSet.removeAll(stringsSeen);
	stringsSeen.addAll(tb.stringSet);
	Set newStrings = new HashSet(tb.stringSet);
	tb.stringSet.clear();

	List r = Arrays.asList(new Data[] {
	    new DataClaz(frame, hc, ch),
	    new DataInterfaceList(frame, hc, ch),
	    new DataStaticFields(frame, hc),
	    new DataStrings(frame, hc, newStrings),
	    new DataInitializers(frame, hc, staticInitializers),
	    new DataJavaMain(frame, hc, main),
	    new DataReflection1(frame, hc, ch),
	    new DataReflection2(frame, hc, ch, frame.pointersAreLong()),
	    new DataReflectionMemberList(frame, hc, ch),
	});
	if (frame.getGCInfo() != null) {
	    r = new java.util.ArrayList(r);
	    r.add(new DataGC(frame, hc));
	}
	return r;
    }
    final Set stringsSeen = new HashSet();
}
