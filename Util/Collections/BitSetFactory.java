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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.AbstractSet;
import java.util.ArrayList;
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
    cause <code>IllegalArgumentException</code> to be thrown.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: BitSetFactory.java,v 1.4 2002-04-10 03:07:10 cananian Exp $
 */
public class BitSetFactory<V> extends SetFactory<V> {
    
    /** Maps each object in the universe for <code>this</code> to an
	index in the <code>BitString</code> for the <code>Set</code>s
	produced.
    */
    private Indexer<V> indexer;

    /** Size that each bit string needs to be.  Does not necessarily 
	equal the size of the universe itself, because the indices of
	the universe can skip values.   
    */
    private int bitStringSize;

    /** Universe of values for this. */
    private Set<V> universe; 

    /** Universe of values for this, represented as a BitSet.  (Used
	for makeFullSet). */
    private BitStringSet<V> bitUniverse = null;
    
    /** Creates a <code>BitSetFactory</code>, given a
	<code>universe</code> of values and an <code>Indexer</code>
	for the elements of <code>universe</code>. 
    */
    public BitSetFactory(final Set<V> universe, final Indexer<V> indexer) {
        final Iterator<V> vals = universe.iterator();
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
	<code>Indexer</code> for <code>universe</code>; the
	created <code>Indexer</code> will implement the
	<code>Indexer.getByID()</code> method to allow
	efficient iteration over sets.
    */
    public BitSetFactory(final Set<V> universe) {
	final HashMap<V,Integer> obj2int = new HashMap<V,Integer>();
	final ArrayList<V> int2obj = new ArrayList<V>();
	final Iterator<V> iter = universe.iterator();
	this.universe = universe;
	int i;
	for(i=0; iter.hasNext(); i++) {
	    V o = iter.next();
	    obj2int.put(o, new Integer(i));
	    int2obj.add(i, o); 
	}
	this.bitStringSize = i+1;
	this.indexer = new Indexer<V>() {
	    public int getID(V o) {
		return obj2int.get(o).intValue();
	    }
	    public V getByID(int id) {
		return int2obj.get(id);
	    }
	    public boolean implementsReverseMapping() { return true; }
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
    public <T extends V> BitStringSet<V> makeSet(Collection<T> c) {
	BitStringSet<V> bss = new BitStringSet<V>(bitStringSize, this);
	bss.addAll(c);
	return bss;
    }
    
    /** Generates a new mutable <code>Set</code>, using the elements
	of the universe for <code>this</code> as its initial contents.
    */
    public Set<V> makeFullSet() {
	if (bitUniverse == null) 
	    bitUniverse = makeSet(universe);

	return (Set<V>) bitUniverse.clone();
    }

    private static class BitStringSet<V> extends AbstractSet<V>
	implements Cloneable {
	// internal rep for set
	BitString bs;

	// ensure that sets come from same factory
	// when doing optimized operations. 
	BitSetFactory<V> fact; 

	BitStringSet(int size, BitSetFactory<V> fact) {
	    this.bs = new BitString(size);
	    this.fact = fact;
	}

	public boolean add(V o) {
	    if (!fact.universe.contains(o)) 
		throw new IllegalArgumentException
		    ("Attempted to add an object: "+o+
		     "that was not part of the "+
		     "original universe of values.");
	    
	    int ind = fact.indexer.getID(o);
	    boolean alreadySet = this.bs.get(ind);
	    if (alreadySet) {
		return false;
	    } else {
		this.bs.set(ind);
		return true;
	    }
	}

	public <T extends V> boolean addAll(Collection<T> c) {
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitStringSet bss = (BitStringSet) c;
		return this.bs.or(bss.bs);
	    } else return super.addAll(c);
	}
	
	public void clear() {
	    this.bs.clearAll();
	}
	
	public boolean contains(Object o) {
	    if (fact.universe.contains(o)) {
		int i = fact.indexer.getID((V)o);
		return this.bs.get(i);
	    } else {
		// not part of original universe, therefore cannot be
		// a member of the set.
		return false;
	    }
	}

	public <T> boolean containsAll(Collection<T> c) {
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
	    } else return super.containsAll(c);
	}
	
	public BitStringSet<V> clone() {
	    try {
		BitStringSet<V> bss = (BitStringSet) super.clone();
		bss.bs = this.bs.clone();
		return bss;
	    } catch (CloneNotSupportedException e) {
		assert false;
		return null;
	    }
	}

	public boolean equals(Object o) {
	    if (o==null) return false;
	    if (o==this) return true;
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
	
	public Iterator<V> iterator() {
	    return fact.indexer.implementsReverseMapping() ?
	      (Iterator<V>) new Iterator<V>() { // fast bit-set iterator
		int lastindex=-1;
		public boolean hasNext() {
		    return BitStringSet.this.bs.firstSet(lastindex)!=-1;
		}
		public V next() {
		    lastindex = BitStringSet.this.bs.firstSet(lastindex);
		    if (lastindex<0) throw new NoSuchElementException();
		    return fact.indexer.getByID(lastindex);
		}
		public void remove() {
		    if (lastindex<0 || !BitStringSet.this.bs.get(lastindex))
			throw new IllegalStateException();
		    BitStringSet.this.bs.clear(lastindex);
		}
	    } : new Iterator<V>() { // slower fall-back
		    // need to wrap a *modifiable* iterator 
		    // around an internal filter iterator...
		    Iterator<V> internIter = new FilterIterator<V,V>
			(fact.universe.iterator(), 
			 new FilterIterator.Filter<V,V>() {
				 public boolean isElement(V o) {
				     return BitStringSet.this.bs.get
					 (fact.indexer.getID(o));
				 }});
		    V last = null;
		    public V next() {
			last = internIter.next();
			return last;
		    }
		    public boolean hasNext() {
			return internIter.hasNext();
		    }
		    public void remove() {
			BitStringSet.this.bs.clear(fact.indexer.getID(last));
		    }};
	}
	
	public boolean remove(Object o) {
	    if (!fact.universe.contains(o)) {
		// o is not member of universe, therefore cannot be in set.
		return false;
	    } else {
		int i = fact.indexer.getID((V)o);
		boolean alreadySet = bs.get(i);
		if (alreadySet) {
		    this.bs.clear(i);
		    return true;
		} else {
		    return false;
		}
	    }
	}

	public <T> boolean removeAll(Collection<T> c) {
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitStringSet bss = (BitStringSet) c;
		BitString notBSS = (BitString) bss.bs.clone();
		notBSS.setAll(); // -> string of ones
		notBSS.xor(bss.bs); // -> complement(bss)
		return this.bs.and(notBSS);
	    } else if (c.size() < this.size()) {
		// optimization hack; super.removeAll takes time
		// proportional to this.size()
		boolean changed = false;
		for(Iterator i=c.iterator(); i.hasNext();) {
		    changed |= remove(i.next());
		}
		return changed;
	    } else {		
		return super.removeAll(c); // slower generic implementation
	    }
	}

	public <T> boolean retainAll(Collection<T> c) {
	    if (c instanceof BitStringSet &&
		((BitStringSet)c).fact == this.fact) {
		BitStringSet bss = (BitStringSet) c;
		return this.bs.and(bss.bs);
	    } else {
		return super.retainAll(c); // slower generic implementation
	    }
	}

	public int size() {
	    return this.bs.numberOfOnes();
	}

	// inherit implementations for toArray() and
	// toArray(Object[]) methods from AbstractSet
    }
    
}


