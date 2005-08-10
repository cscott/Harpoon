// PANode.java, created Sun Jun 26 14:31:12 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import harpoon.Analysis.MetaMethods.GenType;

/**
 * <code>PANode</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: PANode.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public abstract class PANode {

    public enum Kind { INSIDE, PARAM, LOAD, GBL, NULL, CONST, IMM }

    protected PANode(Kind kind, GenType type) {
	this.kind = kind;
	this.type = type;
	this.id   = idCounter++;
    }

    /** Kind of <code>this</code> node. */
    public final Kind kind;

    /** Conservative approximation of the type of objects represented
        by <code>this</code> node.  */
    public final GenType type;

    public int getId() { return id; }
    private final int id;
    private static int idCounter = 0;

    private PANode other = this;   
    public PANode other() { return other; }

    private boolean fresh = false;
    public boolean isFresh() { return fresh; }

    public void link(PANode brother) {
	assert this != brother;

	this.other = brother;
	this.fresh = false;

	brother.other = this;
	brother.fresh = true;	
    }
}
