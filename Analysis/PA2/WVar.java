// WVar.java, created Tue Aug 30 11:02:21 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import jpaul.DataStructs.Pair;

import jpaul.Constraints.Var;

import harpoon.ClassFile.HField;

/**
 * <code>WVar</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WVar.java,v 1.2 2005-09-02 19:22:52 salcianu Exp $
 */
public class WVar extends Var {

    public Object copy(Object x) {
	return DSFactories.abstractFieldSetFactory.create((Set<Pair<PANode,HField>>) x);
    }
    
    public boolean join(Object x, Object y) {
 	Set<Pair<PANode,HField>> esx = (Set<Pair<PANode,HField>>) x;

	for(Pair<PANode,HField> af : esx) {
	    PANode node = af.left;
	    if(node == null) continue;
	    if(node.isFresh()) throw new Error("why fresh?" + node);
	}

 	Set<Pair<PANode,HField>> esy = (Set<Pair<PANode,HField>>) y;
	return esx.addAll(esy);
    }

    private static int count = 0;
    private int id = count++;

    public String toString() { return "W" + id; }

}
