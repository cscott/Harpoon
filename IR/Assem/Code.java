// Code.java, created Tue Jan 25 23:41:07 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Backend.Generic.Frame;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
/**
 * <code>IR.Assem.Code</code> is an abstract superclass of codeviews
 * which use <code>Instr</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.12 2000-07-18 22:35:17 pnkfelix Exp $
 */
public abstract class Code extends HCode {
    private static boolean DEBUG = true;

    /** The method that this code view represents. */
    protected HMethod parent;
    /** The root Instr of the Instrs composing this code view. */
    protected Instr instrs;
    /** Instruction factory. */
    protected final InstrFactory inf;
    /** The Frame associated with this codeview. */
    protected final Frame frame;


    private InstrFactory newINF(final HMethod parent) {
	return newINF(parent, getName());
    }

    /** Creates a new <code>InstrFactory</code> for this codeview.
     *
     *  @param  parent  The method which this codeview corresponds to.
     *  @param  codeName The String that would be returned by a call
     *                   to <code>Code.this.getName()</code>.
     *  @return         Returns a new instruction factory for the scope
     *                  of the parent method and this codeview.
     */
    private InstrFactory newINF(final HMethod parent, String codeName) {
        final String scope = parent.getDeclaringClass().getName() + "." +
            parent.getName() + parent.getDescriptor() + "/" + codeName;
        return new InstrFactory() {
            private final TempFactory tf = Temp.tempFactory(scope);
            private int id = 0;
            public TempFactory tempFactory() { return tf; }
            public HCode getParent() { return Code.this; }
            public Frame getFrame() { return frame; }
            public synchronized int getUniqueID() { return id++; }
        };
    }

    /** constructor. */
    protected Code(HMethod parent, Frame frame, String codeName) {
        this.parent = parent;
	this.instrs = null;
	this.inf = newINF(parent, codeName);
	this.frame = frame;
    }
   
    /** constructor. */
    protected Code(HMethod parent, Frame frame) {
        this.parent = parent;
	this.instrs = null;
	this.inf = newINF(parent);
	this.frame = frame;
    }
    /**
     * Clone this <code>HCode</code>, possibly moving it to a different method.
     * Throws <code>CloneNotSupportedException</code> if not overridden.
     * @exception CloneNotSupportedException if it is not possible to clone
     *            this <code>HCode</code>.
     */
    public HCode clone(HMethod newMethod) throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }
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
    
    /** Prints the assembly instructions of <code>this</code> to
	STDOUT. 
	@see Code#print(java.io.PrintWriter)
    */
    public void print() {
	print(new java.io.PrintWriter(System.out));
    }

    /** Displays the assembly instructions of this codeview. Attempts
     *  to do so in a well-formatted, easy to read way. <BR>
     *  XXX - currently uses generic, not so easy to read, printer.
     *
     *  @param  pw      A PrintWriter to send the formatted output to.
     */
    public void print(java.io.PrintWriter pw) {
	myPrint(pw, true);
    }

    public void printNoAssem(java.io.PrintWriter pw) {
	myPrint(pw, false);
    }

    private void myPrint(java.io.PrintWriter pw, boolean assem) {
	final boolean DEBUG = false;
	final HashSet outputLabels = new HashSet();
	final MultiMap labelsNeeded = new GenericMultiMap();

	for (Instr instr=instrs; instr != null; instr=instr.getNext()) {
	    StringBuffer str = new StringBuffer();
            if (instr instanceof InstrLABEL ||
		instr instanceof InstrDIRECTIVE) {
                str.append(instr.toString());

		if (DEBUG && (instr instanceof InstrLABEL)) {
		    InstrLABEL il = (InstrLABEL) instr;
		    Label l = il.getLabel();
		    outputLabels.add(l);
		}

            } else {
		try {
		    BufferedReader reader = 
			new BufferedReader
			(new StringReader
			 (assem?toAssem(instr):instr.toString()));
		    String s = reader.readLine();
		    while (s != null) {
			str.append("\t"+ s);
			s = reader.readLine();
			if (s!=null) str.append("\n");
		    }
		} catch (IOException e ) {
		    Util.assert(false, "IOException " + e.toString() + 
				" should not be thrown during assembly"+
				" code processing.");
		}
            }

	    pw.println(str.toString());

 	    if (str.toString().indexOf("RETURN") != -1) {
		// System.out.println("Contained RETURN; hasNext():" + iter.hasNext());
	    } 

	    if (DEBUG) {
		Iterator targets = instr.getTargets().iterator();
		while(targets.hasNext()) {
		    labelsNeeded.add(targets.next(), instr);
		}
	    }

	    // System.out.println("InstrStr:"+str+" Next:"+instr.getNext()); 
        }
	
	pw.flush();

	if (DEBUG) { // check that all needed labels have been output
 	    Iterator needed = labelsNeeded.keySet().iterator();
	    while(needed.hasNext()) {
		final Label l = (Label) needed.next();
		Util.assert(outputLabels.contains(l), 
			    new Object() {
		    public String toString() {
			Iterator instrs = getElementsI();
			boolean labelFound = false;
			while(instrs.hasNext()) {
			    Instr i = (Instr) instrs.next();
			    if (i instanceof InstrLABEL &&
				l.equals(((InstrLABEL)i).getLabel())) {
				labelFound = true;
			    }
			}
			return ("label "+l+" , "+
				"needed by "+labelsNeeded.getValues(l)+" , "+
				"was not output.  labelFound: "+labelFound);
		    }
		});
	    }
	}
    }

    /** Produces an assembly code string for <code>instr</code>.
     *  Uses getRegisterName() to do register name string mapping.
     */
    public String toAssem(Instr instr) {
	String assem = instr.getAssem();
        StringBuffer s = new StringBuffer(assem.length());
	
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
		    StringBuffer var = new StringBuffer(assem.length());
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
		    /*
		    Util.assert( ( ! mustGetRegs ) ||
				 frame.getRegFileInfo().isRegister(temp) ||
				 registerAssigned(instr, temp),
				 "final assembly output for "+
				 "Instr: "+instr+" must have "+
				 "reg assigned to Temp: "+temp);
		    */
		    s.append(getRegisterName(instr, temp,
					     var.toString()));
		    s.append(lastChar);
		}

		break;
		
	    default: 
		s.append(c);
	    }
	}

        // return formatCommentedInstr(s.toString(),instr.toString());
	return s.toString();
    }

    /** Returns the <code>Derivation</code> associated with
	<code>this</code>. Returns <code>null</code> if
	<code>Derivation</code> information is not available.
    */
    public Derivation getDerivation() { return null; }

    /** Returns an assembly code identifier for the register that
	<code>val</code> will be stored into.
    */
    public abstract String getRegisterName(Instr i, Temp val, 
					   String suffix);

    /** Returns an assembly code String version of <code>exec</code>
	with <code>orig</code> in the comments for <code>exec</code>.
	<BR> <B>requires:</B> <code>exec</code> and <code>orig</code>
	     have the same number of lines
	<BR> <B>effects:</B>
	     let s be a new empty string 
	     in for each line:le in <code>exec</code>,
	            let lo be the next line of <code>orig</code> ;
	            s += (le + " @" + lo)
		return s
    */
    public static String formatCommentedInstr(String exec, String orig) {
	StringBuffer sb = new StringBuffer(exec.length() + orig.length()); 
	try {
	    BufferedReader er = new BufferedReader(new StringReader(exec));
	    BufferedReader or = new BufferedReader(new StringReader(orig));
	    String e = er.readLine();
	    String o = or.readLine();
	    while(e != null) {
		sb.append(e + " @ " + o);
		e = er.readLine();
		o = or.readLine();
	    }
	    return sb.toString();
	} catch (IOException e) {
	    Util.assert(false);
	    return "died";
	}
    }
}

