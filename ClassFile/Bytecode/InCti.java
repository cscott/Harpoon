package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InCti</code> is used for control-transfer instructions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InCti.java,v 1.1 1998-08-03 07:11:45 cananian Exp $
 * @see Instr
 */
public class InCti extends Instr {
  public Instr prev, next[];
}
