// FieldReducer.java, created Thu Jul 19 18:55:44 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ConstMap;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Maps.ExecMap;
import harpoon.Analysis.Quads.SCC.SCCOptimize;
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
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
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
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * The <code>FieldReducer</code> code factory uses the results of a
 * <code>BitWidthAnalysis</code> to shrink field types and eliminate
 * unused and constant fields from objects.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldReducer.java,v 1.3.2.1 2002-02-27 08:33:09 cananian Exp $
 */
public class FieldReducer extends MethodMutator {
    private static final boolean no_mutate =
	Boolean.getBoolean("harpoon.sizeopt.no-field-reducer");
    private static final boolean DEBUG = false;
    final BitWidthAnalysis bwa;
    final Linker linker;
    
    /** Creates a <code>FieldReducer</code>. */
    public FieldReducer(HCodeFactory parent, Frame frame, ClassHierarchy ch,
			Set roots, String fieldRootResourceName) {
        this(new CachingCodeFactory(QuadSSI.codeFactory(parent)),
	     frame, ch, roots, fieldRootResourceName);
    }
    private FieldReducer(CachingCodeFactory parent, Frame frame,
			 ClassHierarchy ch, Set roots, String frrn) {
	super(parent);
        this.bwa = new BitWidthAnalysis(frame.getLinker(), parent,
					ch, roots, frrn);
	this.linker = frame.getLinker();
	// pull all our classes through the mutator.
	this.hcf = super.codeFactory();
	for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); )
	    hcf.convert((HMethod)it.next());
	// now munge class fields (after we've rewritten all field references)

	// first, collect all non-static fields of this application.
	Set flds = new HashSet(7*ch.classes().size());
	for (Iterator it=ch.classes().iterator(); it.hasNext(); )
	    flds.addAll(Arrays.asList(((HClass)it.next()).getFields()));
	for (Iterator it=flds.iterator(); it.hasNext(); )
	    if (((HField)it.next()).isStatic()) it.remove();
	// now munge fields!
	for (Iterator it=flds.iterator(); it.hasNext(); ) {
	    HField hf = (HField) it.next();
	    if (no_mutate) continue;
	    // remove all unread and constant fields.
	    // (which should have no more references in any HCode)
	    if (bwa.isConst(hf) || !bwa.isRead(hf)) {
		if (DEBUG)
		    System.err.println("REMOVING "+hf+" BECAUSE "+
				       (bwa.isConst(hf)?"it is constant ":"")+
				       (bwa.isRead(hf)?"":"it is unread"));
		hf.getDeclaringClass().getMutator().removeDeclaredField(hf);
		continue;
	    }
	    // shrink field sizes
	    HClass hc = hf.getType();
	    if (!hc.isPrimitive()) continue;
	    if (hc==HClass.Float || hc==HClass.Double) continue;
	    int p = bwa.plusWidthMap(hf), m = bwa.minusWidthMap(hf);
	    // (pick a size)
	    HClass nhc = null;
	    if (hc==HClass.Long   || (m<64 && p<=63)) nhc = HClass.Long;
	    if (hc==HClass.Int    || (m<32 && p<=31)) nhc = HClass.Int;
	    if (hc==HClass.Char   || (m==0 && p<=16)) nhc = HClass.Char;
	    if (hc==HClass.Short  || (m<16 && p<=15)) nhc = HClass.Short;
	    if (hc==HClass.Byte   || (m< 8 && p<= 7)) nhc = HClass.Byte;
	    if (hc==HClass.Boolean|| (m==0 && p<= 1)) nhc = HClass.Boolean;
	    assert nhc!=null; // one of these cases must have matched
	    if (DEBUG && hc!=nhc) System.err.print("REDUCING "+hf);
	    hf.getMutator().setType(nhc);
	    if (DEBUG && hc!=nhc) System.err.println(" TO "+hf);
	}
	// update field sizes in frame. (this is a bit of a hack)
	frame.setClassHierarchy(ch);
	// wrap a size counter around everything (maybe).
	if (Boolean.getBoolean("harpoon.sizeopt.bitcounters")) {
	    this.hcf = new CachingCodeFactory
		(new SizeCounters(this.hcf, frame, bwa).codeFactory());
	    // pull everything through.
	    for (Iterator it=ch.callableMethods().iterator(); it.hasNext(); )
		hcf.convert((HMethod)it.next());
	}
	// done!
    }
    // allow us to override the hcf.
    private HCodeFactory hcf;
    public HCodeFactory codeFactory() { return hcf; }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	final Wrapper w = new Wrapper(input);

	// optimize only methods which this analysis knows are callable.
	// (non-callable methods will be entirely eliminated by dead-code
	//  elimination, resulting in invalid quad hcodes)
	boolean isCallable =
	    w.execMap((METHOD)((Quad)hc.getRootElement()).next(1));
	if (!isCallable) return eviscerate(hc);
	new SCCOptimize(w, w, w).optimize(hc);
	if (no_mutate) return hc;

	Quad[] quads = (Quad[]) hc.getElements();
	for (int i=0; i<quads.length; i++) {
	    if (quads[i] instanceof GET) {
		GET q = (GET) quads[i];
		// if this GET is not dead, then the field is read.
		assert bwa.isRead(q.field());
		if (bwa.isConst(q.field())) {
		    // replace reads of constant fields with constant.
		    Quad.replace(q, new CONST
				 (q.getFactory(), q, q.dst(),
				  bwa.constMap(q.field()),
				  (bwa.constMap(q.field())==null) ?
				  HClass.Void :
				  toInternal(q.field().getType())));
		}
	    }
	    if (quads[i] instanceof SET) {
		SET q = (SET) quads[i];
		if (!bwa.isRead(q.field())) {
		    // throw away writes to unread fields.
		    q.remove();
		} else if (bwa.isConst(q.field()))
		    // throw away writes to compile-time constant fields.
		    q.remove();
	    }
	}
	return hc;
    }
    // Eviscerate this uncallable method, replacing with a simple throw.
    private HCode eviscerate(HCode hc) {
	HEADER header = (HEADER) hc.getRootElement();
	FOOTER footer = (FOOTER) header.next(0);
	METHOD method = (METHOD) header.next(1);
	footer = footer.resize(1); //eliminate all non-HEADER edges into footer
	assert footer.prevLength()==1;
	assert footer.prev(0)==header;
	assert method.nextLength()==1;
	// create new 'throw new RuntimeException()' body.
	HClass HCrex = linker.forName("java.lang.RuntimeException");
	QuadFactory qf = header.getFactory();
	HCodeElement src = method;
	Temp t0 = new Temp(qf.tempFactory());
	Temp t1 = new Temp(qf.tempFactory());
	Temp t2 = new Temp(qf.tempFactory());
	// create new RuntimeException object.
	Quad q0 = new NEW(qf, src, t0, HCrex);
	// initialize it.
	Quad q1 = new CALL(qf, src, HCrex.getConstructor(new HClass[0]),
			   new Temp[] { t0 }, null, t1, false, false,
			   new Temp[0]);
	// merge all exceptions from call
	Quad q2 = new PHI(qf, src, new Temp[] { t2 },
	                  new Temp[][] { new Temp[] { t0, t1 } }, 2);
	// throw!
	Quad q3 = new THROW(qf, src, t2);
	// link everything together.
	Quad.addEdges(new Quad[] { method, q0, q1, q2, q3 });
	Quad.addEdge(q1, 1, q2, 1);
	footer = footer.attach(q3, 0);
	return hc;
    }

    // Deal with the fact that external Byte/Short/Char/Boolean classes
    // are represented internally as ints.
    static HClass toInternal(HClass c) {
	if (c.equals(HClass.Byte) || c.equals(HClass.Short) ||
	    c.equals(HClass.Char) || c.equals(HClass.Boolean))
	    return HClass.Int;
	return c;
    }
    // wrapper to redirect maps through the hcodeandmaps
    class Wrapper implements ExactTypeMap, ConstMap, ExecMap {
	final Map aem; final TempMap atm;
	Wrapper(HCodeAndMaps hcam) {
	    this.aem = hcam.ancestorElementMap();
	    this.atm = hcam.ancestorTempMap();
	}
	HCodeElement map(HCodeElement hce) {
	    return (HCodeElement) aem.get(hce);
	}
	Temp map(Temp t) { return atm.tempMap(t); }
	// ExactTypeMap
	public boolean isExactType(HCodeElement hce, Temp t) {
	    return bwa.isExactType(map(hce), map(t));
	}
	// TypeMap
	public HClass typeMap(HCodeElement hce, Temp t) {
	    return bwa.typeMap(map(hce), map(t));
	}
	// ConstMap
	public boolean isConst(HCodeElement hce, Temp t) {
	    return bwa.isConst(map(hce), map(t));
	}
	public Object constMap(HCodeElement hce, Temp t) {
	    return bwa.constMap(map(hce), map(t));
	}
	// ExecMap
	public boolean execMap(HCodeElement hce) {
	    if (hce instanceof HEADER || hce instanceof FOOTER) return true;
	    return bwa.execMap(map(hce));
	}
	public boolean execMap(HCodeEdge edge) {
	    Edge e = (Edge) edge;
	    if (e.from() instanceof HEADER) return true;
	    Edge ne = ((Quad)map((Quad)e.from())).nextEdge(e.which_succ());
	    return bwa.execMap(ne);
	}
    }
}
