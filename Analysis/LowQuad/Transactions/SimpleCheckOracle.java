// SimpleCheckOracle.java, created Sun Nov 12 01:25:37 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Transactions;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Util.*;

import java.util.*;
/**
 * <code>SimpleCheckOracle</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SimpleCheckOracle.java,v 1.1.2.1 2000-11-14 19:37:33 cananian Exp $
 */
class SimpleCheckOracle extends CheckOracle {
    public Set createReadVersions(HCodeElement hce) {
	if (hce instanceof AGET)
	    return Collections.singleton(((AGET)hce).objectref());
	if (hce instanceof GET)
	    return Collections.singleton(((GET)hce).objectref());
	return Collections.EMPTY_SET;
    }
    public Set createWriteVersions(HCodeElement hce) {
	if (hce instanceof ASET)
	    return Collections.singleton(((ASET)hce).objectref());
	if (hce instanceof SET)
	    return Collections.singleton(((SET)hce).objectref());
	return Collections.EMPTY_SET;
    }
    public Set checkField(HCodeElement hce) {
	if (hce instanceof GET)
	    return Collections.singleton
		(new RefAndField(((GET)hce).objectref(),((GET)hce).field()));
	if (hce instanceof SET)
	    return Collections.singleton
		(new RefAndField(((SET)hce).objectref(),((SET)hce).field()));
	return Collections.EMPTY_SET;
    }
    public Set checkArrayElement(HCodeElement hce) {
	if (hce instanceof AGET) {
	    AGET q = (AGET) hce;
	    return Collections.singleton
		(new RefAndIndexAndType(q.objectref(), q.index(), q.type()));
	}
	if (hce instanceof ASET) {
	    ASET q = (ASET) hce;
	    return Collections.singleton
		(new RefAndIndexAndType(q.objectref(), q.index(), q.type()));
	}
	return Collections.EMPTY_SET;
    }
}

