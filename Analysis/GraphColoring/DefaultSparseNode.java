// DefaultSparseNode.java, created Fri Nov  5 17:09:48 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>DefaultSparseNode</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: DefaultSparseNode.java,v 1.1.2.1 1999-11-05 22:32:17 pnkfelix Exp $
 */
public class DefaultSparseNode extends SparseNode {
    
    protected static int counter = 1;
    protected int id;

    /** Creates a <code>DefaultSparseNode</code>. */
    public DefaultSparseNode() {
        super();
	id = counter;
	counter++;
    }

    public boolean equals(Object o) {
	return this == o;
    }
    
    public int hashCode() {
	return System.identityHashCode(this);
    }

    public String toString() {
	return "DefaultNode["+hashCode()+"]";
    }

}

