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
 * temporary storage during execution.
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
		public Object getByID(int index) { return l.get(index); }
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
	    for (int i=0; i<regGeneral.length; i++) {
		Temp t = regGeneral[i];

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
    
    public Iterator suggestRegAssignment(Temp t, final Map regfile) 
	throws SpillException {
	if (t instanceof TwoWordTemp) {
	    Set regListSet = rlsFactDW.makeFullSet();
	    Iterator regs = regfile.keySet().iterator();
	    while(regs.hasNext()) {
		Temp r = (Temp) regs.next();
		regListSet.removeAll((Set)regToRLSconflictDW.get(r));
	    }
	    return regListSet.iterator();
	} else {
	    Set regListSet = rlsFactSW.makeFullSet();
	    Iterator regs = regfile.keySet().iterator();
	    while(regs.hasNext()) {
		Temp r = (Temp) regs.next();
		regListSet.removeAll((Set)regToRLSconflictSW.get(r));
	    }
	    return regListSet.iterator();
	}
    }
}
