// Code.java, created Tue Feb 16 22:25:11 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
import harpoon.Temp.Label;
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
import java.io.StreamTokenizer;


/**
 * <code>Generic.Code</code> is an abstract superclass of codeviews
 * which use <code>Instr</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.33 1999-12-20 02:41:43 pnkfelix Exp $
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
    
    /**
     * Clone this <code>HCode</code>, possibly moving it to a different method.
     * Throws <code>CloneNotSupportedException</code> if not overridden.
     * @exception CloneNotSupportedException if it is not possible to clone
     *            this <code>HCode</code>.
     */
    public abstract HCode clone(HMethod newMethod) 
        throws CloneNotSupportedException;

    public abstract String getName();

    public HMethod getMethod() { return parent; }

    public HCodeElement getRootElement() { return instrs; }

    public HCodeElement[] getLeafElements() { return null; }
   
    /** Returns an <code>Iterator</code> over the instructions in this
	codeview.  
     *
     *  @return         An iterator over the <code>Instr</code>s
     *                  making up this code view.  The root Instr is
     *                  the first element in the iteration. */
    public Iterator getElementsI() { 
	return new UnmodifiableIterator() {
	    Instr instr = (Instr) getRootElement();
	    public boolean hasNext() { return (instr != null); }
	    public Object next() {
		if (instr == null) throw new NoSuchElementException();
		Instr r = instr;
		instr = r.getNext();
		return r;
	    }
	};
    }
    
    /** Returns an <code>Iterator</code> over the instructions in this
	codeview.  
     *
     *  @return         An iterator over the <code>Instr</code>s
     *                  making up this code view.  The root Instr is
     *                  the first element in the iteration. */
    public Iterator iterator() {
	return getElementsI();
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

	Iterator iter = getElementsI();
        while(iter.hasNext()) {
	    String str = "";
	    Instr instr = (Instr) iter.next();
            if (instr instanceof InstrLABEL ||
		instr instanceof InstrDIRECTIVE) {
                str = instr.toString();
            } else {
		try {
		    BufferedReader reader = 
			new BufferedReader(new StringReader(toAssem(instr)));
		    String s = reader.readLine();
		    while (s != null) {
			str += "\t"+ s;
			s = reader.readLine();
			if (s!=null) str+="\n";
		    }
		} catch (IOException e ) {
		    Util.assert(false, "IOException " + e.toString() + 
				" should not be thrown during assembly"+
				" code processing.");
		}
            }

	    pw.println(str);

	    if (str.indexOf("RETURN") != -1) {
		// System.out.println("Contained RETURN; hasNext():" + iter.hasNext());
	    } 

	    // System.out.println("InstrStr:"+str+" Next:"+instr.getNext()); 
        }
    }

    /** Produces an assembly code string for <code>i</code>, with
	register mappings.  Note that if <code>instr</code> is missing
	any register mappings then this method should not be called,
	since it is designed for final code output, not intermediary
	debugging output.
	
     */
    public String toAssem(Instr instr) {
	// return toAssem(instr, true);
	return toAssem(instr, false);
    }

    /** Produces an assembly code string for <code>i</code>, with
	register mappings where they exist.  Use this function for
	intermediary debugging output.
	
     */
    public String toAssemRegsNotNeeded(Instr instr) {
	return toAssem(instr, false);
    }

    /** Produces an assembly code string for <code>instr</code>.
	If <code>mustGetRegs</code> is true, Then will die on
	any missing register mappings in <code>instr</code>.  
	Else will just return the name for the <code>Temp</code>
	referenced by <code>instr</code> that has no associated
	register. 
     */
    protected String toAssem(Instr instr, boolean mustGetRegs) {
        StringBuffer s = new StringBuffer();
	String assem = instr.getAssem();
	
	int len = assem.length();
	for(int i=0; i<len; i++) {
	    char c = assem.charAt(i);
	    switch(c) {
	    case '`':
		Temp temp = null;
		Label label = null;
		boolean getReg = false;
		i++; c = assem.charAt(i);
		switch(c) {
		case 'd': {
		    i++; int n = Character.digit(assem.charAt(i), 10);
		    if (n < instr.def().length) {
			temp = instr.def()[n];
			getReg = true;
		    } else {
			Util.assert(false, "index mismatch in "+assem + 
				    " " + Arrays.asList(instr.def()));
			s.append("d?");
		    }
		    break;
		}
		case 's': {
		    i++; int n = Character.digit(assem.charAt(i), 10);
		    if (n < instr.use().length) {
			temp = instr.use()[n];
			getReg = true;
		    } else {
			Util.assert(false, "index mismatch in "+assem + 
				    " " + Arrays.asList(instr.use()));
			s.append("s?");
		    }
		    break;
		}
		case 'L': {
		    i++; int n = Character.digit(assem.charAt(i), 10);
		    if (n < instr.getTargets().size()) {
			label = (Label) instr.getTargets().get(n);
			s.append(label);
		    } else {
			Util.assert(false, "index mismatch in "+assem + 
				    " " + Arrays.asList(instr.use()));
			s.append("L?");
		    }
		    break;
		}
		case '`':
		    s.append("`");
		    break;
		default:
		    Util.assert(false, "error parsing "+assem);
		}
		
		if (getReg) {
		    char lastChar = ' ';
		    StringBuffer var = new StringBuffer();
		    boolean more = true;
		    while(more && i<(assem.length()-1)) {
			i++; c = assem.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
			    lastChar = c;
			    more = false;
			} else {
			    var.append(c);
			}
		    }
		    Util.assert( ( ! mustGetRegs ) ||
				 frame.getRegFileInfo().isRegister(temp) ||
				 registerAssigned(instr, temp),
				 "final assembly output for "+
				 "Instr: "+instr+" must have "+
				 "reg assigned to Temp: "+temp);
		    s.append(getRegisterName(instr, temp,
					     var.toString()));
		    s.append(lastChar);
		}

		break;
		
	    default: 
		s.append(c);
	    }
	}

        return s.toString();
    }


    /** Returns an assembly code identifier for the register that
	<code>val</code> will be stored into.

	<P> FSK: The same design flaws that plague Code#assignRegister
	will probably also plague this method.  Sigh.
     */
    protected abstract String getRegisterName(Instr i, Temp val, String suffix);

    /** Returns all of the Register <code>Temp</code>s that
	<code>val</code> maps to in <code>i</code>.
	<BR> <B>requires:</B> <OL>
	      <LI> <code>val</code> must be a <code>Temp</code> that
	           is an element of <code>i.defC()</code> or
		   <code>i.useC()</code>
	      <LI> <code>registerAssigned(i, val)</code> must be true
	<BR> <B>effects:</B> Returns a <code>Collection</code> of the
	     Register <code>Temp</code>s that are assigned to
	     <code>val</code> in <code>i</code>.  Every member of the
	     <code>Collection</code> returned will be a valid Register
	     for this architecture. 
    */
    public Collection getRegisters(Instr i, Temp val) {
	Util.assert(false, "Make abstract and implement in backends");
	return null;
    }
    
    /** Assigns a register to a <code>Temp</code> in <code>i</code>.
	<BR> <B>modifies:</B> <code>i</code> (FSK: potentially at least)
	<BR> <B>effects:</B> creates a mapping 
	<BR> NOTE: This is only an experimental method; only FSK
	should be using it until he makes sure that it implies no
	design flaws. 

	<P> FSK: Flaw 1 -- if there are multiple references to
	<code>pseudoReg</code> in <code>i</code>, like a := a + 1,
	then this method is too general; it does not allow us to put
	a's def in a different register from its use.  Now, since
	we're using SSI form at a high level, I don't know if we'll
	ever encounter code like that (depends on how Tree->Instr form
	is performed), but 
	<BR> (1.) I don't like <b>relying</b> on SSI to catch
	          undocumented problems like this implicitly, 
	<BR> (2.) we could, in theory, try to use this backend with a  
	          non-SSI front end
	<BR> The other issue here is I don't know when allowing the
	flexibility of having different registers for a's def and use
	will buy us anything...
     */
    public abstract void assignRegister(Instr i, 
					Temp pseudoReg, 
					List regs);

    /** Checks if <code>pseudoReg</code> has been assigned to some
	registers in <code>i</code>.
	<BR> <B>requires:</B> 
	      <code>val</code> must be a <code>Temp</code> that
	      is an element of <code>i.defC()</code> or
	      <code>i.useC()</code>
	<BR> <B>effects:</B> 
	     If <code>pseudoReg</code> has been assigned
	     to some <code>List</code> of registers in <code>i</code>
	     and <code>removeAssignment(i, pseudoReg)</code> has not
	     been called since, returns <code>true</code>.  
	     Else returns <code>false</code>.
     */
    public abstract boolean registerAssigned(Instr i, Temp pseudoReg);

    public void removeAssignment(Instr i, Temp pseudoReg) {
	Util.assert(false, "override and implement Code.removeAssignment"+
		    " (which should be abstract but since its an "+
		    "experimental method I don't want have add it "+
		    "to all the other code yet)");
    }

}
