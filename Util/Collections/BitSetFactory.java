// BitSetFactory.java, created Mon Nov  1 11:18:52 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.BitString;
import harpoon.Util.Util;
import harpoon.Util.Indexer;
import harpoon.Util.FilterIterator;


import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.AbstractSet;
import java.util.HashMap;

/** <code>BitSetFactory</code> is a <code>SetFactory</code> that,
    given a complete universe of possible values, produces low space
    overhead representations of <code>Set</code>s. 
    
    Notably, the <code>Set</code>s produced should have union,
    intersection, and difference operations that, while still O(n),
    have <b>blazingly</b> low constant factors.

    The addition operations (<code>Set.add(Object)</code> and its
    cousins) are only defined for objects that are part of the
    universe of values given to the constructor; other Objects will
    cause an assertion failure.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: BitSetFactory.java,v 1.1.2.8 2000-02-02 04:17:32 pnkfelix Exp $
 */
public class BitSetFactory extends SetFactory {
    
    /** Maps each object in the universe for <code>this</code> to an
	index in the <code>BitString</code> for the <code>Set</code>s
	produced.
    */
    private Indexer indexer;

    /** Size that each bit string needs to be.  Does not necessarily 
	equal the size of the universe itself, because the indices of
	the universe can skip values.   
    */
    private int bitStringSize;

    /** Universe of values for this. */
    private Set universe; 
    
    /** Creates a <code>BitSetFactory</code>, given a
	<code>universe</code> of values and an <code>Indexer</code>
	for the elements of <code>universe</code>. 
    */
    public BitSetFactory(final Set universe, final Indexer indexer) {
        final Iterator vals = universe.iterator();
	this.indexer = indexer;
	this.universe = universe;
	int max = 0;
	while(vals.hasNext()) {
	    int i = indexer.getID(vals.next());
	    if (i > max) max = i;
	}
	this.bitStringSize = max+1;
    }

    /** Creates a <code>BitSetFactory</code>, given a
	<code>universe</code> of values.  Makes a new
	<code>Indexer</code> for <code>universe</code>.
    */
    public BitSetFactory(final Set universe) {
	final HashMap obj2int = new HashMap();
	final Iterator iter = universe.iterator();
	this.universe = universe;
	int i;
	for(i=0; iter.hasNext(); i++) {
	    obj2int.put(iter.next(), new Integer(i));
	}
	this.bitStringSize = i+1;
	this.indexer = new Indexer() {
	    public int getID(Object o) {
		return ((Integer)obj2int.get(o)).intValue();
	    }
	};

    }
    
    /** Generates a new mutable <code>Set</code>, using the elements
	of <code>c</code> as a template for its initial contents. 
	<BR> <B>requires:</B> All of the elements of <code>c</code>
	     must have been part of the universe for
	     <code>this</code>. 
	<BR> <B>effects:</B> Constructs a lightweight
	     <code>Set</code> with the elements from <code>c</code>.
    */ 
    public Set makeSet(Collection c) {
	BitStringSet bss = new BitStringSet(bitStringSize, this);
	bss.addAll(c);
	return bss;
    }

    private class BitStringSet extends AbstractSet {
	// internal rep for set
	BitString bs;

	// ensure that sets come from same factory
	// when doing optimized operations. 
	BitSetFactory fact; 

	BitStringSet(int size, BitSetFactory fact) {
	    this.bs = new BitString(size);
	    this.fact = fact;
	}

	public boolean add(Object o) {
	    Util.assert(universe.contains(o),
			"Attempted to add an object "+
			"that was not part of the "+
			"original universe of values.");
	    int ind = indexer.getID(o);
	    Util.assert(ind < bs.size());
	    boolean alreadySet = this.bs.get(ind);
	    if (alreadySet) {
		return false;
	    } else {
		this.bs.set(ind);
		return true;
	    }
	}

	public boolean addAll(Collection c) {
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitStringSet bss = (BitStringSet) c;
		return this.bs.or(bss.bs);
	    } else {
		Iterator vals = c.iterator();
		boolean changed = false;
		while(vals.hasNext()) {
		    changed |= add(vals.next());
		}
		return changed;
	    }
	}
	
	public void clear() {
	    this.bs.clearAll();
	}
	
	public boolean contains(Object o) {
	    if (universe.contains(o)) {
		int i = indexer.getID(o);
		return this.bs.get(i);
	    } else {
		// not part of original universe, therefore cannot be
		// a member of the set.
		return false;
	    }
	}

	public boolean containsAll(Collection c) {
	    // check that ('c' - this) is nullset
	    // (which is the same as C /\ NOT(this) )
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitString notBS = 
		    (BitString) this.bs.clone();
		notBS.setAll(); // -> string of ones
		notBS.xor(this.bs);  // -> complement(bs)

		BitStringSet bss = (BitStringSet) c;
		return bss.bs.intersectionEmpty(notBS);
	    } else {
		Iterator vals = c.iterator();
		boolean contained = true;
		while(vals.hasNext()) {
		    contained &= contains(vals.next());
		}
		return contained;
	    }
	}
	
	public boolean equals(Object o) {
	    try {
		BitStringSet bss = (BitStringSet) o;
		return this.bs.equals(bss.bs);
	    } catch (ClassCastException e) {
		return false;
	    }
	}
	
	public int hashCode() {
	    return this.bs.hashCode() - 1;
	}
	
	public boolean isEmpty() {
	    return this.bs.isZero();
	}
	
	public Iterator iterator() {
	    return new FilterIterator
		(universe.iterator(), 
		 new FilterIterator.Filter() {
		    public boolean isElement(Object o) {
			return BitStringSet.this.bs.get(indexer.getID(o));
		    }});
	}
	
	public boolean remove(Object o) {
	    if (universe.contains(o)) {
		// o is not member of universe, therefore cannot be in set.
		return false;
	    } else {
		int i = indexer.getID(o);
		boolean alreadySet = bs.get(i);
		if (alreadySet) {
		    this.bs.clear(i);
		    return true;
		} else {
		    return false;
		}
	    }
	}

	public boolean removeAll(Collection c) {
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitStringSet bss = (BitStringSet) c;
		BitString notBSS = (BitString) bss.bs.clone();
		notBSS.setAll(); // -> string of ones
		notBSS.xor(bss.bs); // -> complement(bss)
		return this.bs.and(notBSS);
	    } else {
		Iterator vals = c.iterator();
		boolean changed = false;
		while(vals.hasNext()) {
		    changed |= remove(vals.next());
		}
		return changed;
	    }
	}

	public boolean retainAll(Collection c) {
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitStringSet bss = (BitStringSet) c;
		return this.bs.and(bss.bs);
	    } else {
		Iterator vals = this.iterator();
		boolean changed = false;
		while(vals.hasNext()) {
		    Object val = vals.next();
		    if (!c.contains(val)) {
			this.remove(val);
			changed = true;
		    }
		}
		return changed;
	    }
	}

	public int size() {
	    return this.bs.numberOfOnes();
	}

	// inherit implementations for toArray() and
	// toArray(Object[]) methods from AbstractSet
    }
    
}


