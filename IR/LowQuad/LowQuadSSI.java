// LowQuadSSI.java, created Wed Feb  3 16:19:45 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.TypeMap.TypeNotKnownException;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.SSIRename;
import harpoon.IR.Quads.ToNoSSA;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>LowQuadSSI</code> codeview exposes a lowquad based 
 * representation in SSI form. 

 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadSSI.java,v 1.4 2002-04-10 03:04:57 cananian Exp $
 */
public class LowQuadSSI extends Code { /*which extends harpoon.IR.Quads.Code*/
    /** The name of this code view. */
    public static final String codename  = "low-quad-ssi";

    /** Creates a <code>LowQuadSSI</code> object from a <code>QuadSSI</code>
     *  object. */
    LowQuadSSI(final harpoon.IR.Quads.Code code) {
	super(code.getMethod(), null);
	assert code.getName().equals(QuadSSI.codename);
	final Map dT = new HashMap();
	final Map tT = new HashMap();
	final TypeMap tym = new harpoon.Analysis.Quads.TypeInfo(code);
	FinalMap fm = new harpoon.Backend.Maps.DefaultFinalMap();
	AllocationInformationMap aim = (code.getAllocationInformation()!=null)
	    ? new AllocationInformationMap() : null;
	quads = Translate.translate((LowQuadFactory)qf, code, tym, fm,
				    dT, tT, aim);
      
	final LowQuadFactory lqf =  // javac bug workaround to let qf be
	    (LowQuadFactory) qf;    // visible in anonymous Derivation below.
	setDerivation(new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		assert hce!=null && t!=null;
		if (dT.get(t)==null && tT.get(t)==null)
		    throw new TypeNotKnownException(hce, t);
		return (DList)dT.get(t);
	    }
	    public HClass typeMap(HCodeElement hce, Temp t) { 
		assert lqf.tempFactory()==t.tempFactory();
		if (dT.get(t)==null && tT.get(t)==null)
		    throw new TypeNotKnownException(hce, t);
		return (HClass)tT.get(t);
	    }
	});
	setAllocationInformation(aim);
    }

    /** Creates a <code>LowQuadSSI</code> object from a 
     *  <code>LowQuadNoSSA</code> object. */
    public LowQuadSSI(final LowQuadNoSSA code) {
	super(code.getMethod(), null);
	SSIRename rename = new SSIRename(code, qf);
	quads = rename.rootQuad;
	setDerivation(rename.derivation);
	setAllocationInformation(rename.allocInfo);
    }

    /**
     * Create a new code object given a quadruple representation of the
     * method instructions.
     */
    protected LowQuadSSI(HMethod method, Quad quads) {
	super(method, quads);
    }

    /**
     * Clone this code representation.  The clone has its own copy of the
     * quad graph.
     * <p><b>WARNING: does not preserve derivation or allocation info.</b>
     */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new LowQuadSSI(newMethod, null));
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"low-quad-ssi"</code>
     */
    public String getName() { return codename; }

    /**
     * Return a code factory for <code>LowQuadSSI</code>, given a 
     * code factory for <code>QuadSSI</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>QuadSSI</code> or <code>LowQuadNoSSA</code>, then
     *      creates and returns a code factory for <code>LowQuadSSI</code>.
     *      Else passes <code>hcf</code> to
     *      <code>QuadSSI.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>LowQuadSSI</code> from the
     *      code factory returned by <code>QuadSSI</code>.
     * @see QuadSSI#codeFactory(HCodeFactory)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    harpoon.IR.Quads.Code c = (harpoon.IR.Quads.Code)
			hcf.convert(m);
		    return (c==null) ? null : new LowQuadSSI(c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(LowQuadNoSSA.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null : new LowQuadSSI((LowQuadNoSSA)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	}
	else {
	    //throw new Error("don't know how to make " + codename +
	    //	" from " + hcf.getCodeName());
	    return codeFactory(QuadSSI.codeFactory(hcf));
	}
    }
  
    /**
     * Return a code factory for <code>LowQuadSSI</code>, using the default
     * code factory for <code>harpoon.IR.Quads.QuadSSI</code>
     */
    public static HCodeFactory codeFactory() {  
	return codeFactory(QuadSSI.codeFactory());
    }
}
