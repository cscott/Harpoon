// SimpleCheckRemoval.java, created Mon Jan 22 19:49:35 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>SimpleCheckRemoval</code> is the simplest implementation of
 * the interface <code>CheckRemoval</code>: it just keeps all the
 * checks!
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: SimpleCheckRemoval.java,v 1.1.2.2 2001-01-31 21:35:49 wbeebee Exp $ */
public class SimpleCheckRemoval implements CheckRemoval, NoHeapCheckRemoval {
    
    /** Creates a <code>SimpleCheckRemoval</code>. */
    public SimpleCheckRemoval() { }

    /** <i>Very</i> conservative treatment. */
    public boolean shouldRemoveCheck(Quad inst) { return false; }

    /** <i>Very</i> conservative treatment. */
    public boolean shouldRemoveNoHeapWriteCheck(Quad inst) { return false; }

    /** <i>Very</i> conservative treatment. */
    public boolean shouldRemoveNoHeapReadCheck(Quad inst) { return false; }    
}
