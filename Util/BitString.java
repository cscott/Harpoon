package harpoon.Util;

/**
 * BitString
 *
 * @author  John Whaley
 */

public final class BitString implements Cloneable, java.io.Serializable {
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
   * Creates an empty string.
   */
  public BitString() {
    this(1 << BITS_PER_UNIT);
  }
  
  /**
   * Creates an empty string with the specified size.
   * @param nbits the size of the string
   */
  public BitString(int nbits) {
    /* subscript(nbits + MASK) is the length of the array needed to hold nbits */
    bits = new int[subscript(nbits + MASK)];
  }
  
  /**
   * Sets all bits.
   */
  public void setAll() {
    int i = bits.length;
    while (i-- > 0) {
      bits[i] = MASK;
    }
  }

  /**
   * Sets all bits up to and including the given bit.
   * @param bit the bit to be set up to
   */
  public void setUpTo(int bit) {
    int where = subscript(bit);
    bits[where] |= ((1 << ((bit+1) & MASK)) - 1);
    while (where-- > 0) {
      bits[where] = MASK;
    }
  }

  /**
   * Sets a bit.
   * @param bit the bit to be set
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
   * @param bit the bit to be set up to
   */
  public void clearUpTo(int bit) {
    int where = subscript(bit);
    bits[where] &= ~((1 << ((bit+1) & MASK)) - 1);
    while (where-- > 0) {
      bits[where] = 0;
    }
  }

  /**
   * Clears a bit.
   * @param bit the bit to be cleared
   */
  public void clear(int bit) {
    bits[subscript(bit)] &= ~(1 << (bit & MASK));
  }

  /**
   * Gets a bit.
   * @param bit the bit to be gotten
   */
  public boolean get(int bit) {
    int n = subscript(bit);
    return ((bits[n] & (1L << (bit & MASK))) != 0);
  }
  
  /**
   * Logically ANDs this bit set with the specified set of bits.
   * @param set the bit set to be ANDed with
   */
  public void and(BitString set) {
    if (this == set) { // should help alias analysis
      return;
    }
    int n = bits.length;
    for (int i = n ; i-- > 0 ; ) {
      bits[i] &= set.bits[i];
    }
  }
  
  /**
   * Logically ORs this bit set with the specified set of bits.
   * @param set the bit set to be ORed with
   */
  public void or(BitString set) {
    if (this == set) { // should help alias analysis
      return;
    }
    int setLength = set.bits.length;
    for (int i = setLength; i-- > 0 ;) {
      bits[i] |= set.bits[i];
    }
  }
  
  /**
   * Logically ORs this bit set with the specified set of bits.
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
   * @param set the bit set to be XORed with
   */
  public void xor(BitString set) {
    int setLength = set.bits.length;
    for (int i = setLength; i-- > 0 ;) {
      bits[i] ^= set.bits[i];
    }
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
   * Gets the hashcode.
   */
  public int hashCode() {
    int h = 1234;
    for (int i = bits.length; --i >= 0; ) {
      h ^= bits[i] * (i + 1);
    }
    return h;
  }
  
  /**
   * Calculates and returns the set's size in bits.
   * The maximum element in the set is the size - 1st element.
   */
  public int size() {
    return bits.length << BITS_PER_UNIT;
  }

  /**
   * Compares this object against the specified object.
   * @param obj the object to compare with
   * @return true if the objects are the same; false otherwise.
   */
  public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof BitString)) {
      if (this == obj) { // should help alias analysis
	return true;
      }
      BitString set = (BitString) obj;
      int n = bits.length;
      if (n != set.bits.length) return false;
      for (int i = n ; i-- > 0 ;) {
	if (bits[i] != set.bits[i]) {
	  return false;
	}
      }
      return true;
    }
    return false;
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

  public static int howManyOneBits(int x) {
    int number = 0;
    while (x != 0) {
      if ((x & 1) != 0) {
	++number;
	--x;
      }
      x >>>= 1;
    }
    return number;
  }
  
  /**
   * Clones the BitString.
   */
  public Object clone() {
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

}

