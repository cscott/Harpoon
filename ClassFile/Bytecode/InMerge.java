package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;
/**
 * <code>InMerge</code> is used to represent a node where
 * multiple control flows merge.  These are typically branch targets.
 * @author  C. Scott Ananian
 * @version $Id: InMerge.java,v 1.1 1998-08-03 06:54:53 cananian Exp $
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
}
