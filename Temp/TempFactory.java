// TempFactory.java, created Fri Dec 11 21:10:48 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

/**
 * A <code>TempFactory</code> assigns unique identifiers to 
 * <code>Temp</code>s within a given scope.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TempFactory.java,v 1.3 2004-02-08 01:59:47 cananian Exp $
 */
public abstract class TempFactory implements net.cscott.jutil.ReferenceUnique {
    int nextID = 0;
    int newID() {
	return nextID++;
    }

    /** Returns the static scope of this <code>TempFactory</code>.
     *  Should be unique among <code>TempFactory</code>s and invariant
     *  for a given <code>TempFactory</code>. */
    public abstract String getScope();

    /** Returns a unique identifier within this scope.  Not
     *  required to be unique among all <code>TempFactory</code>s.
     *  Should be repeatable; that is, the n'th call to getUniqueID()
     *  with a given suggestion String for a given <code>TempFactory</code>
     *  should always return the same String. */
    protected abstract String getUniqueID(String suggestion);

    /** Human-readable representation of <code>TempFactory</code> */
    public String toString() { return "TempFactory["+getScope()+"]"; }

    /** Return a hashcode for this <code>TempFactory</code>. */
    public int hashCode() { return getScope().hashCode(); }
}
