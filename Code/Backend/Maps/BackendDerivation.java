// BackendDerivation.java, created Tue Feb 29 12:03:41 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
/**
 * <code>BackendDerivation</code> provides, in addition to the type and
 * derivation information provided by <code>Analysis.Maps.Derivation</code>,
 * a means to identify (polymorphically typed) callee-save registers.
 * <p>
 * Note that <code>BackendDerivation</code> extends <code>Derivation</code>,
 * so non-callee-save-register values will continue to have class and
 * derivation types.  The <code>typeMap()</code> and <code>derivation()</code>
 * functions should both return <code>null</code> if the temporary in
 * question is holding a callee-save register value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BackendDerivation.java,v 1.2 2002-02-25 21:01:55 cananian Exp $
 */
public interface BackendDerivation extends harpoon.Analysis.Maps.Derivation {

    /** Map compiler temporaries to the callee-save register location
     *  they correspond to.
     * @param t The temporary to query.
     * @param hce A definition point for <code>t</code>.
     * @return <code>null</code> if the temporary does not hold the
     *         value of a callee-save register, or else the identity
     *         of the callee-save register.
     * @exception NullPointerException if <code>t</code> or <code>hc</code>
     *            is <code>null</code>.
     * @exception TypeNotKnownException if the <code>BackendDerivation</code>
     *            has no information about <code>t</code> as defined
     *            at <code>hce</code>.
     */
    public Register calleeSaveRegister(HCodeElement hce, Temp t);

    public static interface Register {
	/** Returns the index of this register in the machine register file.
	 *  This index is zero based and dense; it may not map directly
	 *  to this register's position in the register file.  For example,
	 *  the first usable/local register in the register file will have
	 *  index 0, even if it is logically designated, say, "r8".  The
	 *  register indexes returned here must be consistent with those
	 *  returned by
	 *  <code>Backend.Generic.RegFileInfo.MachineRegLoc</code>.
	 */
	public int regIndex();
    }
}
