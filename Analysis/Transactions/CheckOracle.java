// CheckOracle.java, created Sun Nov 12 01:19:11 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;

import java.util.Set;
/**
 * A <code>CheckOracle</code> helps the SyncTransformer place
 * field and object version lookups and checks.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CheckOracle.java,v 1.1.2.3 2001-01-16 19:31:33 cananian Exp $
 */
abstract class CheckOracle {
    
    /** Returns <code>Set</code> of <code>Temp</code>s for which read-only
     *  versions should be looked up just before <code>hce</code> is
     *  executed. */
    public abstract Set createReadVersions(HCodeElement hce);
    /** Returns <code>Set</code> of <code>Temp</code>s for which writable
     *  versions should be created just before <code>hce</code> is executed. */
    public abstract Set createWriteVersions(HCodeElement hce);
    /** Returns a <code>Set</code> of <code>RefAndField</code> tuples
     *  which should be checked just before <code>hce</code> is
     *  executed. */
    public abstract Set checkField(HCodeElement hce);
    /** Returns a <code>Set</code> of <code>RefAndIndexAndType</code>
     *  typles which indicate indexed array elements which should be
     *  checked just before <code>hce</code> is executed.  */
    public abstract Set checkArrayElement(HCodeElement hce);

    class RefAndField {
	public final Temp objref;
	public final HField field;
	RefAndField(Temp objref, HField field) {
	    this.objref = objref; this.field = field;
	}
	// define hashCode and equals so these objects work well in sets
	public int hashCode() { return objref.hashCode() ^ field.hashCode(); }
	public boolean equals(Object o) {
	    if (!(o instanceof RefAndField)) return false;
	    RefAndField raf = (RefAndField) o;
	    return this.objref.equals(raf.objref) &&
		this.field.equals(raf.field);
	}
	// for debugging, define toString
	public String toString() { return "{ "+objref+", "+field+" }"; }
    }
    class RefAndIndexAndType {
	public final Temp objref;
	public final Temp index;
	public final HClass type;
	RefAndIndexAndType(Temp objref, Temp index, HClass type) {
	    this.objref = objref; this.index = index; this.type = type;
	}
	// define hashCode and equals so these objects work well in sets
	public int hashCode() { return objref.hashCode() ^ index.hashCode(); }
	public boolean equals(Object o) {
	    if (!(o instanceof RefAndIndexAndType)) return false;
	    RefAndIndexAndType rit = (RefAndIndexAndType) o;
	    return this.objref.equals(rit.objref) &&
		this.index.equals(rit.index) &&
		this.type.equals(rit.type);
	}
	// for debugging, define toString
	public String toString() { return "{ "+objref+", "+index+", "+type+" }"; }
    }
}
