// MZFExternalize.java, created Tue Nov 13 23:07:42 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import net.cscott.jutil.SnapshotIterator;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * The <code>MZFExternalize</code> class takes fields which aren't
 * 'sub-class final' but *are* sufficiently mostly-zero (according to
 * profile information) and turns then into references into an
 * external weak hash map.  This saves the memory which the field
 * occupies for all the mostly-zero fields, at the expense of requiring
 * about twice-the-usual memory for non-zero fields.  If the fields
 * really *are* mostly-zero, then the net will be a space savings.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MZFExternalize.java,v 1.7 2004-02-08 01:53:55 cananian Exp $
 */
class MZFExternalize {
    public static final double THRESHOLD =
	Double.parseDouble(System.getProperty("harpoon.sizeopt.mzf.threshold",
					      "75.0"/*default*/));
    final HCodeFactory hcf;
    final Linker linker;
    final private HMethod intGET, intSET, longGET, longSET;
    final private HMethod floatGET, floatSET, doubleGET, doubleSET;
    final private HMethod ptrGET, ptrSET;
    
    /** Creates a <code>MZFExternalize</code>. */
    MZFExternalize(HCodeFactory hcf, Linker linker, ProfileParser pp,
		   Set<HClass> stoplist, Set<HField> doneFields) {
	//first initialize the HMethods which define the external map interface
	this.linker = linker;
	HClass HCexmap = linker.forClass(harpoon.Runtime.MZFExternalMap.class);
	this.intGET = HCexmap.getDeclaredMethod
	    ("intGET", "(Ljava/lang/Object;Ljava/lang/Object;I)I");
	this.intSET = HCexmap.getDeclaredMethod
	    ("intSET", "(Ljava/lang/Object;Ljava/lang/Object;II)V");
	this.longGET = HCexmap.getDeclaredMethod
	    ("longGET", "(Ljava/lang/Object;Ljava/lang/Object;J)J");
	this.longSET = HCexmap.getDeclaredMethod
	    ("longSET", "(Ljava/lang/Object;Ljava/lang/Object;JJ)V");
	this.floatGET = HCexmap.getDeclaredMethod
	    ("floatGET", "(Ljava/lang/Object;Ljava/lang/Object;F)F");
	this.floatSET = HCexmap.getDeclaredMethod
	    ("floatSET", "(Ljava/lang/Object;Ljava/lang/Object;FF)V");
	this.doubleGET = HCexmap.getDeclaredMethod
	    ("doubleGET", "(Ljava/lang/Object;Ljava/lang/Object;D)D");
	this.doubleSET = HCexmap.getDeclaredMethod
	    ("doubleSET", "(Ljava/lang/Object;Ljava/lang/Object;DD)V");
	this.ptrGET = HCexmap.getDeclaredMethod
	    ("ptrGET", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	this.ptrSET = HCexmap.getDeclaredMethod
	    ("ptrSET", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
	// okay, not collect a list of fields which we want to transform.
	Map<HField,Number> myfields = new HashMap<HField,Number>();
	for (Iterator it=pp.fieldsAboveThresh(THRESHOLD).iterator();
	     it.hasNext(); ) {
	    List pair = (List) it.next();
	    HField hf = (HField) pair.get(0);
	    Number mostly = (Number) pair.get(1);
	    if (stoplist.contains(hf.getDeclaringClass())) continue;
	    if (doneFields.contains(hf)) continue;
	    // okay, this is one we want to do.
	    myfields.put(hf, mostly);
	}
	System.out.println("EXTERNALIZING: "+myfields);
	// turn these fields into accessors.
	Field2Method f2m = new Field2Method(hcf, myfields.keySet());
	hcf = f2m.codeFactory();
	// except now we implement the getters/setters ourselves.
	hcf = new CachingCodeFactory(hcf);
	for (Iterator<HField> it=myfields.keySet().iterator(); it.hasNext(); ){
	    HField hf = it.next();
	    HMethod getter = f2m.field2getter.get(hf);
	    HMethod setter = f2m.field2setter.get(hf);
	    Number mostly = myfields.get(hf);
	    ((CachingCodeFactory)hcf).put
		(getter, makeGetter(hcf, getter, hf, mostly));
	    ((CachingCodeFactory)hcf).put
		(setter, makeSetter(hcf, setter, hf, mostly));
	}
	// okay, it should now be safe to go ahead and remove those fields.
	for (Iterator<HField> it=myfields.keySet().iterator(); it.hasNext(); ){
	    HField hf = it.next();
	    hf.getDeclaringClass().getMutator().removeDeclaredField(hf);//ta-da
	}
	// set the externally-visible code factory.
	this.hcf = hcf;
    }
    public HCodeFactory codeFactory() { return this.hcf; }

    // Let's use the (address of the) string constant corresponding to the
    // full name of the (former) field as our secondary key into the hash
    // table.  This isn't ideal from a space perspective: we ought to be
    // able to squeeze the object pointer and the field identifier into
    // one word.  But it'll do for now.
    
    // also, we're going to simplify the native method implementation so
    // that we just have three types: 32-bit word, 64-bit word, and pointer.
    // we'll do the appropriate conversions in java-land.

    HCode<Quad> makeGetter(HCodeFactory hcf, HMethod getter,
		     HField hf, Number mostly) {
	// xxx cheat: get old getter and replace GET with our cruft.
	// would be better to make this from scratch.
	HCode<Quad> hc = hcf.convert(getter);
	assert hc.getName().equals(QuadRSSx.codename) : hc;
	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (hc.getElementsI()); it.hasNext(); ) {
	    Quad aquad = it.next();
	    if (aquad instanceof GET) {
		GET q = (GET) aquad;
		assert q.field().equals(hf);
		// mu-ha-ha-ha-ha-ha!
		QuadFactory qf = q.getFactory();
		Temp defT = new Temp(qf.tempFactory(), "default");
		Temp keyT = new Temp(qf.tempFactory(), "key");
		Temp retexT = new Temp(qf.tempFactory(), "retex");
		HClass ty = widen(q.field().getType());
		String KEY = hf.getDeclaringClass().getName()+"."+hf.getName();
		Quad q0 = new CONST
		    (qf, q, keyT, KEY, linker.forName("java.lang.String"));
		assert ty.isPrimitive()?true:mostly.intValue()==0;
		Quad q1 = ty.isPrimitive() ?
		    new CONST(qf, q, defT, makeValue(ty, mostly), ty) :
		    new CONST(qf, q, defT, null, HClass.Void);
		// call HASHGET(KEY, obj, mostly)
		Quad q2 = new CALL
		    (qf, q, hashGET(ty),
		     new Temp[] { keyT, q.objectref(), defT },
		     q.dst(), retexT, false, ty.isPrimitive(), new Temp[0]);
		// throw any error returned.
		Quad q3 = new THROW(qf, q, retexT);
		// okay, link all these together.
		Edge in = q.prevEdge(0), out = q.nextEdge(0);
		Quad.addEdge(in.from(), in.which_succ(), q0, 0);
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		Quad.addEdge(q2, 0, (Quad)out.to(), out.which_pred());
		Quad.addEdge(q2, 1, q3, 0);
		FOOTER f = ((HEADER)hc.getRootElement()).footer();
		f=f.attach(q3, 0);
		// insert type check for non-primitive types.
		if (!ty.isPrimitive()) {
		    // INSTANCEOF can't be given null, so do a null-check 1st.
		    // if (q!=null && !(q instanceof xxx)) assert 0;
		    Temp nullT = new Temp(qf.tempFactory(), "null");
		    Temp tstT = new Temp(qf.tempFactory(), "test");
		    Quad q4 = new CONST(qf, q, nullT, null, HClass.Void);
		    Quad q5 = new OPER(qf, q, Qop.ACMPEQ, tstT,
				       new Temp[] { q.dst(), nullT });
		    Quad q6 = new CJMP(qf, q, tstT, new Temp[0]);
		    Quad q7 = new INSTANCEOF(qf, q, tstT, q.dst(), ty);
		    Quad q8 = new CJMP(qf, q, tstT, new Temp[0]);
		    Quad q9 = new PHI(qf, q, new Temp[0], 2);//nrm
		    // what do i do if not valid?  i'm too lazy to create
		    // an exception to throw.  let's infinite-loop.
		    Quad q10= new PHI(qf, q, new Temp[0], 2);//ex
		    Quad.addEdges(new Quad[] { q2, q4, q5, q6, q7, q8, q10 });
		    Quad.addEdge(q6, 1, q9, 0);
		    Quad.addEdge(q8, 1, q9, 1);
		    Quad.addEdge(q9, 0, out.to(), out.which_pred());
		    Quad.addEdge(q10, 0, q10, 1);
		}
	    }
	}
	// done!
	return hc;
    }
    HCode<Quad> makeSetter(HCodeFactory hcf, HMethod setter,
		     HField hf, Number mostly) {
	// xxx cheat: get old setter and replace SET with our cruft.
	// would be better to make this from scratch.
	HCode<Quad> hc = hcf.convert(setter);
	assert hc.getName().equals(QuadRSSx.codename) : hc;
	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (hc.getElementsI()); it.hasNext(); ) {
	    Quad aquad = it.next();
	    if (aquad instanceof SET) {
		SET q = (SET) aquad;
		assert q.field().equals(hf);
		// mu-ha-ha-ha-ha-ha!
		QuadFactory qf = q.getFactory();
		Temp defT = new Temp(qf.tempFactory(), "default");
		Temp keyT = new Temp(qf.tempFactory(), "key");
		Temp retexT = new Temp(qf.tempFactory(), "retex");
		HClass ty = widen(q.field().getType());
		String KEY = hf.getDeclaringClass().getName()+"."+hf.getName();
		Quad q0 = new CONST
		    (qf, q, keyT, KEY, linker.forName("java.lang.String"));
		assert ty.isPrimitive()?true:mostly.intValue()==0;
		Quad q1 = ty.isPrimitive() ?
		    new CONST(qf, q, defT, makeValue(ty, mostly), ty) :
		    new CONST(qf, q, defT, null, HClass.Void);
		// call HASHSET(KEY, obj, newvalue, mostly)
		Quad q2 = new CALL
		    (qf, q, hashSET(ty),
		     new Temp[] { keyT, q.objectref(), q.src(), defT },
		     null, retexT, false, true, new Temp[0]);
		// throw any error returned.
		Quad q3 = new THROW(qf, q, retexT);
		// okay, link all these together.
		Edge in = q.prevEdge(0), out = q.nextEdge(0);
		Quad.addEdge((Quad)in.from(), in.which_succ(), q0, 0);
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		Quad.addEdge(q2, 0, (Quad)out.to(), out.which_pred());
		Quad.addEdge(q2, 1, q3, 0);
		FOOTER f = ((HEADER)hc.getRootElement()).footer();
		f=f.attach(q3, 0);
	    }
	}
	// done!
	return hc;
    }
    // private helper functions.
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = e.from(); int frm_succ = e.which_succ();
	Quad to  = e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }
    // widen sub-int primitive types.
    private static HClass widen(HClass hc) {
	return MZFCompressor.widen(hc);
    }
    // wrap a value w/ an object of the appropriate type.
    private static Object makeValue(HClass type, Number num) {
	return MZFCompressor.wrap(type, num);
    }
    private HMethod hashGET(HClass type) {
	if (!type.isPrimitive()) return ptrGET;
	if (type==HClass.Int) return intGET;
	if (type==HClass.Long) return longGET;
	if (type==HClass.Float) return floatGET;
	if (type==HClass.Double) return doubleGET;
	assert false : ("unknown type: "+type);
	return null;
    }
    private HMethod hashSET(HClass type) {
	if (!type.isPrimitive()) return ptrSET;
	if (type==HClass.Int) return intSET;
	if (type==HClass.Long) return longSET;
	if (type==HClass.Float) return floatSET;
	if (type==HClass.Double) return doubleSET;
	assert false : ("unknown type: "+type);
	return null;
    }
}
