// TreeVerifyingVisitor.java, created Mon Jan 10 20:40:46 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Util.Util;

import java.util.Iterator;
import java.util.HashSet;

/**
 * <code>TreeVerifyingVisitor</code> is a generic Tree Visitor for 
 * verifying particular properties about the Tree Intermediate
 * Representation for a given Tree.  Note that in general
 * <code>TreeVerifyingVisitor</code>s are not meant to be used in 
 * algorithm implementations or even in general assertions, but rather 
 * to debug particular errors in Tree construction.
 * 
 * In general, a Verifier for a given invariant will be written to
 * analyze a given Tree form and assert false when it finds a
 * violation of the invariant.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: TreeVerifyingVisitor.java,v 1.4 2002-04-10 03:05:46 cananian Exp $
 */
public abstract class TreeVerifyingVisitor extends TreeVisitor {
    
    /** Creates a <code>TreeVerifyingVisitor</code>. */
    public TreeVerifyingVisitor() { }

    

    public static TreeVerifyingVisitor norepeats() {
	return new NoRepeats();
    }

    public static final boolean DEBUG = true;

    public static class NoRepeats extends TreeVerifyingVisitor {
	HashSet haveSeen = new HashSet();

	boolean firstCall = true;
	Tree saw = null;
	
	public void visit(Tree e) {
	    if (!DEBUG) return;

	    boolean isFirstCall = firstCall;

	    if (e.kids() == null) return;

	    haveSeen.add(e);

	    Iterator iter = ExpList.iterator(e.kids());
	    while (iter.hasNext() && (saw == null)) {
		Tree o =(Tree) iter.next();
		assert o != null;
		if (haveSeen.contains(o)) {
		    saw = o;
		    break;
		}
		o.accept(this);
	    }

	    assert saw == null : ("should not have seen: "+saw+" in " + e);
	}

	public void visit(SEQ s) {
	    if(!DEBUG) return;
	    boolean isFirstCall = firstCall;
	    if (isFirstCall) firstCall = false;
	    if (s!= null && haveSeen.contains(s)) saw = s;

	    haveSeen.add(s);

	    s.getLeft().accept(this);
	    s.getRight().accept(this);

	    if (isFirstCall)
		assert saw == null : ("should not have seen: "+saw+ " in "+s);
	}
	public void visit(ESEQ s) {
	    if(!DEBUG) return;
	    boolean isFirstCall = firstCall;
	    if (isFirstCall) firstCall = false;
	    if(s != null && haveSeen.contains(s)) saw = s;

	    haveSeen.add(s);

	    s.getExp().accept(this);
	    s.getStm().accept(this);

	    if (isFirstCall)
		assert saw == null : ("should not have seen: "+saw+ " in "+s);
	}
    }
}
