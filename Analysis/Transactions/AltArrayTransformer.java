// AltArrayTransformer.java, created Sun Dec  7 04:04:22 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Collections.SnapshotIterator;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>AltArrayTransformer</code> substitutes an alternative implementation
 * of arrays.  These alternative implementations are designed to reduce the
 * maximum object size, typically.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AltArrayTransformer.java,v 1.1.2.1 2003-12-08 04:20:58 cananian Exp $
 */
// xxx might have trouble at native method boundary.
// xxx add magic to de/re-array-ify objects there?
public class AltArrayTransformer extends MethodMutator<Quad> {
    static final String ALTARRAY_PKG = "harpoon.Runtime.AltArray";
    final Linker linker;
    final String which_impl;

    private class ArrayBundle {
	public final HClass implClass, componentType;
	public final HMethod getMethod, setMethod;
	public final HMethod lengthMethod, componentMethod;
	public final HConstructor newMethod;
	ArrayBundle(HClass implClass, HClass componentType) {
	    this.implClass = implClass;
	    this.componentType = componentType;
	    this.getMethod = implClass.getMethod
		("get", new HClass[] { HClass.Int });
	    this.setMethod = implClass.getMethod
		("set", new HClass[] { HClass.Int, componentType });
	    this.lengthMethod = implClass.getMethod
		("length", new HClass[0]);
	    HClass basicType = componentType.isPrimitive() ?
		componentType : linker.forName("java.lang.Object");
	    this.componentMethod = implClass.getMethod
		("componentOf", new HClass[] { basicType });
	    this.newMethod = implClass.getConstructor
		(new HClass[] { HClass.Int });
	}
    }
    final Map<HClass,ArrayBundle> comp2impl =
	new HashMap<HClass,ArrayBundle>();

    public final ArrayBundle comp2impl(HClass componentType) {
	if (!comp2impl.containsKey(componentType)) {
	    String typeName;
	    if (componentType.isPrimitive()) {
		typeName = componentType.getName();
		// capitalize first letter.
		typeName = typeName.substring(0, 1).toUpperCase() +
		    typeName.substring(1);
	    } else
		typeName="Object";
	    HClass implClass = linker.forName(ALTARRAY_PKG+"."+
					      which_impl+typeName);
	    if (componentType.isPrimitive() ||
		componentType.getName().equals("java.lang.Object"))
		comp2impl.put(componentType, new ArrayBundle
			      (implClass, componentType));
	    else {
		/* XXX make a new subclass with type thunks for 'get' */
		/* must respect array type hierarchy */
		/* add thunk for componentOf */
		assert false;
	    }
	}
	return comp2impl.get(componentType);
    }
    public AltArrayTransformer(HCodeFactory parent, Linker linker,
			       String which_impl) {
	super(new CachingCodeFactory(parent));
	this.linker = linker;
	this.which_impl = which_impl;
	// XX convert all callable methods.
	// XX now rewrite signatures of all callable methods
	//    (and their superclasses?)
	// XX now rewrite all fields.
    }
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	HCode<Quad> hc = input.hcode();
	if (ALTARRAY_PKG.equals
	    (hc.getMethod().getDeclaringClass().getPackage()))
	    return hc; // don't transform protected code.
	SCCAnalysis scc = new SCCAnalysis(hc);
	HEADER header = (HEADER) hc.getRootElement();
	FOOTER footer = header.footer();
	QuadFactory qf = header.getFactory();
	Temp retex = new Temp(qf.tempFactory());
	Set<CALL> calls = new HashSet<CALL>();

	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (hc.getElementsI()); it.hasNext(); ) {
	    Quad qq = it.next();
	    Edge in = qq.prevEdge(0), out = qq.nextEdge(0);
	    if (qq instanceof ANEW) {
		ANEW q = (ANEW) qq;
		if (!scc.execMap(q))
		    Quad.replace(q, new CONST(qf, q, q.dst(),
					      null, HClass.Void));
		else {
		    assert q.dimsLength()==1 :
			"ANEW quads must be simplified.";
		    ArrayBundle ab = comp2impl(q.hclass().getComponentType());
		    NEW qN = new NEW(qf, q, q.dst(), ab.implClass);
		    CALL qC = new CALL(qf, q, ab.newMethod,
				       new Temp[] { q.dst(), q.dims(0) },
				       null, retex, false, false, new Temp[0]);
		    Quad.addEdge(in.from(), in.which_succ(), qN, 0);
		    Quad.addEdge(qN, 0, qC, 0);
		    Quad.addEdge(qC, 0, out.to(), out.which_pred());
		    calls.add(qC);
		}
	    } else if (qq instanceof AGET) {
		AGET q = (AGET) qq;
		if (!scc.execMap(q))
		    Quad.replace(q, _CONST(qf, q, q.dst(), q.type()));
		else {
		    HClass componentType = scc.typeMap(q, q.objectref())
			.getComponentType();
		    ArrayBundle ab = comp2impl(componentType);
		    CALL qC = new CALL(qf, q, ab.getMethod,
				       new Temp[] { q.objectref(), q.index() },
				       q.dst(), retex, false, false,
				       new Temp[0]);
		    Quad.addEdge(in.from(), in.which_succ(), qC, 0);
		    Quad.addEdge(qC, 0, out.to(), out.which_pred());
		    calls.add(qC);
		}
	    } else if (qq instanceof ASET) {
		ASET q = (ASET) qq;
		if (!scc.execMap(q))
		    q.remove();
		else {
		    HClass componentType = scc.typeMap(q, q.objectref())
			.getComponentType();
		    ArrayBundle ab = comp2impl(componentType);
		    CALL qC = new CALL(qf, q, ab.setMethod,
				       new Temp[] { q.objectref(), q.index(),
						    q.src() },
				       null, retex, false, false, new Temp[0]);
		    Quad.addEdge(in.from(), in.which_succ(), qC, 0);
		    Quad.addEdge(qC, 0, out.to(), out.which_pred());
		    calls.add(qC);
		}
	    } else if (qq instanceof ALENGTH) {
		ALENGTH q = (ALENGTH) qq;
		if (!scc.execMap(q))
		    Quad.replace(q, new CONST(qf, q, q.dst(),
					      new Integer(0), HClass.Int));
		else {
		    HClass componentType = scc.typeMap(q, q.objectref())
			.getComponentType();
		    ArrayBundle ab = comp2impl(componentType);
		    CALL qC = new CALL(qf, q, ab.lengthMethod,
				       new Temp[] { q.objectref() },
				       q.dst(), retex, false, false,
				       new Temp[0]);
		    Quad.addEdge(in.from(), in.which_succ(), qC, 0);
		    Quad.addEdge(qC, 0, out.to(), out.which_pred());
		    calls.add(qC);
		}
	    } else if (qq instanceof INSTANCEOF) {
		INSTANCEOF q = (INSTANCEOF) qq;
		if (scc.execMap(q) && q.hclass().isArray()) {
		    ArrayBundle ab = comp2impl(q.hclass().getComponentType());
		    Quad.replace(q, new INSTANCEOF(qf, q, q.dst(), q.src(),
						   ab.implClass));
		}
	    } else if (qq instanceof COMPONENTOF) {
		COMPONENTOF q = (COMPONENTOF) qq;
		if (!scc.execMap(q))
		    Quad.replace(q, new CONST(qf, q, q.dst(),
					      new Integer(0), HClass.Int));
		else {
		    HClass componentType = scc.typeMap(q, q.arrayref())
			.getComponentType();
		    ArrayBundle ab = comp2impl(componentType);
		    CALL qC = new CALL(qf, q, ab.componentMethod,
				       new Temp[] { q.arrayref(),
						    q.objectref() },
				       q.dst(), retex, false, false,
				       new Temp[0]);
		    Quad.addEdge(in.from(), in.which_succ(), qC, 0);
		    Quad.addEdge(qC, 0, out.to(), out.which_pred());
		    calls.add(qC);
		}
	    }
	}
	// XXX now add edges to PHI-THROW-FOOTER for all in 'calls' set.
	return hc;
    }

    // helper method
    private static CONST _CONST(QuadFactory qf, Quad source,
				Temp dst, HClass type) {
	if (!type.isPrimitive())
	    return new CONST(qf, source, dst, null, HClass.Void);
	else if (type==HClass.Boolean || type==HClass.Byte ||
		 type==HClass.Short || type==HClass.Char ||
		 type==HClass.Int)
	    return new CONST(qf, source, dst, new Integer(0), HClass.Int);
	else if (type==HClass.Long)
	    return new CONST(qf, source, dst, new Long(0), HClass.Long);
	else if (type==HClass.Float)
	    return new CONST(qf, source, dst, new Float(0), HClass.Float);
	else if (type==HClass.Double)
	    return new CONST(qf, source, dst, new Double(0), HClass.Double);
	assert false;
	return null;
    }
}
