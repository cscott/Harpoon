// BitString.java, created Wed Mar 10  8:57:56 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

/**
 * <code>BitString</code> implements a vector of bits
 * <!--that grows as needed-->;
 * much like <code>java.util.BitSet</code>... except that this implementation
 * actually works.  Also, <code>BitString</code> has some groovy features
 * which <code>BitSet</code> doesn't; mostly related to efficient iteration
 * over <code>true</code> and <code>false</code> components, I think.
 * <p>
 * Each component of the <code>BitString</code> has a boolean value.
 * The bits of a <code>BitString</code> are indexed by non-negative
 * integers (that means they are zero-based, of course).  Individual
 * indexed bits can be examined, set, or cleared.  One
 * <code>BitString</code> may be used to modify the contents of another
 * <code>BitString</code> through logical AND, logical inclusive OR,
 * and logical exclusive OR operations.
 * <p>
 * By default, all bits in the set initially have the value 
 * <code>false</code>.
 * <p>
 * Every bit set has a current size, which is the number of bits of
 * space currently in use by the bit set.  Note that the size is related
 * to the implementation of a bit set, so it may change with implementation.
 * The length of a bit set related to the logical length of a bit set
 * and is defined independently of implementation.
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BitString.java,v 1.3.2.2 2002-03-14 10:18:28 cananian Exp $
 */

public final class BitString implements Cloneable, java.io.Serializable {
  /* There are 2^BITS_PER_UNIT bits in each unit (int) */
  private final static int BITS_PER_UNIT = 5;
  private final static int MASK = (1<<BITS_PER_UNIT)-1;
  private int bits[];

  /**
   * Convert bitIndex to a subscript into the bits[] array.
   */
  private static int subscript(int bitIndex) {
    return bitIndex >> BITS_PER_UNIT;
  }
  /**
   * Convert a subscript into the bits[] array to a (maximum) bitIndex.
   */
  private static int bitIndex(int subscript) {
    return (subscript << BITS_PER_UNIT) + MASK;
  }
  
  /**
   * Creates an empty string of default length.
   */
  // THIS IS DANGEROUS!
  // YOU MUST SPECIFY LENGTH OF BITSTRING, SINCE IT DOESN'T GROW AUTOMATICALLY
  /*
  public BitString() {
    this(1);
  }
  */
  
  /**
   * Creates an empty string with the specified size.
   * @param nbits the size of the string
   */
  public BitString(int nbits) {
    /* subscript(nbits + MASK) is the length of the array needed to hold
     * nbits.  Can also be written 1+subscript(nbits-1). */
    bits = new int[subscript(nbits + MASK)];
  }

  /** Returns the first index in the bit string which is set, or
   *  -1 if there is no such index.
   */
  public int firstSet() { return firstSet(-1); }
  /** Returns the first index greater than <code>where</code> in the
   *  bit string which is set, or -1 if there is no such index.
   * @param where the starting point for the search.  May be negative.
   */
  public int firstSet(int where) {
    // convert exclusive starting point to inclusive starting point
    where = (where<-1) ? 0 : (where+1);
    // search in first unit is masked.
    int mask = (~0) << (where & MASK);
    // search through units
    for (int i=subscript(where); i<bits.length; i++, mask=~0) {
      int unit = bits[i] & mask;
      if (unit!=0) return (i << BITS_PER_UNIT) + (Util.ffs(unit) - 1);
    }
    return -1;
  }
  /** Returns the last index less than <code>where</code> in the
   *  bit string which is set, or -1 if there is no such index.
   * @param where the starting point for the search.
   */
  public int lastSet(int where) {
    // convert exclusive starting point to inclusive starting point
    if (--where < 0) return -1;
    int start = (bits.length - 1), mask=~0;
    if (subscript(where) < bits.length) {
      // search in first unit is masked.
      start = subscript(where);
      mask = (~0) >>> (MASK - (where & mask));
    }
    // search through units
    for (int i=start; i>=0; i--, mask=~0) {
      int unit = bits[i] & mask;
      if (unit!=0) return (i << BITS_PER_UNIT) + (Util.fls(unit) - 1);
    }
    return -1;
  }
  /** Returns the last index in the bit string which is set, or
   *  -1 if there is no such index.
   */
  public int lastSet() { return lastSet(size()); }

  /**
   * Sets all bits.
   */
  public void setAll() {
    int i = bits.length;
    while (i-- > 0) {
      bits[i] = ~0;
    }
  }

  /**
   * Sets all bits up to and including the given bit.
   * @param bit the bit to be set up to (zero-based)
   */
  public void setUpTo(int bit) {
    int where = subscript(bit);
    /* preaddition of 1 to bit is a clever hack to avoid long arithmetic */
    bits[where] |= ((1 << ((bit+1) & MASK)) - 1);
    while (where-- > 0) {
      bits[where] = ~0;
    }
  }

  /**
   * Sets a bit.
   * @param bit the bit to be set (zero-based)
   */
  public void set(int bit) {
    bits[subscript(bit)] |= (1 << (bit & MASK));
  }
  
  /**
   * Clears all bits.
   */
  public void clearAll() {
    int i = bits.length;
    while (i-- > 0) {
      bits[i] = 0;
    }
  }

  /**
   * Clears all bits up to and including the given bit.
   * @param bit the bit to be set up to (zero-based)
   */
  public void clearUpTo(int bit) {
    int where = subscript(bit);
    /* preaddition of 1 to bit is a clever hack to avoid long arithmetic */
    bits[where] &= ~((1 << ((bit+1) & MASK)) - 1);
    while (where-- > 0) {
      bits[where] = 0;
    }
  }

  /**
   * Clears a bit.
   * @param bit the bit to be cleared (zero-based)
   */
  public void clear(int bit) {
    bits[subscript(bit)] &= ~(1 << (bit & MASK));
  }

  /**
   * Gets a bit.
   * @param bit the bit to be gotten (zero-based)
   */
  public boolean get(int bit) {
    int n = subscript(bit);
    return ((bits[n] & (1 << (bit & MASK))) != 0);
  }
  
  /**
   * Logically ANDs this bit set with the specified set of bits.
   * Returns <code>true</code> if <code>this</code> was modified in
   * response to the operation. 
   * @param set the bit set to be ANDed with
   */
  public boolean and(BitString set) {
    if (this == set) { // should help alias analysis
      return false;
    }
    int n = bits.length;
    boolean changed = false;
    for (int i = n ; i-- > 0 ; ) {
      int old = bits[i];
      bits[i] &= set.bits[i];
      changed |= (old != bits[i]);
    }
    return changed;
  }
  
  /**
   * Logically ORs this bit set with the specified set of bits.
   * Returns <code>true</code> if <code>this</code> was modified in
   * response to the operation. 
   * @param set the bit set to be ORed with
   */
  public boolean or(BitString set) {
    if (this == set) { // should help alias analysis
      return false;
    }
    int setLength = set.bits.length;
    boolean changed = false;
    for (int i = setLength; i-- > 0 ;) {
      int old = bits[i];
      bits[i] |= set.bits[i];
      changed |= (old != bits[i]);
    }
    return changed;
  }
  
  /**
   * Logically ORs this bit set with the specified set of bits.
   * Returns <code>true</code> if <code>this</code> was modified in
   * response to the operation. 
   * @param set the bit set to be ORed with
   */
  public boolean or_upTo(BitString set, int bit) {
    if (this == set) { // should help alias analysis
      return false;
    }
    boolean result;
    int where = subscript(bit);
    int old = bits[where];
    bits[where] |= (set.bits[where] & ((1 << ((bit+1) & MASK)) - 1));
    result = (bits[where] != old);
    while (where-- > 0) {
      old = bits[where];
      bits[where] |= set.bits[where];
      result |= (bits[where] != old);
    }
    return result;
  }

  /**
   * Logically XORs this bit set with the specified set of bits.
   * Returns <code>true</code> if <code>this</code> was modified in
   * response to the operation. 
   * @param set the bit set to be XORed with
   */
  public boolean xor(BitString set) {
    int setLength = set.bits.length;
    boolean changed = false;
    for (int i = setLength; i-- > 0 ;) {
      int old = bits[i];
      bits[i] ^= set.bits[i];
      changed |= (old != bits[i]);
    }
    return changed;
  }
  
  /**
   * Check if the intersection of the two sets is empty
   * @param set the set to check intersection with
   */
  public boolean intersectionEmpty(BitString other) {
    int n = bits.length;
    for (int i = n ; i-- > 0 ; ) {
      if ((bits[i] & other.bits[i]) != 0) return false;
    }
    return true;
  }

  /**
   * Copies the values of the bits in the specified set into this set.
   * @param set the bit set to copy the bits from
   */
  public void copyBits(BitString set) {
    int setLength = set.bits.length;
    for (int i = setLength; i-- > 0 ;) {
      bits[i] = set.bits[i];
    }
  }
  
  /**
   * Returns a hash code value for this bit string whose value depends
   * only on which bits have been set within this <code>BitString</code>.
   */
  public int hashCode() {
    int h = 1234 * bits.length;
    for (int i = bits.length; --i >= 0; ) {
      h ^= bits[i] * (i + 1);
    }
    return h;
  }
  
  /**
   * Returns the "logical size" of this <code>BitString</code>: the
   * index of the highest set bit in the <code>BitString</code> plus
   * one.  Returns zero if the <code>BitString</code> contains no
   * set bits.
   */
  public int length() { return lastSet()+1; }

  /**
   * Returns the number of bits of space actually in use by this
   * <code>BitString</code> to represent bit values.
   * The maximum element in the set is the size - 1st element.
   * The minimum element in the set is the zero'th element.
   */
  public int size() {
    return bits.length << BITS_PER_UNIT;
  }

  /**
   * Compares this object against the specified object.
   * @param obj the object to compare with
   * @return true if the contents of the bitsets are the same; false otherwise.
   */
  public boolean equals(Object obj) {
    BitString set;
    if (obj==null) return false;
    if (this==obj) return true; //should help alias analysis
    try { set = (BitString)obj; }
    catch (ClassCastException e) { return false; }
    if (length() != set.length()) return false;
    int n = bits.length - 1;
    while (n>=0 && bits[n]==0) n--;
    // now n has the first non-zero entry
    for (int i = n ; i >= 0 ; i--) {
      if (bits[i] != set.bits[i]) {
	return false;
      }
    }
    return true;
  }

  public boolean isZero() {
    int setLength = bits.length;
    for (int i = setLength; i-- > 0 ;) {
      if (bits[i] != 0) return false;
    }
    return true;
  }

  public int numberOfOnes() {
    int setLength = bits.length;
    int number = 0;
    for (int i = setLength; i-- > 0 ;) {
      number += howManyOneBits(bits[i]);
    }
    return number;
  }
  
  public int numberOfOnes(int where) {
    int setLength = subscript(where);
    int number = 0;
    for (int i = setLength; i-- > 0 ;) {
      number += howManyOneBits(bits[i]);
    }
    number += howManyOneBits(bits[setLength] & ((1 << ((where+1) & MASK))-1));
    return number;
  }

  private static int howManyOneBits(int x) {
    return Util.popcount(x);
  }
  
  /**
   * Clones the BitString.
   */
  public BitString clone() {
    BitString result = null;
    try {
      result = (BitString) super.clone();
    } catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
    result.bits = new int[bits.length];
    System.arraycopy(bits, 0, result.bits, 0, result.bits.length);
    return result;
  }

  /**
   * Converts the BitString to a String.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    boolean needSeparator = false;
    buffer.append('{');
    int limit = size();
    for (int i = 0 ; i < limit ; i++) {
      if (get(i)) {
	if (needSeparator) {
	  buffer.append(", ");
	} else {
	  needSeparator = true;
	}
	buffer.append(i);
      }
    }
    buffer.append('}');
    return buffer.toString();
  }

  /** Self-test function. */
  public static void main(String argv[]) {
    // NOT COMPLETE: just checking firstSet() and lastSet() for now.
    BitString bs = new BitString(100);
    assert bs.length()==0 && bs.firstSet()==-1 && bs.lastSet()==-1;
    assert bs.firstSet(100)==-1 && bs.firstSet(-100)==-1;
    assert bs.lastSet(100)==-1 && bs.lastSet(-100)==-1;
    assert bs.size()>=bs.length();
    bs.set(52); bs.set(53); bs.set(76); bs.set(77);
    // test get()
    assert bs.get(52) && bs.get(53) && bs.get(76) && bs.get(77);
    assert !bs.get(51) &&!bs.get(54) &&!bs.get(75) &&!bs.get(78);
    // test length()
    assert bs.length()==78 && bs.size()>=bs.length();
    // test firstSet()
    assert bs.firstSet()==bs.firstSet(-100);
    assert bs.firstSet(-1)==52 && bs.firstSet(52)==53;
    assert bs.firstSet(53)==76 && bs.firstSet(76)==77;
    assert bs.firstSet(77)==-1 && bs.firstSet(1000)==-1;
    // test lastSet()
    assert bs.lastSet()==bs.lastSet(99);
    assert bs.lastSet(99)==77 && bs.lastSet(77)==76;
    assert bs.lastSet(76)==53 && bs.lastSet(53)==52;
    assert bs.lastSet(52)==-1 && bs.lastSet(-100)==-1;
    // test toString()
    assert bs.toString().equals("{52, 53, 76, 77}");
    // communicate success.
    System.out.println("TESTS PASSED");
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
