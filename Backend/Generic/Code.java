// Code.java, created Tue Feb 16 22:25:11 1999 by andyb
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
import harpoon.Util.Util;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Util.ArrayEnumerator;
import harpoon.Util.UnmodifiableIterator;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;


/**
 * <code>Generic.Code</code> is an abstract superclass of codeviews
 * which use <code>Instr</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.12 1999-08-04 06:30:52 cananian Exp $
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
	return new UnmodifiableIterator() {
	    Set visited = new HashSet();
	    Stack s = new Stack();
	    {
		s.push(getRootElement());
		visited.add(s.peek());
	    }
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
	
	pw.print("Codeview \""+getName()+"\" for "+getMethod()+":");
	
        pw.println();
	Instr[] instrarr = (Instr[]) getElements();
        for (int i = 0; i < instrarr.length; i++) {
            if (instrarr[i] instanceof InstrLABEL) {
                pw.println(instrarr[i]);
            } else {
		try {
		    BufferedReader reader = 
			new BufferedReader(new StringReader(toAssem(instrarr[i])));
		    String s = reader.readLine();
		    while (s != null) {
			pw.println("\t"+s);
			s = reader.readLine();
		    }
		} catch (IOException e ) {
		    Util.assert(false, "IOException " + e.toString() + 
				" should not be thrown during assembly"+
				" code processing.");
		}
            }
        }
    }

    /** Produces an assembly code string for <code>i</code>.
	
     */
    public String toAssem(Instr instr) {
        StringBuffer s = new StringBuffer();
	String assem = instr.getAssem();
        int len = assem.length();
        for (int i = 0; i < len; i++) 
            if (assem.charAt(i) == '`')
                switch (assem.charAt(++i)) {
		case 'd': { 
		    int n = Character.digit(assem.charAt(++i), 10);
		    Util.assert(n < instr.dst.length, 
				"Code can't parse " + assem);
		    s.append(instr.dst[n]);
		}
		break;
		case 's': {
		    int n = Character.digit(assem.charAt(++i), 10);
		    Util.assert(n < instr.src.length, 
				"Code can't parse " + assem);
		    s.append(instr.src[n]);
		}
		break;
		case 'j': {
		    int n = Character.digit(assem.charAt(++i), 10);
		    Util.assert(n < instr.src.length, 
				"Code can't parse " + assem);
		    s.append(instr.src[n]);
		}
		break;
		case '`': 
		    s.append('`');
		    break;
                }
            else s.append(assem.charAt(i));

        return s.toString();
    }


    /** Returns an assembly code identifier for the register that
	<code>val</code> will be stored into.
     */
    protected abstract String getRegisterName(Instr i, Temp val, String suffix);

    
    /** Assigns a register to a <code>Temp</code> in <code>i</code>.
	<BR> <B>modifies:</B> <code>i</code> (FSK: potentially at least)
	<BR> <B>effects:</B> creates a mapping 
	<BR> NOTE: This is only an experimental method; only FSK
	should be using it until he makes sure that it implies no
	design flaws. 
     */
    public abstract void assignRegister(Instr i, Temp pseudoReg, List regs);

}
