// NodeSetVar.java, created Tue Jun 28 14:25:24 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.HashSet;

import jpaul.Constraints.Var;

/**
 * <code>NodeSetVar</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: NodeSetVar.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public abstract class NodeSetVar extends Var {

    public Object copy(Object x) {
	return DSFactories.nodeSetFactory.create((Set<PANode>) x);
    }

    /** Joins two values from the domain <code>this</code>
        variable takes values from. */
    //public abstract Pair<Info,Boolean> join(Info x, Info y);
    public boolean join(Object x, Object y) {
	Set<PANode> esx = (Set<PANode>) x;
	Set<PANode> esy = (Set<PANode>) y;
	return esx.addAll(esy);
    }

}
