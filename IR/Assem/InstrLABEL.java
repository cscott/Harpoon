// InstrLABEL.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;

/**
 * <code>InstrLABEL</code> is used to represents code labels in
 * assembly-level instruction representations.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrLABEL.java,v 1.1.2.3 1999-04-05 21:36:36 pnkfelix Exp $
 */
public class InstrLABEL extends Instr {
    public Label label;

    public InstrLABEL(InstrFactory inf, HCodeElement src, String a, Label l) {
        super(inf, src, a, null, null);
        label = l;
    } 

    /** Accept a visitor. 
	<BR> <B>NOTE:</B> for VISITOR pattern to work, all subclasses
	                  must override this method with the body:
			  { v.visit(this); }
     */
    public void visit(InstrVisitor v) { v.visit(this); }
}
