// SimpleCheckRemoval.java, created Mon Jan 22 19:49:35 2001 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>SimpleCheckRemoval</code> is the simplest implementation of
 * the interface <code>CheckRemoval</code>: it just keeps all the
 * checks!
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: SimpleCheckRemoval.java,v 1.2 2002-02-25 20:59:47 cananian Exp $ */
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
