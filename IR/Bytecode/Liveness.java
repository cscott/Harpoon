// Liveness.java, created Mon Feb 22 23:03:30 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.cscott.jutil.FilterIterator;
import net.cscott.jutil.UnmodifiableIterator;
import net.cscott.jutil.WorkSet;
/**
 * <code>Liveness</code> is a local-variable liveness analysis on Bytecode
 * form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Liveness.java,v 1.4 2004-02-08 01:55:10 cananian Exp $
 */
public class Liveness  {
    /** internal data structure is a hashtable of boolean arrays */
    private LiveMap live;
    /** Return the liveness of local variable #<code>lv_index</code> at
     *  instruction <code>where</code>. */
    public boolean isLive(Instr where, int lv_index) {
	return live.get(where).isLive(lv_index);
    }
    /** Return a <code>Collection</code> of live local variables at the
     *  given instruction <code>where</code>. */
    public Set liveSet(Instr where) {
	return live.get(where).asCollection();
    }
    
    /** Creates a <code>Liveness</code>. */
    public Liveness(Code bcode) {
	int max_locals = bcode.getMaxLocals();
	this.live = new LiveMap(max_locals);
	/* add all elements to the workset initially. */
	WorkSet workset = new WorkSet(bcode.getElementsL());
        /* iterate until stable. */
	while (!workset.isEmpty()) {
	    Instr in = (Instr) workset.pop();
	    LiveSet liveIn = new LiveSet(max_locals);
	    if (Op.JSR == in.getOpcode() || Op.JSR_W == in.getOpcode()) {
		// jsrs are special: live set of subroutine except where
		// undefined, where we use live set of next instr.
		LiveSet liveNext=live.get(in.next(0));//live set of next instr
		liveIn.or(live.get(in.next(1))); // live set of subroutine.
		for (int i=0; i<max_locals; i++)
		    // for those locals which the subroutine doesn't touch...
		    if (!liveIn.isDefined(i) && liveNext.isDefined(i))
			// use the liveSet of the next instruction.
			if (liveNext.isLive(i))
			    liveIn.markLive(i);
			else
			    liveIn.markDead(i);
	    } else {
		// if it is in the live set of a successor and I don't define
		// it, then it is in the live set here.
		for (Iterator i=in.next.iterator(); i.hasNext(); )
		    liveIn.or(live.get((Instr) i.next()));
	    }
	    // if it is defined here, then it is dead.
	    int defs = def(in);
	    if (defs != -1) {
		liveIn.markDead(defs);
		if (isLong(in))
		    liveIn.markDead(defs+1);
	    }
	    // if this instruction uses a local variable, it is in the live set
	    // (note how liveness beats deadness in the case of Op.IINC)
	    int uses = use(in);
	    if (uses!=-1) {
		liveIn.markLive(uses);
		if (isLong(in)) 
		    liveIn.markLive(uses+1);
	    }
	    // optimization hack: try to share LiveSets.
	    if (in.next.size()>0 && live.get(in.next(0)).equals(liveIn))
		liveIn = live.get(in.next(0)); // share livesets if equal.

	    // if live set has changed, add predecessors to work set
	    boolean changed = live.update(in, liveIn);
	    if (changed)
		for (Iterator i=in.prev.iterator(); i.hasNext(); )
		    workset.add(i.next());
	}
    }
    // opcode properties.
    private static int def(Instr in) {
	switch(in.getOpcode()) {
	case Op.ASTORE:
	case Op.ASTORE_0: case Op.ASTORE_1:
	case Op.ASTORE_2: case Op.ASTORE_3:
	case Op.FSTORE:
	case Op.FSTORE_0: case Op.FSTORE_1:
	case Op.FSTORE_2: case Op.FSTORE_3:
	case Op.ISTORE:
	case Op.ISTORE_0: case Op.ISTORE_1:
	case Op.ISTORE_2: case Op.ISTORE_3:
	case Op.DSTORE:
	case Op.DSTORE_0: case Op.DSTORE_1:
	case Op.DSTORE_2: case Op.DSTORE_3:
	case Op.LSTORE:
	case Op.LSTORE_0: case Op.LSTORE_1:
	case Op.LSTORE_2: case Op.LSTORE_3:
	case Op.IINC: // both use *and* definition.
	    return ((OpLocalVariable)((InGen)in).getOperand(0)).getIndex();
	default:
	    return -1;
	}
    }
    private static int use(Instr in) {
	switch(in.getOpcode()) {
	case Op.ALOAD:
	case Op.ALOAD_0: case Op.ALOAD_1:
	case Op.ALOAD_2: case Op.ALOAD_3:
	case Op.FLOAD:
	case Op.FLOAD_0: case Op.FLOAD_1:
	case Op.FLOAD_2: case Op.FLOAD_3:
	case Op.ILOAD:
	case Op.ILOAD_0: case Op.ILOAD_1:
	case Op.ILOAD_2: case Op.ILOAD_3:
	case Op.DLOAD:
	case Op.DLOAD_0: case Op.DLOAD_1:
	case Op.DLOAD_2: case Op.DLOAD_3:
	case Op.LLOAD:
	case Op.LLOAD_0: case Op.LLOAD_1:
	case Op.LLOAD_2: case Op.LLOAD_3:
	case Op.IINC: // both use *and* definition.
	    return ((OpLocalVariable)((InGen)in).getOperand(0)).getIndex();
	case Op.RET: // use of local variable.
	    return ((OpLocalVariable)((InRet)in).getOperand()).getIndex();
	default:
	    return -1;
	}
    }
    private static boolean isLong(Instr in) {
	switch (in.getOpcode()) {
	case Op.DLOAD:
	case Op.DLOAD_0: case Op.DLOAD_1:
	case Op.DLOAD_2: case Op.DLOAD_3:
	case Op.LLOAD:
	case Op.LLOAD_0: case Op.LLOAD_1:
	case Op.LLOAD_2: case Op.LLOAD_3:
	case Op.DSTORE:
	case Op.DSTORE_0: case Op.DSTORE_1:
	case Op.DSTORE_2: case Op.DSTORE_3:
	case Op.LSTORE:
	case Op.LSTORE_0: case Op.LSTORE_1:
	case Op.LSTORE_2: case Op.LSTORE_3:
	    return true;
	default:
	    return false;
	}
    }
    // liveness bitset
    private class LiveSet extends java.util.BitSet {
	LiveSet(int max_locals) { super(2*max_locals); }
	// treat bits in pairs: (defined, live)
	// so that: 0X = undefined, 10 = defined but dead, 11 = live.
	public boolean isDefined(int which_lv) {
	    return get(2*which_lv);
	}
	public boolean isLive(int which_lv) {
	    return get(2*which_lv)/*defined*/ && get(2*which_lv+1)/*live*/;
	}
	public void markDead(int which_lv) {
	    set(2*which_lv); // defined.
	    clear(2*which_lv+1); // but dead.
	}
	public void markLive(int which_lv) {
	    set(2*which_lv); // defined
	    set(2*which_lv+1); // and live.
	}
	// collection view.
	public Set asCollection() {
	    final int lvsize = size()/2;
	    return new AbstractSet() {
		public int size() {
		    int size=0;
		    for (int i=0; i < lvsize; i++)
			if (isLive(i)) size++;
		    return size;
		}
		public Iterator iterator() {
		    // simple integer iterator
		    Iterator i = new UnmodifiableIterator() {
			int i=0;
			public boolean hasNext() { return (i < lvsize); }
			public Object next() { return new Integer(i++); }
		    };
		    // filtered by liveness method.
		    return new FilterIterator(i, new FilterIterator.Filter() {
			public boolean isElement(Object o) {
			    return isLive(((Integer)o).intValue());
			}
		    });
		}
	    };
	}
    }

    // liveness map data structure.
    private class LiveMap {
	private final HashMap hm = new HashMap();

	private final LiveSet EMPTY; // all zero.
	LiveMap(int max_locals) {
	    this.EMPTY = new LiveSet(max_locals);
	}
        LiveSet get(Instr in) {
	    if (!hm.containsKey(in)) return EMPTY;
	    return (LiveSet) hm.get(in);
	}
	boolean update(Instr in, LiveSet newset) {
	    if (get(in).equals(newset)) return false;
	    hm.put(in, newset);
	    return true;
	}
    }
}
