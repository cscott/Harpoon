// Util.java, created Mon Aug  3  2:42:35 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.HCodeElement;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.io.PrintWriter;

import java.lang.reflect.Array;


/** 
 * Miscellaneous static utility functions.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Util.java,v 1.20 2003-02-12 19:04:38 salcianu Exp $
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
  public static final <T> T[] safeCopy(ArrayFactory<T> factory, T[] src) {
    if (!debug) return src;
    if (src.length==0) return src;
    T[] dst = factory.newArray(src.length);
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
    
  /**
   * Copy an array type to prevent modification.
   * @deprecated The <code>clone()</code> method on arrays works 
   * much better, and should be used instead of this method.
   */
  public static final <T> T[] copy(ArrayFactory<T> factory, T[] src) {
    T[] dst = factory.newArray(src.length);
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
  /** Remove element 'n' from array 'src'. */
  public static final <T> T[] shrink(ArrayFactory<T> factory, 
				      T[] src, int n) {
    assert src.length>0;
    assert n<src.length;
    T[] dst = factory.newArray(src.length-1);
    System.arraycopy(src,   0, dst, 0, n);
    System.arraycopy(src, n+1, dst, n, src.length-(n+1));
    return dst;
  }
  /** Insert element <code>o</code> before <code>src[n]</code>. <p>
   *  After return, <code>src[n]==o</code>.  */
  public static final <T> T[] grow(ArrayFactory<T> factory,
				    T[] src, T o, int n) {
    assert n>=0;
    T[] dst = factory.newArray(src.length+1);
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

  static boolean isJasminKeyWord(String str) {
    if (str.equals("method")) 
      return true;
    if (str.equals("from")) 
      return true;
    if (str.equals("to")) 
      return true;
    if (str.equals("is")) 
      return true;
    if (str.equals("using")) 
      return true;
    if (str.equals("tableswitch")) 
      return true;
    if (str.equals("lookupswitch")) 
      return true;
    return false;
  }

  /** Escape the contents of a String so they are safe to print. */
  public static final String jasminEscape(String str) {
    boolean flag;
    StringBuffer sb = new StringBuffer();
    flag=isJasminKeyWord(str);
    for (int i=0; i<str.length(); i++) {
      char c = str.charAt(i);
      if (!jasminIsSafelyPrintable(c)||flag) {
	flag=false;
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
  static boolean jasminIsSafelyPrintable(char c) {
    // always escape backslash
    if (c=='\\') return false;
    if (c=='\"') return false;
    // define 'safe' characters.
    if (' ' <= c && c <= '~') return true;
    // all others are 'unsafe'
    return false;
  }
  
  /** Repeat a given string a certain number of times.
   *  @return a string consisting of <code>s</code> repeated <code>n</code>
   *          times. */
  /* OLD WAY
  public static final String repeatString(String s, int n) {
    assert n>=0;
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
    assert n>=0;
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
  static { assert bytemsb.length==0x100; }
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
  static { assert bytelsb.length==0x100; }
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
  static { assert bytezeros.length==0x100; }
  
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
        return 24 + bytemsb[(v>>24) & 0xFF];
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
  // Use the 'binary Euclidean algorithm' to compute gcd.
  /** Returns the greatest common divisor of a pair of numbers. */
  public static final long gcd(long u, long v) { // long version.
    assert u>0 && v>0;
    int u2s = ffs(u)-1, v2s = ffs(v)-1;
    u>>=u2s; v>>=v2s; // cast out twos.
    // binary gcd algorithm; u and v must be odd at this point.
    while (u != v) {
      while (u<v) {
	v-=u;
	v>>=(ffs(v)-1);
      }
      long t=u; u=v; v=t;
    }
    // u,v have gcd
    return u << Math.min(u2s,v2s); // restore cast out twos.
  }
  /** Returns the greatest common divisor of a pair of numbers. */
  public static final int gcd(int u, int v) { // integer version.
    assert u>0 && v>0;
    int u2s = ffs(u)-1, v2s = ffs(v)-1;
    u>>=u2s; v>>=v2s; // cast out twos.
    // binary gcd algorithm; u and v must be odd at this point.
    while (u != v) {
      while (u<v) {
	v-=u;
	v>>=(ffs(v)-1);
      }
      int t=u; u=v; v=t;
    }
    // u,v have gcd
    return u << Math.min(u2s,v2s); // restore cast out twos.
  }

  /** Return a <code>String</code> representing the elements of
      <code>collection</code> in a human readable format.  
      <BR> <B>effects:</B> Iterates over <code>collection</code>,
           calling <code>toString()</code> on each element and
	   appending the result to a <code>String</code>.  The format
	   of the returned <code>String</code> is 
	   <NOBR>{ elem1, elem2, ... , elemN }</NOBR>
      @return A <code>String</code> representing
              <code>collection</code>. 
  */
  public static final String print(java.util.Collection collection) {
    StringBuffer sb = new StringBuffer("{ ");
    java.util.Iterator iter = collection.iterator();
    while(iter.hasNext()) {
      Object elem = iter.next();
      sb.append(elem.toString());
      if (iter.hasNext()) {
	sb.append(", ");
      }
    }
    sb.append(" }");
    return sb.toString();
  }

  /** Pretty printer for collections.

      @param c The <code>Collection</code> to be printed.
      @param c_name The name of the collection.
      @param indent String used for indentation (eg &quot;<code>\t</code>&quot;.
      @param pw The <code>PrintWriter</code> used to do the printing itself.

      The collection is printed in the following format: the name of
  the collection followed by the collection size (in parans) and one
  opening curly bracet on the first line, the elements of the
  collection, one by one, each on a separate line and indented with a
  tab; on the last line, one closing curly bracet. In addition, each
  line is prefixed with the string <code>indent</code>. */
  public static final void print_collection(Collection c, String c_name,
					    String indent, PrintWriter pw) {
    pw.print(indent + c_name + " (" + c.size() + "): {");
    if(c.isEmpty()) {
      pw.println("}");
      return;
    }
    pw.println();
    for(Iterator it = c.iterator(); it.hasNext(); ) {
      pw.print(indent);
      pw.print("\t");
      pw.println(it.next());
    }
    pw.println(indent + "}");
  }

  /** Simplified version of <code>print_collection</code>.
      Uses the default value
      <code>new java.io.PrintWriter(System.out, true)</code>
      for the <code>pw</code> parameter. */
  public static final void print_collection(Collection c, String c_name,
					    String indent) {
    print_collection(c, c_name, indent,
		     new java.io.PrintWriter(System.out, true));
  }


  /** Even more simplified version of <code>print_collection</code>.
      Uses the default value <code>&quot;&quot;</code> for <code>indent</code>
      and <code>new java.io.PrintWriter(System.out, true)</code>
      for the <code>pw</code> parameter. */
  public static final void print_collection(Collection c, String c_name) {
    print_collection(c, c_name, "");
  }

  /** Computes the difference of two sets: <code>a-b</code>. */
  public static final <A,B> Set<A> set_diff(final Set<A> a, final Set<B> b) {
    Set<A> diff = new HashSet<A>(a);
    diff.removeAll(b);
    return diff;
  }


  /** Returns a string that is identical to <code>str</code>, except
      that every <code>&quot;</code> character has been replaced with
      the sequence <code>\&quot;</code>.
      This is useful when we generate output for
      tools like VCG which expects the value of some attributes to be put
      between quotes (and of course any quote inside has to be given using
      an escape sequence). */
  public static final String adjust_quotes(String str) {
    StringBuffer buff = new StringBuffer();
    for(int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if(c == '\"')
	buff.append("\\\"");
      else
	buff.append(c);
    }
    return buff.toString();
  }

  /** Converts an objects using the reflexive closure of a map.
      @param obj object to be converted
      @param map conversion map
      @return if <code>obj</code> is in the key set of
      <code>map</code>, returns <code>map.get(obj)</code>. Otherwise,
      the method simply returns <code>obj</code>. */
  public static Object convert(Object obj, Map map) {
    Object image = map.get(obj);
    return (image == null) ? obj : image;
  }


  // the powers of 10!!
  private static double[] fact = 
  {1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0};
  

  /** Returns the string representation of the double d, with a certain
      number of decimals. Let me know if you know something better. */
  public static String doubleRep(double d, int decimals) {
    double dfloor = Math.floor(d); // (double) integer part of d
    double dfrac = d - dfloor;     // fractional part of d
    int ifloor = (int) dfloor;     // integer part of d
    // the last d decimals
    int fracint = (int) Math.ceil(dfrac * fact[decimals]); 
    
    StringBuffer fracstr = new StringBuffer(String.valueOf(fracint));
    int missing_decs = decimals - fracstr.length();
    for(int i = 0; i < missing_decs; i++)
      fracstr.append("0");
    
    return String.valueOf(ifloor) + "." + fracstr.toString();
  }
  
  /** Returns the string representation of the double d, with a certain
      number of decimals. Let me know if you know something better. */
  public static String doubleRep(double d, int digits, int decimals) {
    StringBuffer buffer = new StringBuffer(doubleRep(d,decimals));
    int desired_point = digits - decimals - 1;
    int current_point = buffer.toString().indexOf(".");
    int needed_spaces = desired_point - current_point;
    if(needed_spaces == 0) return buffer.toString();
    StringBuffer buffer2 = new StringBuffer();
    for(int i = 0; i < needed_spaces; i++)
      buffer2.append(" ");
    return buffer2.toString() + buffer.toString();
  }
  
  /** Returns a string representing the proportion a/total (in percents). */
  public static String percentage(double a, double total) {
    double perct = (100.0 * a) / total;
    return doubleRep(perct, 5, 2) + "%";
  }
  
  /** Returns the line of the instruction q in the format
      <code>source_file:line_number</code>. */
  public static String getLine(HCodeElement q) {
    return
      q.getSourceFile() + ":" + q.getLineNumber();
  }
  
  /** Returns the string representation of the code instruction q
      in the formay: <code>source_file:line_number instruction</code>. */
  public static String code2str(HCodeElement q) {
    if(q == null) return "(null)";
    return
      getLine(q)  + " " + q;
  }
  
  /** Returns the string representation of the proportion a/b.  The
      proportion is printed as a percentage, with 2 decimals.  Eg,
      1/2 is presented as 50.00%. */
  public static String proportion2str(long a, long b) {
    double frac = (((double) 100) * ((double) a)) / ((double) b);
    return doubleRep(frac, 5, 2) + "%";
  }


  /** Pretty printer for a proportion: &quot; <tt>a</tt> out of
      <tt>total</tt> = <tt>proportion</tt>&quot; (where
      <tt>total=a+b</tt>). If <code>memSizeFormat<code> is true,
      <tt>a</tt> and <tt>total</tt> are displayed as memory size
      (i.e., 2048 is 2K). <code>digits</code> and
      <code>decimals</code> indicates the formatting for the
      proportion. */
  public static String longProportion
    (long a, long b, int digits, int decimals, boolean memSizeFormat) {
    long total = a + b;
    double frac = (a * 100.0) / (total + 0.0);
    return 
      (memSizeFormat ? Util.memSizeFormat(a) : (new Long(a)).toString()) +
      "\tout of\t" + 
      (memSizeFormat ? Util.memSizeFormat(total) : 
       (new Long(total)).toString()) +
      "\t = " +
      doubleRep(frac, digits, decimals) + "%";
  }

  /** Convenient version of <code>longProportion</code> that uses
      <code>null</code> for the <code>memSizeFormat</code>. */
  public static String longProportion
    (long a, long b, int digits, int decimals) {
    return longProportion(a, b, digits, decimals, false);
  }
  

  /** Nicely formats the memory size <code>size</code>, using normal
      units for memory size.  For example, 2048 is printed as 2.00K.
      The biggest unit we support now is Tera.  */
  public static String memSizeFormat(long size) {
    // special case: size < 1K
    if(size < 1024)
      return (new Long(size)).toString();
    
    double dsize = (double) size;
    int index = 0;
    while((dsize > 1024) && (index < msf_units.length)) {
      dsize = dsize / 1024;
      index++;
    }
    return doubleRep(dsize, 2) + msf_units[index];
  }
  private static String msf_units[] = {"", "K", "M", "G", "T"};

}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
