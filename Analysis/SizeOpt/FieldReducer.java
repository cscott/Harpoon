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
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Arrays;
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
 * @version $Id: FieldReducer.java,v 1.1.2.2 2001-09-12 18:33:58 cananian Exp $
 */
public class FieldReducer extends MethodMutator {
    final BitWidthAnalysis bwa;
    
    /** Creates a <code>FieldReducer</code>. */
    public FieldReducer(HCodeFactory parent, Linker linker, ClassHierarchy ch,
			Set roots) {
        this(new CachingCodeFactory(QuadSSI.codeFactory(parent)),
	     linker, ch, roots);
    }
    private FieldReducer(CachingCodeFactory parent, Linker linker,
			 ClassHierarchy ch, Set roots) {
	super(parent);
        this.bwa = new BitWidthAnalysis(linker, parent, ch, roots);
	// pull all our classes through the mutator.
	HCodeFactory hcf = codeFactory();
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
	    // remove all unread and constant fields.
	    // (which should have no more references in any HCode)
	    if (bwa.isConst(hf) || !bwa.isRead(hf)) {
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
	    Util.assert(nhc!=null); // one of these cases must have matched
	    hf.getMutator().setType(nhc);
	}
	// done!
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	Wrapper w = new Wrapper(input);
	new SCCOptimize(w, w, w).optimize(hc);
	Quad[] quads = (Quad[]) hc.getElements();
	for (int i=0; i<quads.length; i++) {
	    if (quads[i] instanceof GET) {
		GET q = (GET) quads[i];
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
		}
	    }
	}
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
