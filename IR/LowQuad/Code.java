// Code.java, created Fri Jan 22 17:00:41 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Hashtable;
/**
 * <code>LowQuad.Code</code> is an abstract superclass of codeviews
 * using the components in <code>IR.LowQuad</code>.  It implements
 * shared methods for the various codeviews using <code>LowQuad</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.14 2000-05-17 03:19:30 cananian Exp $
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
    private Derivation m_derivation = null;
    /** Return a <code>Derivation</code> object for this
     *  <code>LowQuad.Code</code>. */
    public Derivation getDerivation() { return m_derivation; }
    /** Allow subclasses to initialize the <code>Derivation</code> for
     *  this <code>LowQuad.Code</code>. */
    protected void setDerivation(Derivation deriv) { m_derivation = deriv; }
}
