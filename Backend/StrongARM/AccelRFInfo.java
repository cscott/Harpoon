// AccelRFInfo.java, created Mon Jul 10  8:43:42 2000 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;

import harpoon.Util.Util;
import harpoon.Util.Indexer;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.CombineIterator;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.AbstractSet;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * <code>AccelRFInfo</code> is an optimized version of
 * <code>RegFileInfo</code> that is designed to allocate less
 * temporary storage for the Iterators returned by
 * suggestRegAssignment(..) methods.
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: AccelRFInfo.java,v 1.2 2002-02-25 21:02:49 cananian Exp $
 */
class AccelRFInfo extends RegFileInfo {
    // rls stands for RegListSet

    BitSetFactory rlsFactSW; // single word
    BitSetFactory rlsFactDW; // double word
    Map regToRLSconflictSW; 
    Map regToRLSconflictDW; 

    static class LSet extends AbstractSet { 
	private List l;
	/** `list' may not contain any duplicates. */
	LSet(List list) { l = list; }
	public int size() { return l.size(); }
	public Iterator iterator() { return l.iterator(); }
	Indexer indexer() {
	    class HIndexer extends Indexer {
		public int getID(Object o) { return ((RegList)o).index(); }
		public boolean implementsReverseMapping() { return true; }
		public Object getByID(int i) { 
		    return l.get(i); 
		}
	    }
	    return new HIndexer();
	}
    }

    static class RegList extends ArrayList implements Comparable {
	int ind; // must be unique
	private RegList(int capacity, int index) {
	    super(capacity); ind = index;
	}
	RegList(Temp t, int index) { this(1, index); add(t); }
	RegList(Temp t1, Temp t2, int index) { 
	    this(2, index); add(t1); add(t2);
	}
	int index() { return ind; }
	public int compareTo(Object o) {
	    return this.index() - ((RegList)o).index();
	}
    }

    AccelRFInfo() { 
	super();

	final ArrayList reglistsSW = new ArrayList();
	final ArrayList reglistsDW = new ArrayList();

	{ // build static register lists

	    // single reg lists
	    int i;
	    for(i=0; i<regGeneral.length; i++) {
		RegList rl = new RegList(regGeneral[i], i);
		reglistsSW.add(i, rl);
	    }
	    
	    // double reg lists
	    i = 0;
	    for(int j=0; j<regGeneral.length-1; j+=2, i++ ) {
		RegList rl = new RegList(regGeneral[j], 
					 regGeneral[j+1], i);
		reglistsDW.add(i, rl);
	    }
	}

	LSet uniSW = new LSet(reglistsSW);
	LSet uniDW = new LSet(reglistsDW);

	rlsFactSW = new BitSetFactory(uniSW, uniSW.indexer());
	rlsFactDW = new BitSetFactory(uniDW, uniDW.indexer());
	regToRLSconflictSW = new HashMap();
	regToRLSconflictDW = new HashMap();

	{ // build conflict sets
	    for (int i=0; i<reg.length; i++) {
		Temp t = reg[i];

		Set conflictSetSW = rlsFactSW.makeSet();
		Iterator iterSW = uniSW.iterator();
		while(iterSW.hasNext()) {
		    RegList rl = (RegList) iterSW.next();
		    if (rl.contains(t)) 
			conflictSetSW.add(rl);
		}
		regToRLSconflictSW.put(t, conflictSetSW);

		Set conflictSetDW = rlsFactDW.makeSet();
		Iterator iterDW = uniDW.iterator();
		while(iterDW.hasNext()) {
		    RegList rl = (RegList) iterDW.next();
		    if (rl.contains(t)) 
			conflictSetDW.add(rl);
		}
		regToRLSconflictDW.put(t, conflictSetDW);

	    }
	}
    }
    
    public Iterator suggestRegAssignment(final Temp t, 
					 final Map regfile) 
	throws SpillException {
	Set regListSet;
	if (t instanceof TwoWordTemp) {
	    regListSet = rlsFactDW.makeFullSet();
	    Iterator regs = regfile.keySet().iterator();
	    while(regs.hasNext()) {
		Temp r = (Temp) regs.next();
		regListSet.removeAll((Set)regToRLSconflictDW.get(r));
	    }
	} else {
	    regListSet = rlsFactSW.makeFullSet();
	    Iterator regs = regfile.keySet().iterator();
	    while(regs.hasNext()) {
		Temp r = (Temp) regs.next();
		regListSet.removeAll((Set)regToRLSconflictSW.get(r));
	    }
	}

	if (!regListSet.isEmpty())
	    return regListSet.iterator();
	else 
	    throw new SpillException() {
	    public Iterator getPotentialSpills() {
		final Iterator regs;

		if (t instanceof TwoWordTemp) {
		    regs = rlsFactDW.makeFullSet().iterator();
		} else {
		    regs = rlsFactSW.makeFullSet().iterator();
		}

		return new harpoon.Util.UnmodifiableIterator() {
		    public boolean hasNext() {
			return regs.hasNext();
		    }
		    public Object next() {
			return new LSet((List)regs.next());
		    }
		};
	    }
	};
    }
}
