package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;
/**
 * <code>InMerge</code> is used to represent a node where
 * multiple control flows merge.  These are typically branch targets.
 * @author  C. Scott Ananian
 * @version $Id: InMerge.java,v 1.2 1998-08-03 07:00:49 cananian Exp $
 * @see Instr
 */
public class InMerge extends Instr {
  public Instr prev[], next;

  public InMerge(String sourcefile, int linenumber,
		 Instr[] prev, Instr next) {
    super(sourcefile, linenumber);
    this.prev = (Instr[]) Util.copy(prev);
    this.next = next;
  }

  /** Return a list of all the <code>Instr</code>s that can precede
   *  this one. */
  public Instr[] prev() { return (Instr[]) Util.copy(prev); }
  /** Return a list of all the possible <code>Instr</code>s that may
   *  succeed this one. Always returns a one-element array. */
  public Instr[] next() { return new Instr[] { next }; }
}
