// Util.java, created Mon Aug  3  2:42:35 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.lang.reflect.Array;
/** 
 * Miscellaneous static utility functions.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Util.java,v 1.12.2.9 1999-08-04 06:31:03 cananian Exp $
 */
public abstract class Util {
  // Util contains only static fields and methods.

  /** If <code>debug</code> is false, we make some efficiency optimizations
   *  which are only safe for correct code. */
  private static final boolean debug = true;

  /** Returns an ArrayFactory which makes an array of the same type as the
   *  <code>src</code> parameter (which should be an array of some
   *  object type).  This method uses run-time type information and
   *  thus <b>should not be used</b>.
   *  @deprecated requires java language reflection.
   */
  public static final ArrayFactory genericFactory(final Object[] src) {
    final Class type = src.getClass().getComponentType();
    return new ArrayFactory() {
      public Object[] newArray(int len) {
	return (Object[])Array.newInstance(type, len);
      }
    };
  }

  /**
   * Copy an array type to prevent modification.  Does not bother
   * to copy array types of length 0, because they're already immutable.
   * After code has been debugged, this method can skip the copy for
   * efficiency.
   */
  public static final Object[] safeCopy(ArrayFactory factory, Object[] src) {
    if (!debug) return src;
    if (src.length==0) return src;
    Object[] dst = factory.newArray(src.length);
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
    
  /**
   * Copy an array type to prevent modification.
   * @deprecated The <code>clone()</code> method on arrays works 
   * much better, and should be used instead of this method.
   */
  public static final Object[] copy(ArrayFactory factory, Object[] src) {
    Object[] dst = factory.newArray(src.length);
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
  /** Remove element 'n' from array 'src'. */
  public static final Object[] shrink(ArrayFactory factory, 
				      Object[] src, int n) {
    Util.assert(src.length>0);
    Util.assert(n<src.length);
    Object[] dst = factory.newArray(src.length-1);
    System.arraycopy(src,   0, dst, 0, n);
    System.arraycopy(src, n+1, dst, n, src.length-(n+1));
    return dst;
  }
  /** Insert element <code>o</code> before <code>src[n]</code>. <p>
   *  After return, <code>src[n]==o</code>.  */
  public static final Object[] grow(ArrayFactory factory,
				    Object[] src, Object o, int n) {
    Util.assert(n>=0);
    Object[] dst = factory.newArray(src.length+1);
    System.arraycopy(src, 0, dst, 0, n);
    System.arraycopy(src, n, dst, n+1, src.length-n);
    dst[n] = o;
    return dst;
  }

  /** Escape the contents of a String so they are safe to print. */
  public static final String escape(String str) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<str.length(); i++) {
      char c = str.charAt(i);
      if (!isSafelyPrintable(c)) {
	if (c < 256) {
	  String octval=Integer.toOctalString((int)c);
	  while(octval.length()<3) octval="0"+octval;
	  sb.append('\\'); sb.append(octval);
	} else {
	  String hexval=Integer.toHexString((int)c);
	  while(hexval.length()<4) hexval="0"+hexval;
	  sb.append('\\'); sb.append('u'); sb.append(hexval);
	}
      }
      else sb.append(c);
    }
    return sb.toString();
  }
  /** Determine if we should escape this character or print it as-is. */
  static boolean isSafelyPrintable(char c) {
    // always escape backslash
    if (c=='\\') return false;
    // define 'safe' characters.
    if (' ' <= c && c <= '~') return true;
    // all others are 'unsafe'
    return false;
  }

  /** Assertion facility.  Throws a <code>RuntimeException</code> if
   *  the boolean parameter is <code>false</code>. */
  public static final void assert(boolean val) {
    if (!val)
      throw new RuntimeException("Assertion Failure.") { };
  }
  /** Assertion facility, which explanatory string.
   *  Throws a <code>RuntimeException</code> including the specified
   *  message string if the boolean parameter is <code>false</code>. */
  public static final void assert(boolean val, String msg) {
    if (!val)
      throw new RuntimeException("Assertion Failure: "+msg) { };
  }
  /** Repeat a given string a certain number of times.
   *  @return a string consisting of <code>s</code> repeated <code>n</code>
   *          times. */
  /* OLD WAY
  public static final String repeatString(String s, int n) {
    if (n==0) return "";
    if (n==1) return s;
    if ((n & 1) == 1)  // n is odd
      return repeatString(s, n-1) + s;
    else {
      String half = repeatString(s, n/2);
      return half + half;
    }
  }
  */
  // another way of doing the same thing.
  public static final String repeatString(String s, int n) {
    StringBuffer sb = new StringBuffer();
    for (int bit=fls(n)-1; bit>=0; bit--) {
      sb = sb.append(sb.toString());
      if ( (n & (1<<bit)) != 0)
	sb = sb.append(s);
    }
    return sb.toString();
  }
  /** Highest bit set in a byte. */
  static final byte bytemsb[] = {
    0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5,
    5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 /* 256 */
  };
  static { assert(bytemsb.length==0x100); }
  /** Lowest bit set in a byte. */
  static final byte bytelsb[] = {
    0, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1, 5, 1, 2, 1, 3, 1, 2, 1,
    4, 1, 2, 1, 3, 1, 2, 1, 6, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1,
    5, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1, 7, 1, 2, 1, 3, 1, 2, 1,
    4, 1, 2, 1, 3, 1, 2, 1, 5, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1,
    6, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1, 5, 1, 2, 1, 3, 1, 2, 1,
    4, 1, 2, 1, 3, 1, 2, 1, 8, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1,
    5, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1, 6, 1, 2, 1, 3, 1, 2, 1,
    4, 1, 2, 1, 3, 1, 2, 1, 5, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1,
    7, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1, 5, 1, 2, 1, 3, 1, 2, 1,
    4, 1, 2, 1, 3, 1, 2, 1, 6, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1,
    5, 1, 2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 3, 1, 2, 1  };
  static { assert(bytelsb.length==0x100); }
  /** Number of zeros in a byte. */
  static final byte bytezeros[] = {
    8, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 4, 7, 6, 6, 5, 6, 5, 5, 4,
    6, 5, 5, 4, 5, 4, 4, 3, 7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3,
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 7, 6, 6, 5, 6, 5, 5, 4,
    6, 5, 5, 4, 5, 4, 4, 3, 6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2,
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 5, 4, 4, 3, 4, 3, 3, 2,
    4, 3, 3, 2, 3, 2, 2, 1, 7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3,
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 6, 5, 5, 4, 5, 4, 4, 3,
    5, 4, 4, 3, 4, 3, 3, 2, 5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1,
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 5, 4, 4, 3, 4, 3, 3, 2,
    4, 3, 3, 2, 3, 2, 2, 1, 5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1,
    4, 3, 3, 2, 3, 2, 2, 1, 3, 2, 2, 1, 2, 1, 1, 0
  };
  static { assert(bytezeros.length==0x100); }
  
  /** Find first set (least significant bit).
   *  @return the first bit set in the argument.  
   *          <code>ffs(0)==0</code> and <code>ffs(1)==1</code>. */
  public static final int ffs(int v) {
    if ( (v & 0x0000FFFF) != 0)
      if ( (v & 0x000000FF) != 0)
        return bytelsb[v&0xFF];
      else
        return 8 + bytelsb[(v>>8)&0xFF];
    else
      if ( (v & 0x00FFFFFF) != 0)
        return 16 + bytelsb[(v>>16)&0xFF];
      else
        return 24 + bytelsb[(v>>24)&0xFF];
  }
  /** Find first set (least significant bit).
   *  @return the first bit set in the argument.  
   *          <code>ffs(0)==0</code> and <code>ffs(1)==1</code>. */
  public static final int ffs(long v) {
    if ( (v & 0xFFFFFFFFL) != 0) 
      return ffs( (int) (v & 0xFFFFFFFFL) );
    else
      return 32 + ffs( (int) ( v >> 32 ) );
  }
  /** Find last set (most significant bit).
   *  @return the last bit set in the argument.
   *          <code>fls(0)==0</code> and <code>fls(1)==1</code>. */
  public static final int fls(int v) {
    if ( (v & 0xFFFF0000) != 0)
      if ( (v & 0xFF000000) != 0)
        return 24 + bytemsb[v>>24];
      else
  	  return 16 + bytemsb[v>>16];
    if ( (v & 0x0000FF00) != 0)
      return 8 + bytemsb[v>>8];
    else
      return bytemsb[v];
  }
  /** Find last set (most significant bit).
   *  @return the last bit set in the argument.
   *          <code>fls(0)==0</code> and <code>fls(1)==1</code>. */
  public static final int fls(long v) {
    if ((v & 0xFFFFFFFF00000000L) != 0)
      return 32 + fls( (int) (v>>32) );
    else
      return fls( (int) v );
  }
  /** Returns ceil(log2(n)) */
  public static final int log2c(int v) {
    return (v==0)?-1:fls(v-1);
  }
  /** Return the number of zeros in the binary representation of the
   *  value of the argument. */
  public static final int zerocount(int v) {
    return bytezeros[v&0xFF] + bytezeros[(v>>8)&0xFF] +
      bytezeros[(v>>16)&0xFF] + bytezeros[(v>>24)&0xFF];
  }
  /** Return the number of zeros in the binary representation of the
   *  value of the argument. */
  public static final int zerocount(long v) {
    return zerocount((int)v)+zerocount((int)(v>>32));
  }
  /** Return the number of ones in the binary representation of the
   *  value of the argument. */
  public static final int popcount(int v) {
    return 32-zerocount(v);
  }
  /** Return the number of ones in the binary representation of the
   *  value of the argument. */
  public static final int popcount(long v) {
    return 64-zerocount(v);
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
