// LLDisjointSetStructure.java, created Mon Jan 10 13:48:49 2000 by pnkfelix
// Copyright (C) 1999 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.*;

/**
 * <code>LLDisjointSetStructure</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: LLDisjointSetStructure.java,v 1.1.2.1 2000-01-13 17:54:34 pnkfelix Exp $
 */
public class LLDisjointSetStructure extends DisjointSetStructure {

    class LLElem extends DisjointSetStructure.Elem {
	LLElem next;
	Set representative;
	LLElem(Object o, Set rep) {
	    super(o);
	    representative = rep;
	}
    }

    public Set setView(Elem o) { return null; }

    public Elem makeSet(Object o) { return null; }

    public Elem union(Elem x, Elem y) { return null; }

    public Elem findSet(Elem elem) { return null; }
}
