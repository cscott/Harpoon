// Code.java, created Sun Sep 13 22:49:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.RawClass.MethodInfo;
import harpoon.IR.RawClass.AttributeCode;
import harpoon.IR.RawClass.AttributeLineNumberTable;
import harpoon.IR.RawClass.LineNumberTable;
import harpoon.IR.RawClass.Constant;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.Util.ArrayFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>Bytecode.Code</code> is a code view that exposes the
 * raw java classfile bytecodes.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.10 2002-02-25 21:04:17 cananian Exp $
 * @see harpoon.ClassFile.HCode
 */
public class Code extends HCode {
  /** The name of this code view. */
  public static final String codename = "bytecode";

  final Linker linker;
  final HMethod parent;
  final MethodInfo methodinfo;

  /** Constructor. */
  public Code(HMethod parent, MethodInfo methodinfo) {
    this.linker = parent.getDeclaringClass().getLinker();
    this.parent = parent;
    this.methodinfo = methodinfo;
  }
  /** Clone this code representation.  The clone has its own copy of the
   *  bytecode graph. */
  public HCodeAndMaps clone(HMethod newMethod) {
    final HCode cloned = new Code(newMethod, methodinfo);
    return new HCodeAndMaps(cloned, null, null, this, null, null);
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
  public String getName() { return codename; }

  /**
   * Return an ordered list of the <code>Bytecode.Instr</code>s
   * making up this code view.  The first instruction to be
   * executed is in element 0 of the array.
   */
  public List getElementsL() {
    if (elements==null) {
      if (getCode()==null) return Collections.EMPTY_LIST; // no elements.
      String sf = parent.getDeclaringClass().getSourceFile(); // source file.
      byte[] code = getCode().code; // bytecode array.
      // First locate merge nodes.
      int[] merge = new int[code.length+1]; // init to 0.
      merge[0]++; // the first instruction is reachable from outside.
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
	if ((!Op.isUnconditionalBranch(code[pc])) ||
	    Op.isJSR(code[pc])) // jsrs eventually return to next instr, too.
	  merge[pc+Op.instrSize(code, pc)]++;
      }
      // try handlers count as targets, too
      for (int i=0; i<getCode().exception_table.length; i++)
	merge[getCode().exception_table[i].handler_pc]++;

      // if merge[code.length]!=0, then execution may run off end of method.
      if (merge[code.length]>0)
	System.err.println("WARNING: execution may run off end of "+parent);

      // now all pc's for which merge>1 are merge nodes.
      Instr[] sparse = new Instr[code.length]; // index by pc still. 
      // crank through and add instrs without making links.
      List v = new ArrayList(code.length);
      for (int pc=0; pc<code.length; pc+=Op.instrSize(code, pc)) {
	int line = getLine(pc);
	// make merge node if appropriate.
	InMerge m = null;
	if (merge[pc] > 1)
	  v.add(m = new InMerge(sf, line, merge[pc] /*arity*/));
	// make Instr object for this pc.
	if (Op.isBranch(code[pc])) {
	  if (code[pc]==Op.TABLESWITCH || code[pc]==Op.LOOKUPSWITCH)
	    sparse[pc] = new InSwitch(sf, line, code, pc);
	  else if (code[pc]==Op.RET)
	    sparse[pc] = new InRet(sf, line, code, pc);
	  else
	    sparse[pc] = new InCti(sf, line, code, pc);
	} else
	  sparse[pc] = new InGen(sf, line, code, pc, this);
	v.add(sparse[pc]);
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
	  curr = ((InMerge)curr).next(0);
	if ((!Op.isUnconditionalBranch(code[pc])) ||
	    Op.isJSR(code[pc])) { // jsrs return to next instruction eventually
	  // JVM spec is not clear whether 'code can run off end of array'
	  // or not.
	  if ((pc+Op.instrSize(code, pc)) < code.length) {
	    // link to next pc.
	    Instr next = sparse[pc+Op.instrSize(code, pc)];
	    curr.addNext(next);
	    next.addPrev(curr);
	  }
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
      harpoon.IR.RawClass.ExceptionTable et[] =
	getCode().exception_table;
      tryBlocks = new ExceptionEntry[et.length];
      for (int i=0; i<tryBlocks.length; i++) { // for each table entry...
	// Add all the PC's in the try block to a list.
	Set uv = new HashSet(et[i].end_pc-et[i].start_pc);
	for (int pc=et[i].start_pc;
	     pc < et[i].end_pc;
	     pc+=Op.instrSize(code,pc)) {
	  uv.add(sparse[pc]);
	  if (sparse[pc] instanceof InMerge) // merges come in pairs.
	    uv.add(sparse[pc].next(0));
	}
	// Make an HClass for the exception caught...
	HClass ex = null;
	if (et[i].catch_type != 0)
	  ex = linker.forDescriptor("L"+et[i].catch_type().name()+";");
	else
	  ex = null; // to indicate 'catch any'.
	// and make the official exception entry.
	tryBlocks[i] = new ExceptionEntry(i, uv, ex, sparse[et[i].handler_pc]);
      }
      // Okay.  Just trim our list and we're ready to rumble.
      ((ArrayList)v).trimToSize();
      elements = Collections.unmodifiableList(v);
    }
    return elements;
  }
  /** Cached value of <code>getElements</code>. */
  private List elements = null;
  /** Cached value of <code>getTryBlocks</code> blocks. */
  private ExceptionEntry[] tryBlocks = null;

  /** @deprecated use getElementsL() */
  public HCodeElement[] getElements() {
    List l = getElementsL();
    return (HCodeElement[]) l.toArray(new Instr[l.size()]);
  }
  /** @deprecated use getElementsI() */
  public Enumeration getElementsE() {
    return Collections.enumeration(getElementsL());
  }
  public Iterator getElementsI() {
    return getElementsL().listIterator();
  }

  public List getLeafElementsL() {
    if (leaves == null) {
      leaves = new ArrayList();
      for (Iterator i = getElementsI(); i.hasNext(); ) {
	Instr in = (Instr) i.next();
	if (in.next.size()==0)
	  leaves.add(in);
      }
      ((ArrayList)leaves).trimToSize();
      leaves = Collections.unmodifiableList(leaves);
    }
    return leaves;
  }
  private List leaves = null;

  public HCodeElement[] getLeafElements() {
    List l = getLeafElementsL();
    return (HCodeElement[]) l.toArray(new Instr[l.size()]);
  }

  // implement elementArrayFactory which returns Instr[]s.
  public ArrayFactory elementArrayFactory() { return Instr.arrayFactory; }

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
  public static class ExceptionEntry implements Comparable {
    int order; // smaller numbers have precedence over higher numbers.
    Set tryBlock;
    HClass caughtException;
    Instr handler;
    ExceptionEntry(int order,
		   Set tryBlock, HClass caughtException, Instr handler) {
      this.order = order;
      this.tryBlock = tryBlock;
      this.caughtException = caughtException;
      this.handler = handler;
    }
    public boolean inTry(Instr i) { return tryBlock.contains(i); }
    public HClass  caughtException() { return caughtException; }
    public Instr   handler() { return handler; }

    public int compareTo(Object o) {
      int cmp = this.order - ((ExceptionEntry)o).order;
      if (cmp==0 && !this.equals(o)) // check consistency.
	throw new ClassCastException("Comparing uncomparable objects");
      return cmp;
    }
    public String toString() {
      return "Exception Entry #"+order+" for "+caughtException;
    }
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

  /** Return an HCodeFactory for Bytecode form. */
  public static HCodeFactory codeFactory() {
    return harpoon.ClassFile.Loader.systemCodeFactory;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
