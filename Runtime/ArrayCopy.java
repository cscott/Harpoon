// ArrayCopy.java, created Tue Jan 23 16:09:31 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;

/**
 * <code>ArrayCopy</code> contains a pure java implementation of
 * the <code>System.arraycopy()</code> method.  This is used
 * by the <code>ArrayCopyImplementer</code> <code>HCodeFactory</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayCopy.java,v 1.1.2.2 2001-01-24 22:54:47 cananian Exp $
 */
public final class ArrayCopy {
    private ArrayCopy() { }

    /* pure-java implementation of System.arraycopy() */
    public static void arraycopy(Object src, int src_position,
				 Object dst, int dst_position,
				 int length) {
	// tricky tricky: spec allows for overlapping copies.
	if (src==dst && src_position < dst_position) {
	    // hopefully intelligent pointer analysis will be able to
	    // statically evaluate the condition above when this is
	    // inlined.
	    arraycopy_backwards(src, src_position, dst, dst_position, length);
	    return; // wow, that was a close one!
	}
	if (src==null || dst==null)
	    throw new NullPointerException();
	if (src_position < 0 || dst_position < 0 || length < 0)
	    throw new ArrayIndexOutOfBoundsException();
	if (src instanceof boolean[] && dst instanceof boolean[]) {
	    boolean[] sa = (boolean[]) src, da = (boolean[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof byte[] && dst instanceof byte[]) {
	    byte[] sa = (byte[]) src, da = (byte[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof char[] && dst instanceof char[]) {
	    char[] sa = (char[]) src, da = (char[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof short[] && dst instanceof short[]) {
	    short[] sa = (short[]) src, da = (short[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof int[] && dst instanceof int[]) {
	    int[] sa = (int[]) src, da = (int[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof long[] && dst instanceof long[]) {
	    long[] sa = (long[]) src, da = (long[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof float[] && dst instanceof float[]) {
	    float[] sa = (float[]) src, da = (float[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof double[] && dst instanceof double[]) {
	    double[] sa = (double[]) src, da = (double[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof Object[] && dst instanceof Object[]) {
	    Object[] sa = (Object[]) src, da = (Object[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=0; i<length; i++)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	throw new ArrayStoreException();
    }
    // same as arraycopy, except the copy is done in the reverse direction.
    public static void arraycopy_backwards(Object src, int src_position,
				 Object dst, int dst_position,
				 int length) {
	if (src==null || dst==null)
	    throw new NullPointerException();
	if (src_position < 0 || dst_position < 0 || length < 0)
	    throw new ArrayIndexOutOfBoundsException();
	if (src instanceof boolean[] && dst instanceof boolean[]) {
	    boolean[] sa = (boolean[]) src, da = (boolean[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof byte[] && dst instanceof byte[]) {
	    byte[] sa = (byte[]) src, da = (byte[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof char[] && dst instanceof char[]) {
	    char[] sa = (char[]) src, da = (char[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof short[] && dst instanceof short[]) {
	    short[] sa = (short[]) src, da = (short[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof int[] && dst instanceof int[]) {
	    int[] sa = (int[]) src, da = (int[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof long[] && dst instanceof long[]) {
	    long[] sa = (long[]) src, da = (long[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof float[] && dst instanceof float[]) {
	    float[] sa = (float[]) src, da = (float[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof double[] && dst instanceof double[]) {
	    double[] sa = (double[]) src, da = (double[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	if (src instanceof Object[] && dst instanceof Object[]) {
	    Object[] sa = (Object[]) src, da = (Object[]) dst;
	    if (src_position+length > sa.length ||
		dst_position+length > da.length)
		throw new ArrayIndexOutOfBoundsException();
	    for (int i=length-1; i>=0; i--)
		da[dst_position+i] = sa[src_position+i];
	    return;
	}
	throw new ArrayStoreException();
    }
}
