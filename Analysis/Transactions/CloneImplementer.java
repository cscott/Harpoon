// CloneImplementer.java, created Wed Jan 17 21:15:00 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.DuplicateMemberException;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>CloneImplementer</code> adds synthetic specialized implementations
 * for all clone methods.  This works around the incomplete field type
 * information available to the runtime, as well as making clone() methods
 * a little more amenable to standard analysis.  (OK, the field type
 * information isn't really *incomplete* as much as it is *inefficient to
 * access* in the way we'd like to.)
 * <p>
 * Arguably, this class should belong in the
 * <code>harpoon.Analysis.Quads</code> package, but we'll leave it
 * here until someone other than the Transactions transformation
 * needs it.
 * <p>
 * Implementation details: a specialized method called
 * <code>$clone$()</code> is created in all known classes.  The
 * existing native <code>clone()</code> methods are made non-native
 * and given implementations which redirect to the virtual
 * <code>$clone$()</code> method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CloneImplementer.java,v 1.2 2002-02-25 21:00:09 cananian Exp $
 * @see harpoon.IR.Quads.CloneSynthesizer */
public class CloneImplementer implements HCodeFactory, java.io.Serializable {
    /** CONSTANTS */
    /** Name of usual "clone" method. */
    private final static String CLONE_NAME = "clone";
    /** Name of our synthesized "$clone$" method. */
    private final static String CLONEX_NAME = "$clone$";
    /** Descriptor of all clone methods. */
    private final static String CLONE_DESC = "()Ljava/lang/Object;";
    /** Modifiers of all standard clone methods. */
    private final static int    CLONE_MODS = Modifier.PROTECTED;
    /** Descriptor of java.lang.Object */
    private final static String OBJ_DESC = "Ljava/lang/Object;";
    /** Descriptor of Object[] */
    private final static String OBJARR_DESC = "[Ljava/lang/Object;";
    /** Parent code factory. */
    final HCodeFactory parent;
    /** Representation cache. */
    final Map cache = new HashMap();

     /** Creates a <code>CloneImplementer</code> based on the
      *  given <code>HCodeFactory</code>, which must produce
      *  some QuadSSI form. */
    public CloneImplementer(HCodeFactory parent, Linker l, Set knownClasses) { 
	Util.assert(parent.getCodeName().equals(QuadSSI.codename));
	this.parent = parent;
	HClass HCobject = l.forName("java.lang.Object");
	HClass HCcloneable = l.forName("java.lang.Cloneable");
	/* okay, we need to add clone() methods to all of the knownClasses */
	for (Iterator it=knownClasses.iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    if (hc.isPrimitive() || hc.isInterface()) continue;
	    if (!hc.equals(HCobject) && !hc.isInstanceOf(HCcloneable))
		continue; // non-cloneable objects get the default impl.
	    try {
		HMethod hm =
		    hc.getMutator().addDeclaredMethod(CLONEX_NAME, CLONE_DESC);
		hm.getMutator().setModifiers(CLONE_MODS);
		hm.getMutator().setSynthetic(false);
	    } catch(DuplicateMemberException dme) {
		Util.assert(false, "Can't create "+CLONEX_NAME+" in "+hc);
	    }
	}
	/* 'un-native-ify' standard clone() methods (incl prim arrays)  */
	String descs[] = new String[] {
	    "[Z", "[B", "[S", "[I", "[J", "[F", "[D", "[C",
	    OBJ_DESC, OBJARR_DESC
	};
	for (int i=0; i<descs.length; i++)
	    l.forDescriptor(descs[i]).getMethod(CLONE_NAME, CLONE_DESC)
		.getMutator().removeModifiers(Modifier.NATIVE);
	/* done */
    }
    public String getCodeName() { return parent.getCodeName(); }
    public void clear(HMethod m) { cache.remove(m); parent.clear(m); }
    public HCode convert(HMethod m) {
	// check cache first
	if (cache.containsKey(m)) return (HCode) cache.get(m);
	// now see if we need to synthesize a code for this method.
	if (m.getName().equals(CLONE_NAME) &&
	    m.getDescriptor().equals(CLONE_DESC) &&
	    (m.getDeclaringClass().getDescriptor().equals(OBJ_DESC) ||
	     m.getDeclaringClass().isArray()))
	      return new CloneRedirectCode(m);
	if (m.getName().equals(CLONEX_NAME) &&
	    m.getDescriptor().equals(CLONE_DESC)) {
	    if (m.getDeclaringClass().getDescriptor().equals(OBJ_DESC))
		return new NotCloneableCode(m);
	    else if (m.getDeclaringClass().isArray())
		return new ArrayCloneCode(m);
	    else
		return new ObjectCloneCode(m);
	}
	return parent.convert(m); // not synthetic: use parent's code.
    }
    /** this method just redirects to Object.$clone$() */
    private static class CloneRedirectCode extends QuadSSI {
	CloneRedirectCode(HMethod m) {
	    super(m, null);
	    HMethod hm = m.getDeclaringClass().getLinker()
		.forDescriptor(OBJ_DESC).getMethod(CLONEX_NAME, CLONE_DESC);
	    Temp thisT = new Temp(qf.tempFactory(), "this");
	    Temp retvT = new Temp(qf.tempFactory(), "retval");
	    Temp retxT = new Temp(qf.tempFactory(), "retex");
	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, new Temp[] { thisT }, 1);
	    Quad q2 = new CALL(qf, null, hm, new Temp[] { thisT },
	                       retvT, retxT, true, true, new Temp[0]);
	    Quad q3 = new RETURN(qf, null, retvT);
	    Quad q4 = new THROW(qf, null, retxT);
	    Quad qF = new FOOTER(qf, null, 3);
	    Quad.addEdge(q0, 0, qF, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q2, 1, q4, 0);
	    Quad.addEdge(q3, 0, qF, 1);
	    Quad.addEdge(q4, 0, qF, 2);
	    // done!
	    this.quads = q0;
	}
    }
    /** this method throws a CloneNotSupportedException */
    private static class NotCloneableCode extends QuadSSI {
	NotCloneableCode(HMethod m) {
	    super(m, null);
	    HClass HCcnse = m.getDeclaringClass().getLinker()
		.forName("java.lang.CloneNotSupportedException");
	    HMethod hm = HCcnse.getConstructor(new HClass[0]);
	    Temp thisT = new Temp(qf.tempFactory(), "this");
	    Temp excT0 = new Temp(qf.tempFactory(), "exc");
	    Temp excT1 = new Temp(qf.tempFactory(), "exc");
	    Temp excT2 = new Temp(qf.tempFactory(), "exc");
	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, new Temp[] { thisT }, 1);
	    Quad q2 = new NEW(qf, null, excT0, HCcnse);
	    Quad q3 = new CALL(qf, null, hm, new Temp[] { excT0 }, null,
	                       excT1, false, false, new Temp[0]);
	    Quad q4 = new PHI(qf, null,
			      new Temp[] { excT2 },
			      new Temp[][] { new Temp[] { excT0, excT1 }}, 2);
	    Quad q5 = new THROW(qf, null, excT2);
	    Quad qF = new FOOTER(qf, null, 2);
	    Quad.addEdge(q0, 0, qF, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdges(new Quad[] { q1, q2, q3, q4, q5 });
	    Quad.addEdge(q3, 1, q4, 1);
	    Quad.addEdge(q5, 0, qF, 1);
	    // done!
	    this.quads = q0;
	}
    }
    /* this method implements a specialized array clone operation */
    private static class ArrayCloneCode extends QuadSSI {
	ArrayCloneCode(HMethod m) {
	    //XXX: this is actually pruned-ssi form, since we don't
	    //     add phis and sigmas for thisT and objT.
	    super(m, null);
	    HClass hc = m.getDeclaringClass();
	    HClass hcC = hc.getComponentType();
	    Temp thisT = new Temp(qf.tempFactory(), "this");
	    Temp lenT = new Temp(qf.tempFactory(), "length");
	    Temp objT = new Temp(qf.tempFactory(), "result");
	    Temp indT0 = new Temp(qf.tempFactory(), "index");
	    Temp indT1 = new Temp(qf.tempFactory(), "index");
	    Temp indT2 = new Temp(qf.tempFactory(), "index");
	    Temp indT3 = new Temp(qf.tempFactory(), "index");
	    Temp indT4 = new Temp(qf.tempFactory(), "index");
	    Temp tstT = new Temp(qf.tempFactory(), "test");
	    Temp tmpT = new Temp(qf.tempFactory(), "temp");
	    Temp oneT = new Temp(qf.tempFactory(), "one");
	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, new Temp[] { thisT }, 1);
	    Quad q2 = new ALENGTH(qf, null, lenT, thisT);
	    Quad q3 = new ANEW(qf, null, objT, hc, new Temp[] { lenT });
	    // for (i=0; i<len; i++) ASET(objT, AGET(thisT, i))
	    Quad q4 = new CONST(qf, null, indT0, new Integer(0), HClass.Int);
	    Quad q5 = new PHI(qf, null, // indT1 = phi(indT0, indT4)
			      new Temp[] { indT1 },
			      new Temp[][] { new Temp[] { indT0, indT4 } }, 2);
	    Quad q6 = new OPER(qf, null, Qop.ICMPGT, tstT,
			       new Temp[] { lenT, indT1});
	    Quad q7 = new CJMP(qf, null, tstT, // <indT2, indT3>=sigma(indT1)
			       new Temp[][] { new Temp[] { indT2, indT3 } },
			       new Temp[] { indT1 });
	    Quad q8 = new AGET(qf, null, tmpT, thisT, indT3, hcC);
	    Quad q9 = new ASET(qf, null, objT, indT3, tmpT, hcC);
	    Quad qA = new CONST(qf, null, oneT, new Integer(1), HClass.Int);
	    Quad qB = new OPER(qf, null, Qop.IADD, indT4,
			       new Temp[] { indT3, oneT });
	    Quad qC = new RETURN(qf, null, objT);
	    Quad qF = new FOOTER(qf, null, 2);
	    Quad.addEdge(q0, 0, qF, 0);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdges(new Quad[] { q1, q2, q3, q4, q5, q6, q7, qC });
	    Quad.addEdge(q7, 1, q8, 0);
	    Quad.addEdge(qC, 0, qF, 1);
	    Quad.addEdges(new Quad[] { q8, q9, qA, qB });
	    Quad.addEdge(qB, 0, q5, 1);
	    // done!
	    this.quads = q0;
	}
    }
    /* this method implements a specialized object clone operation */
    private static class ObjectCloneCode extends QuadSSI {
	ObjectCloneCode(HMethod m) {
	    super(m, null);
	    HClass hc = m.getDeclaringClass();
	    Temp thisT = new Temp(qf.tempFactory());
	    Temp objT = new Temp(qf.tempFactory());
	    Quad q0 = new HEADER(qf, null);
	    Quad q1 = new METHOD(qf, null, new Temp[] { thisT }, 1);
	    Quad q2 = new NEW(qf, null, objT, hc);
	    Quad.addEdge(q0, 1, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad qq = q2;
	    for (Iterator it=fields(hc).iterator(); it.hasNext(); ) {
		HField hf = (HField) it.next();
		Temp tT = new Temp(qf.tempFactory());
		Quad q3 = new GET(qf, null, tT, hf, thisT);
		Quad q4 = new SET(qf, null, hf, objT, tT);
		Quad.addEdges(new Quad[] { qq, q3, q4 });
		qq = q4;
	    }
	    Quad q5 = new RETURN(qf, null, objT);
	    Quad qF = new FOOTER(qf, null, 2);
	    Quad.addEdge(qq, 0, q5, 0);
	    Quad.addEdge(q5, 0, qF, 1);
	    Quad.addEdge(q0, 0, qF, 0);
	    // done!
	    this.quads = q0;
	}
	/** Return a <code>List</code> of all fields (including
            private fields of superclasses) belonging to
            <code>HClass</code> <code>hc</code>. */
	private static List fields(HClass hc) {
	    List l = new ArrayList(hc.getFields().length);
	    for (; hc!=null; hc=hc.getSuperclass())
		l.addAll(0, Arrays.asList(hc.getDeclaredFields()));
	    // filter out static fields.
	    for (Iterator it=l.iterator(); it.hasNext(); )
		if (((HField) it.next()).isStatic())
		    it.remove();
	    // done.
	    return l;
	}
    }
}
