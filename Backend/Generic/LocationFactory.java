// LocationFactory.java, created Tue Oct 12 19:06:17 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HData;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
/**
 * The <code>LocationFactory</code> interface provides a means for
 * the runtime system to get access to some global storage for its
 * use.  For example, the memory manager may might to store pointer
 * to the current top-of-heap and top-of-memory in some place accessible
 * by every method.  The locations returned may be global registers
 * (in which case the <code>Generic.LocationFactory</code> must coordinate
 * with <code>Generic.RegFileInfo</code> to ensure those registers
 * are not used for other purposes by the register allocator) or
 * static locations in memory (useful on register-constrained machines).
 * If static storage is allocated, the <code>makeLocationData()</code>
 * method will generate an <code>HData</code> containing a label and
 * the appropriate declaration for the storage; otherwise it will
 * return an empty <code>HData</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LocationFactory.java,v 1.3 2003-04-19 01:03:44 salcianu Exp $
 */
public interface LocationFactory extends java.io.Serializable {
    /** The <code>Location</code>s returned by the 
     * <code>LocationFactory</code> allocation function
     * are opaque data structures that permit only the creation of
     * an accessor fragment in Tree form.
     */
    public static interface Location {
	/** Return a <code>Tree.Exp</code> which can be used to address
	 *  the contents of this <code>Location</code> for a fetch or
	 *  set operation.  For a global register, the returned value
	 *  will typically be an instance of <code>Tree.TEMP</code>;
	 *  for a memory location one would expect an instance of
	 *  <code>Tree.MEM</code>.  The accessor may return any valid
	 *  fragment of Tree code, though.  As Tree form may not
	 *  reuse subtrees, a new <code>Tree.Exp</code> object should
	 *  be returned every time this method is called.<p>
	 *  The given <code>TreeFactory</code> is used to create
	 *  the accessor fragment.
	 */
	Exp makeAccessor(TreeFactory tf, HCodeElement source);
    }
    /** Allocate a global location of the specified type and return
     *  a handle to it.  <strong>This method may not be called after
     *  <code>makeLocationData()</code> has been invoked</strong>.  It
     *  is suggested that this constraint is checked in any
     *  implementation using a boolean flag to help ensure correctness.
     *  @param type a <code>IR.Tree.Type</code> specifying the type
     *              of location to allocate.
     */
    Location allocateLocation(int type);
    /** Create an <code>HData</code> object that allocates static space
     *  for any allocated locations which need it.
     *  <strong>The <code>allocateLocation</code> method may not be
     *  called after this method has been invoked</strong>.
     *  @param f the <code>Generic.Frame</code> to which the
     *           <code>HData</code> will belong.
     */
    HData makeLocationData(Frame f);
}
