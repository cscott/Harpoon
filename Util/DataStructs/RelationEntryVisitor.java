// RelationEntryVisitor.java, created Sat Feb 12 14:31:04 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.DataStructs;

/**
 * <code>RelationEntryVisitor</code> is a wrapper for a function that is
 called on a relation entry of the form <code>&lt;key,value&gt;</code>.
 There is no other way to pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: RelationEntryVisitor.java,v 1.2 2002-02-25 21:09:19 cananian Exp $
 */
public interface RelationEntryVisitor {
    /** Visits a <code>&lt;key,value&gt;</code> entry of a relation. */
    public void visit(Object key, Object value);
}
