package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.MethodInfo;
import harpoon.ClassFile.Raw.Attribute.AttributeCode;
import harpoon.ClassFile.Raw.Attribute.AttributeLineNumberTable;
import harpoon.ClassFile.Raw.Attribute.LineNumberTable;
import harpoon.ClassFile.Raw.Constant.Constant;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;

import java.util.Vector;
/**
 * <code>Bytecode.Code</code> is a code view that exposes the
 * raw java classfile bytecodes.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.11 1998-08-20 22:47:24 cananian Exp $
 * @see harpoon.ClassFile.HCode
 */
public class Code extends HCode {
  HMethod parent;
  MethodInfo methodinfo;

  /** Constructor. */
  public Code(HMethod parent, MethodInfo methodinfo) {
    this.parent = parent;
    this.methodinfo = methodinfo;
  }

  /**
   * Return the <code>HMethod</code> this codeview
   * belongs to.
   */
  public HMethod getMethod() { return parent; }

  /**
   * Return the name of this code view, <code>"bytecode"</code>.
   * @return the string <code>"bytecode"</code>.
   */
  public String getName() { return "bytecode"; }

  /**
   * Convert from a different code view, by way of intermediates.
   * <code>Bytecode</code> is the basic codeview; no conversion
   * functions are implemented.
   * @return <code>null</code>, always.
   */
  public static HCode convertFrom(HCode codeview) { return null; }

  /**
   * Return an ordered list of the <code>Bytecode.Instr</code>s
   * making up this code view.  The first instruction to be
   * executed is in element 0 of the array.
   */
  public HCodeElement[] getElements() {
    if (elements==null) {
      if (getCode()==null) return new HCodeElement[0]; // no elements.
      String sf = parent.getDeclaringClass().getSourceFile(); // source file.
      byte[] code = getCode().code; // bytecode array.
      // First locate merge nodes.
      int[] merge = new int[code.length]; // init to 0.
      for (int pc=0; pc<code.length; pc+=Op.instrSize(code, pc)) {
	// if its a branch, mark the possible targets.
	if (Op.isBranch(code[pc])) {
	  int[] targets = Op.branchTargets(code, pc);
	  for (int i=0; i<targets.length; i++)
	    merge[targets[i]]++; // mark a jump to this pc.
	}
	// unless its an unconditional branch, we can fall through to the
	// next instr, too.  Note that we shouldn't be able to fall off
	// the end of the method.
	if (!Op.isUnconditionalBranch(code[pc]))
	  merge[pc+Op.instrSize(code, pc)]++;
      }
      // now all pc's for which merge>1 are merge nodes.
      Instr[] sparse = new Instr[code.length]; // index by pc still. 
      // crank through and add instrs without making links.
      Vector v = new Vector();
      for (int pc=0; pc<code.length; pc+=Op.instrSize(code, pc)) {
	int line = getLine(pc);
	// make merge node if appropriate.
	InMerge m = null;
	if (merge[pc] > 1)
	  v.addElement(m = new InMerge(sf, getLine(pc), merge[pc] /*arity*/));
	// make Instr object for this pc.
	if (Op.isBranch(code[pc])) {
	  if (code[pc]==Op.TABLESWITCH || code[pc]==Op.LOOKUPSWITCH)
	    sparse[pc] = new InSwitch(sf, line, code, pc);
	  else
	    sparse[pc] = new InCti(sf, line, code, pc);
	} else
	  sparse[pc] = new InGen(sf, line, code, pc, this);
	v.addElement(sparse[pc]);
	// Daisy-chain merge node if appropriate.
	if (m != null) {
	  m.addNext(sparse[pc]);
	  sparse[pc].addPrev(m);
	  sparse[pc] = m;
	}
      }
      // okay.  The instructions are made (in pc order, no less...)
      // link 'em.
      for (int pc=0; pc<code.length; pc+=Op.instrSize(code, pc)) {
	Instr curr = sparse[pc];
	if (curr instanceof InMerge)
	  curr = ((InMerge)curr).next()[0];
	if (!Op.isUnconditionalBranch(code[pc])) {
	  // link to next pc.
	  Instr next = sparse[pc+Op.instrSize(code, pc)];
	  curr.addNext(next);
	  next.addPrev(curr);
	}
	if (Op.isBranch(code[pc])) {
	  // link to branch targets.
	  int[] targets = Op.branchTargets(code, pc);
	  for (int i=0; i<targets.length; i++) {
	    Instr next = sparse[targets[i]];
	    curr.addNext(next);
	    next.addPrev(curr);
	  }
	}
      }
      // Make tryBlocks table.
      harpoon.ClassFile.Raw.Attribute.ExceptionTable et[] =
	getCode().exception_table;
      tryBlocks = new ExceptionEntry[et.length];
      for (int i=0; i<tryBlocks.length; i++) { // for each table entry...
	// Add all the PC's in the try block to a list.
	UniqueVector uv = new UniqueVector(et[i].end_pc-et[i].start_pc);
	for (int pc=et[i].start_pc; pc < et[i].end_pc; pc++)
	  uv.addElement(sparse[pc]);
	// Make an HClass for the exception caught...
	HClass ex = null;
	if (et[i].catch_type != 0)
	  ex = HClass.forDescriptor("L"+et[i].catch_type().name()+";");
	// and make the official exception entry.
	tryBlocks[i] = new ExceptionEntry(uv, ex, sparse[et[i].handler_pc]);
      }
      // Okay.  Just convert our vector to an array and we're ready to rumble.
      elements = new Instr[v.size()];
      v.copyInto(elements);
    }
    return (HCodeElement[]) Util.copy(elements);
  }
  /** Cached value of <code>getElements</code>. */
  private HCodeElement[] elements = null;
  /** Cached value of <code>getTryBlocks</code> blocks. */
  private ExceptionEntry[] tryBlocks = null;

  // special non-HCode-mandated access functions.
  /** Get the number of local variables used in this method, including
   *  the parameters passed to the method on invocation.  */
  public int getMaxLocals() { return getCode().max_locals; }
  /** Get the maximum number of words on the operand stack at any point
   *  during execution of this method. */
  public int getMaxStack() { return getCode().max_stack; }
  /** Get an array with the try-catch blocks/handlers for this bytecode. */
  public ExceptionEntry[] getTryBlocks() { getElements(); return tryBlocks; }

  /** Represents exception handlers in this code view. */
  public static class ExceptionEntry {
    UniqueVector tryBlock;
    HClass caughtException;
    Instr handler;
    ExceptionEntry(UniqueVector tryBlock, HClass caughtException,
		   Instr handler) {
      this.tryBlock = tryBlock;
      this.caughtException = caughtException;
      this.handler = handler;
    }
    public boolean inTry(Instr i) { return tryBlock.contains(i); }
    public HClass  caughtException() { return caughtException; }
    public Instr   handler() { return handler; }
  }

  // Utility functions.
  /** Return the Code attribute of this method. */
  private AttributeCode getCode() {
    if (attrcode==null) { // check cache.
      for (int i=0; i<methodinfo.attributes.length; i++)
	if (methodinfo.attributes[i] instanceof AttributeCode) {
	  attrcode = (AttributeCode) methodinfo.attributes[i];
	  break;
	}
    }
    return attrcode; // null if no code attribute was found.
  }
  private AttributeCode attrcode = null;
  /** Look up a constant in the appropriate constant_pool. */
  public Constant getConstant(int index) { return getCode().constant(index); }


  /** Get the line number corresponding to a given pc in the code
   *  array.  Zero is returned if there is no line number information
   *  in the code attribute. */
  private int getLine(int pc) {
    int lineno = 0;
    AttributeCode code = getCode();
    for (int i=0; i<code.attributes.length; i++) {
      if (code.attributes[i] instanceof AttributeLineNumberTable) {
	LineNumberTable[] lnt = 
	  ((AttributeLineNumberTable) code.attributes[i]).line_number_table;
	for (int j=0; j<lnt.length; j++)
	  if (lnt[j].start_pc <= pc)
	    lineno = lnt[j].line_number;
	  else
	    break; // start_pc must be uniformly increasing within each table.
      }
    }
    // hopefully this has done it.
    return lineno;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
