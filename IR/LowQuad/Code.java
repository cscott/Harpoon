// Code.java, created Fri Jan 22 17:00:41 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Hashtable;
/**
 * <code>Code</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.2 1999-01-23 10:06:16 cananian Exp $
 */
public class Code extends harpoon.IR.Quads.Code /* which extends HCode */ 
    implements harpoon.IR.Properties.Derivation
{
    /** The name of this code view. */
    public static final String codename = "low-quad";
    /** Hashtable to implement derivation map. */
    private Hashtable hD = new Hashtable();

    /** Make the quad factory. */
    protected harpoon.IR.Quads.QuadFactory newQF(final HMethod parent) {
	final String scope = parent.getDeclaringClass().getName() + "." +
	    parent.getName() + parent.getDescriptor() + "/" + getName();
	return new LowQuadFactory() {
	    private final TempFactory tf = Temp.tempFactory(scope);
	    private int id=0;
	    public TempFactory tempFactory() { return tf; }
	    public harpoon.IR.Quads.Code getParent() { return Code.this; }
	    public synchronized int getUniqueID() { return id++; }
	};
    }
    /** Creates a <code>harpoon.IR.LowQuad.Code</code> object from a
     *  <code>QuadSSA</code> object. */
    Code(QuadSSA qsa)
    {
	super(qsa.getMethod(), null);
	TypeMap tym = new harpoon.Analysis.QuadSSA.TypeInfo();
	FinalMap fm = new harpoon.Backend.Maps.DefaultFinalMap();
	quads = Translate.translate((LowQuadFactory)this.qf, qsa,
				    tym, fm, hD);
    }
    private Code(HMethod parent, Quad quads) {
	super(parent, quads);
    }
    /** Clone this code representatino.  The clone has its own copy of
     *  the quad graph. */
    public HCode clone(HMethod newMethod) {
	Code lq = new Code(newMethod, null);
	lq.quads = Quad.clone(lq.qf, quads);
	return lq;
    }
    /** Return the name of this code view.
     * @return the string <code>"low-quad"</code>.
     */
    public String getName() { return codename; }

    /** Implement derivation interface. */
    public DList derivation(Temp t) { return (DList) hD.get(t); }

    public static void register() {
	HCodeFactory f = new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode c = m.getCode(QuadSSA.codename);
		return (c==null) ? null :
		new Code((QuadSSA)c);
	    }
	    public String getCodeName() { return codename; }
	};
	HMethod.register(f);
    }
}
