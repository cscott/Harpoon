// TempChain.java, created Thu May  6 00:58:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>TempChain</code> needs to be documented.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TempChain.java,v 1.2 2002-02-25 21:05:13 cananian Exp $
 */
public class TempChain extends harpoon.Temp.Temp {
    public HCodeElement def;
    public TempChain nextUse; // never null.
    
    /** Creates a <code>TempChain</code>. */ // new def
    public TempChain(final TempFactory tf, final String prefix) {
        super(tf, prefix);
	this.def = null;
	this.nextUse = this; // circular list
	Util.assert(nextUse!=null);
    }
    public TempChain(TempChain tc) { // new use of name used/defined by tc
	super(tc.tempFactory(), tc.name());
	this.def = tc.def;
	this.nextUse = tc.nextUse;
	tc.nextUse = this;
	Util.assert(nextUse!=null);
    }
}
