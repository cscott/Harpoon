// IgnoreSpillUseDefer.java, created Fri Jun 30 19:14:06 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Properties.UseDefable;
import harpoon.IR.Properties.UseDefer;
import harpoon.ClassFile.HCodeElement;
import java.util.Collection;
import java.util.Collections;

/**
 * <code>IgnoreSpillUseDefer</code> defines a view of the code that
 * will skip over newly inserted spill instructions when deciding what
 * <code>Temp</code>s instructions read and write.  This is useful for
 * the garbage collection analyses which need to know what Temps are
 * live in terms of the program itself, ignoring memory
 * communication due to spill code. 
 * <p>
 * For example, a SpillLoad from the stack location for <tt>t0</tt> to
 * <tt>r3</tt> is no longer considered a definition of <tt>t0</tt> by
 * this <code>UseDefer</code>.
 *
 * <p> This class may need to be updated to take a
 * <code>UseDefer</code> parameter, rather than relying on the
 * underlying IR implementing the <code>UseDefable</code> interface. 
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: IgnoreSpillUseDefer.java,v 1.1.2.9 2001-08-03 02:40:33 pnkfelix Exp $
 */
public class IgnoreSpillUseDefer extends UseDefer {

    public static IgnoreSpillUseDefer USEDEFER = 
	new IgnoreSpillUseDefer();
    
    /** Creates a <code>IgnoreSpillUDr</code>. */
    public IgnoreSpillUseDefer() {
        
    }
    
    public Collection useC(HCodeElement hce) {
	if (hce instanceof RegAlloc.SpillStore)
	    return Collections.EMPTY_SET;
	else 
	    return ((UseDefable)hce).useC();
    }

    public Collection defC(HCodeElement hce) {
	if (hce instanceof RegAlloc.SpillLoad)
	    return Collections.EMPTY_SET;
	else
	    return ((UseDefable)hce).defC();
    }
}
