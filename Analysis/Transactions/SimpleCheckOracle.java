// SimpleCheckOracle.java, created Sun Nov 12 01:25:37 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.SET;

import java.util.Collections;
import java.util.Set;
/**
 * A <code>SimpleCheckOracle</code> provides a valid but extremely
 * simple-minded implementation of <code>CheckOracle</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SimpleCheckOracle.java,v 1.1.2.3 2001-02-23 03:10:40 cananian Exp $
 */
class SimpleCheckOracle extends CheckOracle {
    private final boolean noArrayModification;
    SimpleCheckOracle() { this(false); }
    SimpleCheckOracle(boolean noArrayModification) {
	this.noArrayModification = noArrayModification;
    }
    public Set createReadVersions(HCodeElement hce) {
	if (hce instanceof AGET && !noArrayModification)
	    return Collections.singleton(((AGET)hce).objectref());
	if (hce instanceof GET && !((GET)hce).isStatic())
	    return Collections.singleton(((GET)hce).objectref());
	return Collections.EMPTY_SET;
    }
    public Set createWriteVersions(HCodeElement hce) {
	if (hce instanceof ARRAYINIT && !noArrayModification)
	    return Collections.singleton(((ARRAYINIT)hce).objectref());
	if (hce instanceof ASET && !noArrayModification)
	    return Collections.singleton(((ASET)hce).objectref());
	if (hce instanceof SET && !((SET)hce).isStatic())
	    return Collections.singleton(((SET)hce).objectref());
	return Collections.EMPTY_SET;
    }
    public Set checkField(HCodeElement hce) {
	if (hce instanceof GET && !((GET)hce).isStatic())
	    return Collections.singleton
		(new RefAndField(((GET)hce).objectref(),((GET)hce).field()));
	if (hce instanceof SET && !((SET)hce).isStatic())
	    return Collections.singleton
		(new RefAndField(((SET)hce).objectref(),((SET)hce).field()));
	return Collections.EMPTY_SET;
    }
    public Set checkArrayElement(HCodeElement hce) {
	if (hce instanceof AGET && !noArrayModification) {
	    AGET q = (AGET) hce;
	    return Collections.singleton
		(new RefAndIndexAndType(q.objectref(), q.index(), q.type()));
	}
	if (hce instanceof ASET && !noArrayModification) {
	    ASET q = (ASET) hce;
	    return Collections.singleton
		(new RefAndIndexAndType(q.objectref(), q.index(), q.type()));
	}
	return Collections.EMPTY_SET;
    }
}

