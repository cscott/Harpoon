// MZFWidenType.java, created Tue Nov 13 00:23:59 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Util.Default.PairList;
import harpoon.Util.HClassUtil;
import harpoon.Util.Collections.SnapshotIterator;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>MZFWidenType</code> widens the types mentioned in
 * <code>ANEW</code>, <code>INSTANCEOF</code>, and <code>TYPESWITCH</code>
 * quads so that they play nicely with the split types generated by
 * <code>MZFCompressor</code>.  It also widens types mentioned in
 * field and method signatures.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MZFWidenType.java,v 1.6 2002-09-03 15:17:52 cananian Exp $
 */
class MZFWidenType extends MethodMutator<Quad> {
    /** the linker to use */
    final Linker linker;
    /** a map from HClasses to a list of sorted fields; the splitting has
     *  been done in the order of the list. */
    final Map<HClass,List<PairList<HField,Integer>>> listmap;
    /** a map from <code>HField</code>s to the <code>HClass</code> which
     *  eliminates that field. */
    final Map<HField,HClass> field2class;
    /** Creates a <code>MZFWidenType</code>. */
    public MZFWidenType(HCodeFactory hcf,
			Linker linker,
			Map<HClass,List<PairList<HField,Integer>>> listmap,
			Map<HField,HClass> field2class,
			Set<HMethod> callableMethods, Set<HClass> allClasses) {
        super(hcf);
	this.linker = linker;
	this.listmap = listmap;
	this.field2class = field2class;
	// widen parameter and return types of all callable methods.
	// (also, of all interfaces which these callable methods implement,
	//  and all superclass methods which these callable methods override)
	for (Iterator<HMethod> it=allMethods(allClasses).iterator();
	     it.hasNext(); ) {
	    HMethod hm = it.next();
	    hm.getMutator().setReturnType(widen(hm.getReturnType()));
	    HClass[] paramTypes = hm.getParameterTypes();
	    for (int i=0; i<paramTypes.length; i++)
		if (widen(paramTypes[i])!=paramTypes[i])
		    hm.getMutator().setParameterType(i, widen(paramTypes[i]));
	}
	// widen types of all fields
	for (Iterator<HClass> it=allClasses.iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
	    HField[] hfa = hc.getDeclaredFields();
	    for (int i=0; i<hfa.length; i++)
		if (widen(hfa[i].getType())!=hfa[i].getType())
		    hfa[i].getMutator().setType(widen(hfa[i].getType()));
	}
    }
    private Set<HMethod> allMethods(Set<HClass> allClasses) {
	Set<HMethod> result = new HashSet<HMethod>();
	for (Iterator<HClass> it=allClasses.iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
	    result.addAll(Arrays.asList(hc.getMethods()));
	}
	return result;
    }
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	HCode<Quad> hc = input.hcode();
	QuadVisitor qv = new QuadVisitor() {
		public void visit(Quad q) { /* booring. */ }
		public void visit(ANEW q) {
		    Quad.replace
			(q, new ANEW(q.getFactory(), q, q.dst(),
				     widen(q.hclass()), q.dims()));
		}
		public void visit(INSTANCEOF q) {
		    Quad.replace
			(q, new INSTANCEOF(q.getFactory(), q, q.dst(),
					   q.src(), widen(q.hclass())));
		}
		public void visit(TYPESWITCH q) {
		    HClass[] keys = (HClass[]) q.keys().clone();
		    for (int i=0; i<keys.length; i++)
			keys[i] = widen(keys[i]);
		    Quad.replace
			(q, new TYPESWITCH(q.getFactory(), q, q.index(),
					   keys, q.dst(), q.src(),
					   q.hasDefault()));
		}
	    };
	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (hc.getElementsI()); it.hasNext(); )
	    it.next().accept(qv);
	// done.
	return hc;
    }
    HClass widen(HClass hc) {
	if (hc.isPrimitive() || hc.isInterface())
	    return hc;
	if (hc.isArray())
	    return HClassUtil.arrayClass
		(linker, widen(HClassUtil.baseClass(hc)), HClassUtil.dims(hc));
	List<PairList<HField,Integer>> sortedFields = listmap.get(hc);
	if (sortedFields==null) return hc; // not a split class.
	PairList<HField,Integer> lastpair =
	    sortedFields.get(sortedFields.size()-1);
	HField lastF = (HField) lastpair.get(0);
	HClass broadest = field2class.get(lastF);
	assert broadest!=null;
	return broadest;
    }
}
