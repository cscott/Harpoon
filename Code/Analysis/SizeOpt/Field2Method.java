// Field2Method.java, created Sat Nov 10 20:43:46 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.GenericInvertibleMultiMap;
import harpoon.Util.Collections.InvertibleMultiMap;
import harpoon.Util.Collections.SnapshotIterator;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <code>Field2Method</code> code factory converts all <code>GET</code>
 * and <code>SET</code> operations on a given set of fields into calls
 * to accessor getter/setter methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Field2Method.java,v 1.5 2002-09-03 14:43:03 cananian Exp $
 */
public class Field2Method {
    // xxx declarations below *should* be 'invertibleMap' but the
    //     definitions aren't consistent yet.
    /** maps 'getter' methods to the field they get. MUTABLE. */
    private final InvertibleMultiMap<HMethod,HField> _getters =
	new GenericInvertibleMultiMap<HMethod,HField>();
    /** maps 'setter' methods to the field they set. MUTABLE. */
    private final InvertibleMultiMap<HMethod,HField> _setters =
	new GenericInvertibleMultiMap<HMethod,HField>();
    // these are public-visible unmodifiable versions of these.
    /** This maps 'getter' methods to the field they get. */
    public final Map<HMethod,HField> getter2field =
	Collections.unmodifiableMap(_getters);
    /** This maps 'setter' methods to the field they set. */
    public final Map<HMethod,HField> setter2field =
	Collections.unmodifiableMap(_setters);
    /** This maps fields to 'getter' methods. */
    public final Map<HField,HMethod> field2getter =
	Collections.unmodifiableMap(_getters.invert());
    /** This maps fields to 'setter' methods. */
    public final Map<HField,HMethod> field2setter =
	Collections.unmodifiableMap(_setters.invert());
    
    /** Creates a <code>Field2Method</code> code factory which converts
     *  all <code>GET</code> and <code>SET</code> operations on the
     *  fields in the <code>fields2convert</code> <code>Set</code> into
     *  calls to accessor getter/setter methods.  The input may be
     *  in Quad SSI/SSA/NoSSA/RSSx forms.  The output will be in
     *  Quad-SSI form. */
    public Field2Method(final HCodeFactory hcf, Set<HField> fields2convert) {
	// okay, create the appropriate new methods.
	for (Iterator<HField> it=fields2convert.iterator(); it.hasNext(); ) {
	    HField hf = it.next();
	    HClass hc = hf.getDeclaringClass();
	    String name = "$$$"+
		hf.getDeclaringClass().getName()+".."+hf.getName();
	    name = replace(name, "_", "_1");
	    name = replace(name, ".", "_");
	    // TYPE XXX_<classname>_get()
	    HMethod getter = hc.getMutator().addDeclaredMethod
		(name+"$get", "()"+hf.getType().getDescriptor());
	    getter.getMutator().addModifiers(Modifier.PUBLIC);
	    // void XXX_<classname>_set(TYPE val)
	    HMethod setter = hc.getMutator().addDeclaredMethod
		(name+"$set", "("+hf.getType().getDescriptor()+")V");
	    setter.getMutator().addModifiers(Modifier.PUBLIC);
	    // add these to the appropriate maps.
	    _getters.put(getter, hf);
	    _setters.put(setter, hf);
	}
	// Make an HCodeFactory that defines these getters and setters
	HCodeFactory expanded = new HCodeFactory() {
		public HCode convert(HMethod hm) {
		    if (getter2field.containsKey(hm))
			return makeGetter(hm, getCodeName());
		    if (setter2field.containsKey(hm))
			return makeSetter(hm, getCodeName());
		    return hcf.convert(hm);
		}
		public String getCodeName() { return hcf.getCodeName(); }
		public void clear(HMethod hm) { hcf.clear(hm); }
	    };
	// And chain this with a MethodMutator to change the GETs and SETs
	// to calls to the getter/setters.
	this.hcf = new Mutator(expanded).codeFactory();
	// done!
    }
    final HCodeFactory hcf;
    public HCodeFactory codeFactory() { return hcf; }
    /** This mutator class turns GETs and SETs into calls to accessor methods.
     */
    class Mutator extends MethodMutator<Quad> {
	Mutator(HCodeFactory hcf) { super(hcf); }
	protected String mutateCodeName(String codeName) {
	    /** XXX: do we need to turn SSI into RSSx form?
	    if (codeName.equals(QuadSSI.codename)) return QuadRSSx.codename;
	    */
	    return codeName;
	}
	protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	    HCode<Quad> hc = input.hcode();
	    // only mutate if this is not itself a setter/getter!
	    if (getter2field.containsKey(hc.getMethod()) ||
		setter2field.containsKey(hc.getMethod())) return hc; // bail!
	    // List of THROWs which need to be added to the FOOTER.
	    final List<THROW> fixup = new ArrayList<THROW>();
	    // visitor class to effect the transformation.
	    QuadVisitor qv = new QuadVisitor() {
		    public void visit(Quad q) {/* do nothing for most quads */}
		    public void visit(GET q) {
			if (!field2getter.containsKey(q.field())) return;
			assert !q.field().isStatic();
			Temp retex = new Temp(q.getFactory().tempFactory());
			CALL call = new CALL(q.getFactory(), q,
					     field2getter.get(q.field()),
					     new Temp[] { q.objectref() },
					     q.dst(), retex,
					     true/* is virtual */,
					     false/* not a tailCall */,
					     new Temp[0]);
			replace(q, call);
		    }
		    public void visit(SET q) {
			if (!field2setter.containsKey(q.field())) return;
			assert !q.field().isStatic();
			Temp retex = new Temp(q.getFactory().tempFactory());
			CALL call = new CALL(q.getFactory(), q,
					     field2setter.get(q.field()),
					     new Temp[]{q.objectref(),q.src()},
					     null, retex,
					     true/* is virtual */,
					     false/* not a tailCall */,
					     new Temp[0]);
			replace(q, call);
		    }
		    /** replace q with CALL, make THROW, add to fixup list. */
		    private void replace(Quad q, CALL call) {
			assert q.prevLength()==1 && q.nextLength()==1;
			Edge in = q.prevEdge(0), out = q.nextEdge(0);
			Quad.addEdge(in.from(), in.which_succ(), call, 0);
			Quad.addEdge(call,0, out.to(), out.which_pred());
			THROW thr = new THROW(call.getFactory(), call, 
					      call.retex());
			Quad.addEdge(call, 1, thr, 0);
			fixup.add(thr);
		    }
		};
	    // use the visitor to replace them boys!
	    for (Iterator<Quad> it=new SnapshotIterator<Quad>
		     (hc.getElementsI()); it.hasNext(); )
		it.next().accept(qv);
	    // fixup throws by adding to footer.
	    if (fixup.size()>0) {
		FOOTER oldF = (FOOTER) ((HEADER)hc.getRootElement()).next(0);
		FOOTER newF = oldF.resize(oldF.arity()+fixup.size());
		int n = newF.arity();
		for (Iterator<THROW> it=fixup.iterator(); it.hasNext(); ) {
		    THROW thr = it.next();
		    Quad.addEdge(thr, 0, newF, --n);
		}
	    }
	    // presto, done!
	    return hc;
	}
    }
    /** Make a getter method. */
    HCode<Quad> makeGetter(HMethod hm, String codename) {
	HField hf = getter2field.get(hm);
	MyCode hc = new MyCode(hm, codename);
	// make quads.
	QuadFactory qf = hc.getQF();
	Temp thisT = new Temp(qf.tempFactory(), "this");
	Temp retvT = new Temp(qf.tempFactory(), "retval");
	Quad qH = new HEADER(qf, null);
	Quad q1 = new METHOD(qf, null, new Temp[] { thisT }, 1);
	Quad q2 = new GET(qf, null, retvT, hf, thisT);
	Quad q3 = new RETURN(qf, null, retvT);
	Quad qF = new FOOTER(qf, null, 2);
	Quad.addEdge(qH, 0, qF, 0);
	Quad.addEdge(qH, 1, q1, 0);
	Quad.addEdges(new Quad[] { q1, q2, q3 });
	Quad.addEdge(q3, 0, qF, 1);
	// put these quads into hc.
	hc.setRoot(qH);
	// done!
	return hc;
    }
    /** Make a setter method. */
    HCode<Quad> makeSetter(HMethod hm, String codename) {
	HField hf = setter2field.get(hm);
	MyCode hc = new MyCode(hm, codename);
	// make quads.
	QuadFactory qf = hc.getQF();
	Temp thisT = new Temp(qf.tempFactory(), "this");
	Temp newvT = new Temp(qf.tempFactory(), "value");
	Quad qH = new HEADER(qf, null);
	Quad q1 = new METHOD(qf, null, new Temp[] { thisT, newvT }, 1);
	Quad q2 = new SET(qf, null, hf, thisT, newvT);
	Quad q3 = new RETURN(qf, null, null);
	Quad qF = new FOOTER(qf, null, 2);
	Quad.addEdge(qH, 0, qF, 0);
	Quad.addEdge(qH, 1, q1, 0);
	Quad.addEdges(new Quad[] { q1, q2, q3 });
	Quad.addEdge(q3, 0, qF, 1);
	// put these quads into hc.
	hc.setRoot(qH);
	// done!
	return hc;
    }
    class MyCode extends harpoon.IR.Quads.QuadSSI {
	final String codename;
	MyCode(HMethod hm, String codename) {
	    super(hm, null);
	    this.codename=codename;
	}
	// Field2Method-private accessors.
	void setRoot(Quad root) { this.quads = root; }
	QuadFactory getQF() { return this.qf; }
	// implementation of Code abstract methods.
	public String getName() { return codename; }
	public HCodeAndMaps<Quad> clone(HMethod newMethod) {
	    return cloneHelper(new MyCode(newMethod, codename));
	}
    }
    /** helper method to replace occurences of 'oldstr' in 's' with 'newstr'.*/
    private static String replace(String s, String oldstr, String newstr) {
	StringBuffer sb = new StringBuffer();
	while (true) {
	    // find oldstr
	    int idx = s.indexOf(oldstr);
	    // if not found, then done.
	    if (idx<0) break;
	    // split at idx
	    sb.append(s.substring(0, idx));
	    s = s.substring(idx+oldstr.length());
	    // add newstr.
	    sb.append(newstr);
	}
	sb.append(s);
	return sb.toString();
    }
}
