// Code.java, created Fri Jan 22 17:00:41 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Hashtable;
import java.util.Iterator;
/**
 * <code>LowQuad.Code</code> is an abstract superclass of codeviews
 * using the components in <code>IR.LowQuad</code>.  It implements
 * shared methods for the various codeviews using <code>LowQuad</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.3 2003-03-10 22:19:13 cananian Exp $
 */
public abstract class Code extends harpoon.IR.Quads.Code 
{
    /** Make the quad factory. */
    protected harpoon.IR.Quads.QuadFactory newQF(final HMethod parent) {
	final String scope = parent.getDeclaringClass().getName() + "." +
	    parent.getName() + parent.getDescriptor() + "/" + getName();
	abstract class SerializableLowQuadFactory extends LowQuadFactory
	    implements java.io.Serializable { /* only declare inheritance */ }
	return new SerializableLowQuadFactory() {
	    private final TempFactory tf = Temp.tempFactory(scope);
	    private int id=0;
	    public TempFactory tempFactory() { return tf; }
	    public harpoon.IR.Quads.Code getParent() { return Code.this; }
	    public synchronized int getUniqueID() { return id++; }
	};
    }

    protected Code(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    // Provide access to derivation information.
    private Derivation<Quad> m_derivation = null;
    /** Return a <code>Derivation</code> object for this
     *  <code>LowQuad.Code</code>. */
    public Derivation<Quad> getDerivation() { return m_derivation; }
    /** Allow subclasses to initialize the <code>Derivation</code> for
     *  this <code>LowQuad.Code</code>. */
    protected void setDerivation(Derivation<Quad> deriv){ m_derivation=deriv; }

    // clone derivation information, too.
    protected HCodeAndMaps<Quad> cloneHelper(harpoon.IR.Quads.Code _this,
					     harpoon.IR.Quads.Code qc) {
	final HCodeAndMaps hcam = super.cloneHelper(_this, qc);
	if (_this.getDerivation()!=null)
	    ((Code)qc).setDerivation(cloneDerivation(hcam));
	return hcam;
    }
    private static Derivation<Quad> cloneDerivation(HCodeAndMaps<Quad> hcam) {
	Code ocode = (Code) hcam.ancestorHCode();
	Derivation<Quad> oderiv = ocode.getDerivation();
	DerivationMap<Quad> nderiv = new DerivationMap<Quad>();
	for (Iterator<Quad> it=ocode.getElementsI(); it.hasNext(); ) {
	    Quad ohce = it.next();
	    Quad nhce = hcam.elementMap().get(ohce);
	    nderiv.transfer(nhce, ohce, ohce.def(),
			    hcam.tempMap(), oderiv);
	}
	return nderiv;
    }
}
