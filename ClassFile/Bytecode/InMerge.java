package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;
/**
 * <code>InMerge</code> is used to represent a node where
 * multiple control flows merge.  These are typically branch targets.
 * @author  C. Scott Ananian
 * @version $Id: InMerge.java,v 1.4 1998-08-03 10:35:39 cananian Exp $
 * @see Instr
 */
public class InMerge extends Instr {
  int arity;

  public InMerge(String sourcefile, int linenumber, int arity) {
    super(sourcefile, linenumber);
    this.arity = arity;
  }

  // Provide run-time checks on arity.
  /** @see Instr#addPrev */
  void addPrev(Instr prev) {
    if (this.prev.size()>=arity) throw new Error("Exceeding arity of MERGE.");
    super.addPrev(prev);
  }
  /** @see Instr#addNext */
  void addNext(Instr next) {
    if (this.next.size()>=1) 
      throw new Error("MERGE should have only one successor.");
    super.addNext(next);
  }

  /** Return human-readable representation. */
  public String toString() { return "MERGE"; }
}
