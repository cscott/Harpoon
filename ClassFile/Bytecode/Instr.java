package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

import java.util.Vector;
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
 * @version $Id: Instr.java,v 1.8 1998-08-05 00:52:25 cananian Exp $
 * @see InGen
 * @see InCti
 * @see InMerge
 * @see Code
 */
public abstract class Instr implements HCodeElement {
  String sourcefile;
  int linenumber;
  int id;
  /** Constructor. */
  protected Instr(String sourcefile, int linenumber) {
    this.sourcefile = sourcefile;
    this.linenumber = linenumber;
    synchronized(lock) {
      this.id = next_id++;
    }
  }
  static int next_id = 0;
  static final Object lock = new Object();

  /** Get the original source file name that this bytecode instruction 
   *  is derived from. */
  public String getSourceFile() { return sourcefile; }
  /** Get the line in the original source file that this bytecode 
   *  instruction can be traced to. */
  public int getLineNumber() { return linenumber; }
  /** Returns a unique numeric identifier for this element. */
  public int getID() { return id; }
  /** Returns the java bytecode of this instruction. */
  public abstract byte getOpcode();

  /** Return a list of all the <code>Instr</code>s that can precede
   *  this one. */
  public Instr[] prev() {
    Instr[] p = new Instr[prev.size()]; prev.copyInto(p); return p;
  }
  /** Return a list of all the possible <code>Instr</code>s that may
   *  succeed this one. */
  public Instr[] next() {
    Instr[] n = new Instr[next.size()]; next.copyInto(n); return n;
  }

  /** Add a predecessor to this <code>Instr</code>. */
  void addPrev(Instr prev) { this.prev.addElement(prev); }
  /** Add a successor to this <code>Instr</code>. */
  void addNext(Instr next) { this.next.addElement(next); }
  /** Remove a predecessor from this <code>Instr</code>. */
  void removePrev(Instr prev) { this.prev.removeElement(prev); }
  /** Remove a successor from this <code>Instr</code>. */
  void removeNext(Instr next) { this.next.removeElement(next); }

  /** Internal predecessor list. */
  Vector prev = new Vector(2);
  /** Internal successor list. */
  Vector next = new Vector(2);
}
