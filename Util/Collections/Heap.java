// Heap.java, created Sat Feb 12 08:42:27 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
/**
 * <code>Heap</code>s support create, insert, minimum, extract-min,
 * union, decrease-key, and delete operations.  There are three primary
 * implementations, each with different expected run-times:<br>
 * <TABLE BORDER=2 ALIGN=CENTER>
 * 
 * <TR BGCOLOR="cornsilk"><TH>Procedure</TH>
 *     <TH><code>BinaryHeap</code><BR>(worst-case)</TH>
 *     <TH><code>BinomialHeap</code><BR>(worst-case)</TH>
 *     <TH><code>FibonacciHeap</code><BR>(amortized)</TH></TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">M<font size=-1>AKE</font>-H<font size=-1>EAP</font>
 *  </TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 * </TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">I<font size=-1>NSERT</font></TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER><i>O</i>(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 * </TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">M<font size=-1>INIMUM</font></TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 *     <TD ALIGN=CENTER><i>O</i>(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 * </TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">E<font size=-1>XTRACT</font>-M<font size=-1>IN</font>
 *  </TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER><i>O</i>(lg <i>n</i>)</TD>
 * </TR>
 * 
 * <TR><TD BGCOLOR="wheat">U<font size=-1>NION</font></TD>
 *     <TD ALIGN=CENTER>Theta(<i>n</i>)</TD>
 *     <TD ALIGN=CENTER><i>O</i>(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 * </TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">D<font size=-1>ECREASE</font>-K<font size=-1>EY</font>
 *  </TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(1)</TD>
 * </TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">U<font size=-1>PDATE</font>-K<font size=-1>EY</font>
 *  </TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 * </TR>
 * 
 * <TR>
 *  <TD BGCOLOR="wheat">D<font size=-1>ELETE</font></TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER>Theta(lg <i>n</i>)</TD>
 *     <TD ALIGN=CENTER><i>O</i>(lg <i>n</i>)</TD>
 * </TR>
 * 
 * <CAPTION ALIGN="BOTTOM">
 * Running times for operations on three implementations of mergable
 * heaps.  The number of items in the heap(s) at the time of an operation
 * is denoted by <i>n</i>.<br>
 * <DIV ALIGN=RIGHT><SMALL><i>
 * (From "Introduction to Algorithms" by Cormen, Leiserson, and Rivest)
 * </i></small></div>
 * </CAPTION>
 * 
 * </TABLE>
 * <p>
 * All implementations of <code>Heap</code> should have a no-argument
 * constructor which implements the 
 * M<font size=-1>AKE</font>-H<font size=-1>EAP</font> operation in
 * the above-stated time bound.  In addition, certain implementations
 * may also have constructors which take a <code>Map</code> or
 * <code>Heap</code> and construct a heap in less time than it would
 * take to call <code>insert()</code> on each item on an initially-empty
 * <code>Heap</code>.
 *
 * Note that the 
 * U<font size=-1>PDATE</font>-K<font size=-1>EY</font> operation is
 * typically implemented as a delete followed by an insert, which
 * often has worse performance than a
 * D<font size=-1>ECREASE</font>-K<font size=-1>EY</font> operation.
 * However, some algorithms need to increase keys as well as decrease
 * them; there's nothing you can do about that.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Heap.java,v 1.2 2002-02-25 21:09:04 cananian Exp $
 * @see BinaryHeap
 * @see BinomialHeap
 * @see FibonacciHeap
 */
public interface Heap {
    /** Inserts a node with the specified key and value into the
     *  <code>Heap</code>.  Returns the generated <code>Map.Entry</code>
     *  which may be stored and eventually passed back to
     *  <code>decreaseKey()</code> or <code>delete</code> to remove
     *  this node. */
    public Map.Entry insert(Object key, Object value);
    /** Returns a mapping entry with minimal key.
     * @exception java.util.NoSuchElementException
     *            if the heap has no entries.
     */
    public Map.Entry minimum();
    /** Remove and return a map entry with minimal key.
     * @exception java.util.NoSuchElementException
     *            if the heap has no entries.
     */
    public Map.Entry extractMinimum();
    /** Merges all of the mappings from the specified <code>Heap</code> 
     *  into this <code>Heap</code>.  Note that duplicates <b>are</b>
     *  permitted.  After calling <code>union()</code>, the <code>Heap</code>
     *  passed in as an argument will be empty.<p>
     *  Note that usually efficient implementations of this method require
     *  that the <code>Heap</code> argument be from the same implementation
     *  as <code>this</code>. (That is, they must both be binomial heaps, or
     *  both fibonacci heaps, etc.)
     */
    public void union(Heap h);
    /** Replace the key in the specified map entry with the specified
     *  <b>smaller</b> key.  */
    public void decreaseKey(Map.Entry me, Object newkey);
    /** Replace the key in the specified map entry with the specified key,
     *  which may be either larger or smaller than its current key. */
    public void updateKey(Map.Entry me, Object newkey);
    /** Remove the specified map entry from the mapping. */
    public void delete(Map.Entry me);
    /** Returns <code>true</code> if this <code>Heap</code> has no more
     *  entries. */
    public boolean isEmpty();
    /** Removes all entries from this <code>Heap</code>. */
    public void clear();
    /** Returns the number of entries in this <code>Heap</code>. */
    public int size();
    /** Returns a collection view of all the <code>Map.Entry</code>s
     *  in this <code>Heap</code>. */
    public Collection entries();
    /** Returns the hash code for this heap.  The hash code for this
     *  heap will be one greater than the hash code for the
     *  <code>Collection</code> returned by <code>entries()</code>. */
    public int hashCode();
    /** Compares the specified object with this heap for equality.
     *  Returns <code>true</code> iff the given object is also a
     *  <code>Heap</code> and the <code>Collection</code>s returned
     *  by their <code>entries()</code> methods are equal using
     *  the <code>equals()</code> method of <code>Collection</code>.
     */
    public boolean equals(Object o);
    /** Returns a string representation of this <code>Heap</code>. */
    public String toString();

    /** Returns the comparator associated with this <code>Heap</code>,
     *  or <code>null</code> if it uses its elements' natural ordering. */
    public Comparator comparator();
}
