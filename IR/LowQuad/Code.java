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
 * @version $Id: Code.java,v 1.1.2.6 1999-02-04 22:56:14 cananian Exp $
 */
public abstract class Code extends harpoon.IR.Quads.Code 
    implements harpoon.IR.Properties.Derivation
{
    /** Hashtable to implement derivation map. */
    protected final Hashtable hD = new Hashtable();

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

    protected Code(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    /** Implement derivation interface. */
    public DList derivation(Temp t) { return (DList) hD.get(t); }

}
