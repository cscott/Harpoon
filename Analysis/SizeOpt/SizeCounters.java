// SizeCounters.java, created Tue Jul 10 11:27:01 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.Counters.CounterFactory;
import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.Backend.Analysis.ClassFieldMap;
import harpoon.Backend.Maps.FieldMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import java.util.List;

/**
 * The <code>SizeCounters</code> code factory adds counters for
 * various allocation properties, to aid in determining the
 * effectiveness of the various size optimizations in this
 * package.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SizeCounters.java,v 1.1.2.1 2001-07-10 21:26:10 cananian Exp $
 */
public class SizeCounters extends MethodMutator {
    final FieldMap cfm;
    
    /** Creates a <code>SizeCounters</code>. */
    public SizeCounters(HCodeFactory parent) {
	super(parent);
	// lifted from Runtime1/TreeBuilder.  this is a hack!
	final int WORD_SIZE = 4;
	final int LONG_WORD_SIZE = 8;
	final int POINTER_SIZE = 4;
	this.cfm = new harpoon.Backend.Analysis.ClassFieldMap() {
	    public int fieldSize(HField hf) {
		HClass type = hf.getType();
		return (!type.isPrimitive()) ? POINTER_SIZE :
		    (type==HClass.Double||type==HClass.Long) ? LONG_WORD_SIZE :
		    (type==HClass.Int||type==HClass.Float) ? WORD_SIZE :
		    (type==HClass.Short||type==HClass.Char) ? 2 : 1;
	    }
	    public int fieldAlignment(HField hf) {
		// every field is aligned to its size
		return fieldSize(hf);
	    }
	};
    }
    // lifted from Runtime1/TreeBuilder.  would like to use actual
    // runtime/treebuilder here, but there is something of a chicken-and-egg
    // problem.
    public int objectSize(HClass hc) {
	List l = cfm.fieldList(hc);
	if (l.size()==0) return 0;
	HField lastfield = (HField) l.get(l.size()-1);
	return cfm.fieldOffset(lastfield) + cfm.fieldSize(lastfield);
    }
    public int headerSize(HClass hc) { // hc is ignored
	return 8;//OBJECT_HEADER_SIZE;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	// prevent instrumentation of the instrumentation code. (sigh)
	if ("harpoon.Runtime.Counters".equals
	    (hc.getMethod().getDeclaringClass().getName()))
	    return hc; // skip this class.
	// total allocation counter.
	Visitor v = new Visitor(hc);
	// copy quads into array before visiting so as not to confuse iterator
	Quad[] quads = (Quad[]) hc.getElements();
	for (int i=0; i<quads.length; i++)
	    quads[i].accept(v);
	// done!
	return hc;
    }
    private class Visitor extends QuadVisitor {
	final QuadFactory qf;
	Visitor(HCode hc) { qf=((Quad)hc.getRootElement()).getFactory(); }
	public void visit(Quad q) { /* do nothing */ }
	public void visit(ANEW q) {
	    Edge e = q.prevEdge(0);
	    e = CounterFactory.spliceIncrement(qf, e, "array_count");
	    // code to compute array size and add it to array_size here.
	}
	public void visit(NEW q) {
	    Edge e = q.prevEdge(0);
	    e = CounterFactory.spliceIncrement(qf, e, "object_count");
	    int size = headerSize(q.hclass())+objectSize(q.hclass());
	    e = CounterFactory.spliceIncrement(qf, e, "object_size", size);
	    e = CounterFactory.spliceIncrement(qf, e, "object_size_"+size);
	}
    }
}
