// Instr.java, created Sun Sep 13 22:49:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGEdge; 
import harpoon.IR.Properties.CFGraphable;
import harpoon.Util.ArrayFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
/**
 * <code>Bytecode.Instr</code> is the base type for the specific
 * bytecode instruction classes.  It provides standard methods
 * for accessing the opcode of a specific instruction and for
 * determining which instructions may preceed or follow it.
 * <p>As with all <code>HCodeElement</code>s, <code>Instr</code>s are
 * traceable to an original source file and line number, and have
 * a unique numeric identifier.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Instr.java,v 1.8 2003-05-09 20:38:43 cananian Exp $
 * @see InGen
 * @see InCti
 * @see InMerge
 * @see InSwitch
 * @see Code
 */
public abstract class Instr 
  implements HCodeElement, harpoon.IR.Properties.CFGraphable<Instr,InstrEdge>,
	     Comparable<Instr> {
  final String sourcefile;
  final int linenumber;
  final int id;
  /** Constructor. */
  protected Instr(String sourcefile, int linenumber) {
    this.sourcefile = sourcefile;
    this.linenumber = linenumber;
    /* it would be nice if this were unique for a particular Instr in a
     * method, instead of depending on the translation order of different
     * methods, but we'll settle for the total ordering Comparable implies.
     */
    synchronized(lock) {
      this.id = next_id++;
    }
  }
  static int next_id = 0;
  static final Object lock = new Object();

  /** Returns the original source file name that this bytecode instruction 
   *  is derived from. */
  public String getSourceFile() { return sourcefile; }
  /** Returns the line in the original source file that this bytecode 
   *  instruction can be traced to. */
  public int getLineNumber() { return linenumber; }
  /** Returns a unique numeric identifier for this element. */
  public int getID() { return id; }
  /** Returns the java bytecode of this instruction. */
  public abstract byte getOpcode();
  /** Natural ordering on <code>Instr</code>s. */
  public int compareTo(Instr o) {
    /* ordering is by id. */
    return o.id - this.id;
  }
  public boolean equals(Object o) { // exploit global uniqueness of id.
    if (this==o) return true;
    if (o==null) return false;
    try { return ((Instr) o).id == this.id; }
    catch (ClassCastException e) { return false; }
  }
  public int hashCode() { return id; } // exploit global uniqueness of id.

  /** Array Factory: makes <code>Instr[]</code>s. */
  public static final ArrayFactory<Instr> arrayFactory =
    new ArrayFactory<Instr>() {
      public Instr[] newArray(int len) { return new Instr[len]; }
    };

  /** Return a list of all the <code>Instr</code>s that can precede
   *  this one. */
  public Instr[] prev() {
    return prev.toArray(new Instr[prev.size()]);
  }
  /** Return a list of all the possible <code>Instr</code>s that may
   *  succeed this one. */
  public Instr[] next() {
    return next.toArray(new Instr[next.size()]);
  }
  /** Return the specified successor of this <code>Instr</code>. */
  public Instr next(int i) { return next.get(i); }
  /** Return the specified predecessor of this <code>Instr</code>. */
  public Instr prev(int i) { return prev.get(i); }

  /** Add a predecessor to this <code>Instr</code>. */
  void addPrev(Instr prev) { this.prev.add(prev); }
  /** Add a successor to this <code>Instr</code>. */
  void addNext(Instr next) { this.next.add(next); }
  /** Remove a predecessor from this <code>Instr</code>. */
  void removePrev(Instr prev) { this.prev.remove(prev); }
  /** Remove a successor from this <code>Instr</code>. */
  void removeNext(Instr next) { this.next.remove(next); }

  /** Internal predecessor list. */
  final List<Instr> prev = new ArrayList<Instr>(2);
  /** Internal successor list. */
  final List<Instr> next = new ArrayList<Instr>(2);

  // CFGraphable interface:
  public InstrEdge newEdge(Instr from, Instr to) {
      return new InstrEdge(from, to);
  }
  public InstrEdge[] succ() {
    InstrEdge[] r = new InstrEdge[next.size()];
    for (int i=0; i<r.length; i++)
      r[i] = newEdge(this, next.get(i));
    return r;
  }
  public InstrEdge[] pred() {
    InstrEdge[] r = new InstrEdge[prev.size()];
    for (int i=0; i<r.length; i++)
      r[i] = newEdge(prev.get(i), this);
    return r;
  }
  public InstrEdge[] edges() {
    InstrEdge[] n = succ();
    InstrEdge[] p = pred();
    InstrEdge[] r = new InstrEdge[n.length + p.length];
    System.arraycopy(n, 0, r, 0, n.length);
    System.arraycopy(p, 0, r, n.length, p.length);
    return r;
  }
  public Collection<InstrEdge> edgeC() { return Arrays.asList(edges()); }
  public Collection<InstrEdge> predC() { return Arrays.asList(pred()); }
  public Collection<InstrEdge> succC() { return Arrays.asList(succ()); }
  public boolean isSucc(Instr i) { return next.contains(i); }
  public boolean isPred(Instr i) { return prev.contains(i); }
}
class InstrEdge extends CFGEdge<Instr,InstrEdge> {
    final Instr from, to;
    InstrEdge(Instr from, Instr to) { this.from = from; this.to = to; }
    public Instr from() { return from; }
    public Instr to() { return to; }
    public boolean equals(Object o) {
	CFGEdge hce;
	if (this==o) return true;
	if (o==null) return false;
	try { hce=(CFGEdge)o; } catch (ClassCastException e) {return false; }
	return hce.from() == from && hce.to() == to;
    }
    public int hashCode() { return from.hashCode() ^ to.hashCode(); }
}
