// Frame.java, created Fri Feb  5 05:48:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.Linker;

/**
 * A <code>Frame</code> encapsulates the machine-dependent information
 * needed for compilation.  <code>Frame</code>s are not intended to be
 * <i>entirely</i> machine-specific; all machines with roughly the same
 * datatypes (for example, 32-bit words) and which use the same runtime
 * implementation should be able to share most, if not all, of a
 * <code>Frame</code> implementation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Frame.java,v 1.1.2.43 2001-06-17 22:32:10 cananian Exp $
 * @see harpoon.IR.Assem
 */
public abstract class Frame {
    /** The <code>Linker</code> object to use when compiling for this
     *  <code>Frame</code>. */
    public abstract Linker getLinker();

    /** Returns <code>false</code> if pointers can be represented in
     *  32 bits, or <code>true</code> otherwise. */
    public abstract boolean pointersAreLong();

    /** Returns the appropriate <code>Generic.Runtime</code> for
     *  this <code>Frame</code>. */
    public abstract Runtime getRuntime();

    /** Returns the appropriate <code>InstrBuilder</code> for this
	<code>Frame</code>. 
    */
    public abstract InstrBuilder getInstrBuilder();

    /** Returns the appropriate <code>RegFileInfo</code> for this
	<code>Frame</code>.
    */
    public abstract RegFileInfo getRegFileInfo();

    /** Returns the appropriate <code>LocationFactory</code> for this
     *  <code>Frame</code>.
     */
    public abstract LocationFactory getLocationFactory();

    /** Returns the <code>Generic.CodeGen</code> for the backend
	associated with <code>this</code>.
     */
    public abstract CodeGen getCodeGen();

    /** Returns a code factory for machine-specific <code>IR.Assem</code>s,
     * given a code factory for <code>CanonicalTreeCode</code>.  Uses
     * the code generator defined by this frame.  Returns <code>null</code>
     * if this frame does not have a code factory that generates
     * <code>IR.Assem.Instr</code>s. */
    public abstract HCodeFactory getCodeFactory(HCodeFactory hcf);

    /** Returns the <code>Generic.TempBuilder</code> for the backend
	associated with <code>this</code>.
     */
    public abstract TempBuilder getTempBuilder();

    /** Returns the <code>GCInfo</code> for the backend
	associated with <code>this</code>. <code>null</code>
	may be returned in some cases. i.e. if precise
	garbage collection is not supported.
     */
    public abstract GCInfo getGCInfo();
}
