// Code.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Util.ArrayEnumerator;

import java.util.*;

/**
 * <code>Generic.Code</code> is an abstract superclass of codeviews
 * which use <code>Instr</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.6 1999-05-25 16:45:58 andyb Exp $
 */
public abstract class Code extends HCode {
    /** The method that this code view represents. */
    protected HMethod parent;
    /** The root Instr of the Instrs composing this code view. */
    protected Instr instrs;
    /** Instruction factory. */
    protected final InstrFactory inf;
    /** The Frame associated with this codeview. */
    protected final Frame frame;

    /** Creates a new <code>InstrFactory</code> for this codeview.
     *
     *  @param  parent  The method which this codeview corresponds to.
     *  @return         Returns a new instruction factory for the scope
     *                  of the parent method and this codeview.
     */
    protected InstrFactory newINF(final HMethod parent) {
        final String scope = parent.getDeclaringClass().getName() + "." +
            parent.getName() + parent.getDescriptor() + "/" + getName();
        return new InstrFactory() {
            private final TempFactory tf = Temp.tempFactory(scope);
            private int id = 0;
            public TempFactory tempFactory() { return tf; }
            public HCode getParent() { return Code.this; }
            public Frame getFrame() { return frame; }
            public synchronized int getUniqueID() { return id++; }
        };
    }

    protected Code(final HMethod parent, final Instr instrs, 
                   final Frame frame) {
        this.parent = parent; this.instrs = instrs; this.frame = frame;
        this.inf = newINF(parent);
    }
    
    public abstract HCode clone(HMethod newMethod) 
        throws CloneNotSupportedException;

    public abstract String getName();

    public HMethod getMethod() { return parent; }

    public HCodeElement getRootElement() { return instrs; }

    public HCodeElement[] getLeafElements() { return null; }
   
    /** Returns an array of the instructions in this codeview. 
     *
     *  @return         An iterator over the <code>Instr</code>s
     *                  making up this code view.  The root Instr is
     *                  the first element in the iteration. */
    public Iterator getElementsI() { 
	return new Iterator() {
	    Set visited = new HashSet();
	    Stack s = new Stack();
	    {
		s.push(getRootElement());
		visited.add(s.peek());
	    }
	    public void remove() { throw new UnsupportedOperationException(); }
	    public boolean hasNext() { return !s.isEmpty(); }
	    public Object next() {
		if (s.empty()) throw new NoSuchElementException();
		Instr instr = (Instr) s.pop();
		HCodeEdge[] next = instr.succ();
		for (int i = next.length - 1; i >= 0; i--)
		    if (!visited.contains(next[i].to())) {
			s.push(next[i].to());
			visited.add(next[i].to());
		    }
		return instr;
	    }
	};
    }
  
    /** Returns an array factory to create the instruction elements
     *  of this codeview.
     *
     *  @return         An ArrayFactory which produces Instrs.
     */
    public ArrayFactory elementArrayFactory() { 
	return Instr.arrayFactory; 
    }

    /** Allows access to the InstrFactory used by this codeview.
     *
     *  @return         The InstrFactory used by this codeview.
     */
    public InstrFactory getInstrFactory() {
        return inf;
    }

    public Frame getFrame() {
        return frame;
    }
    
    /** Displays the assembly instructions of this codeview. Attempts
     *  to do so in a well-formatted, easy to read way. <BR>
     *  XXX - currently uses generic, not so easy to read, printer.
     *
     *  @param  pw      A PrintWriter to send the formatted output to.
     */
    public void print(java.io.PrintWriter pw) {
        pw.println();
	Instr[] instrarr = (Instr[]) getElements();
        for (int i = 0; i < instrarr.length; i++) {
            if (instrarr[i] instanceof InstrLABEL) {
                pw.println(instrarr[i]);
            } else {
                pw.println("\t"+instrarr[i]);
            }
        }
    }
}
