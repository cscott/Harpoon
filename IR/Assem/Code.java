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
import harpoon.Util.Util;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.UnmodifiableIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
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
 * @version $Id: Code.java,v 1.6 2002-08-31 00:24:32 cananian Exp $
 */
public abstract class Code extends HCode<Instr> {
    private static boolean DEBUG = true;

    /** The method that this code view represents. */
    protected HMethod parent;
    /** The root Instr of the Instrs composing this code view. */
    protected Instr instrs;
    /** Instruction factory. */
    protected final InstrFactory inf;
    /** The Frame associated with this codeview. */
    protected final Frame frame;
    /** Keep track of modifications to this <code>Code</code> so that the
     *  <code>getElementsI()</code> <code>Iterator</code> can fail-fast. */
    int modCount=0;

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
            public Code getParent() { return Code.this; }
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

    public HMethod getMethod() { return parent; }
    public Instr getRootElement() { return instrs; }
    public Instr[] getLeafElements() { return null; }

    /** Returns an <code>Iterator</code> over the instructions in this
	codeview.  
     *
     *  @return         An iterator over the <code>Instr</code>s
     *                  making up this code view.  The root Instr is
     *                  the first element in the iteration. */
    public Iterator<Instr> getElementsI() { 
	return new UnmodifiableIterator<Instr>() {
	    // record # of modifications to enable fail-fast.
	    int modCount = Code.this.modCount;
	    // setup starting point.
	    Instr instr = getRootElement();
	    public boolean hasNext() { return (instr != null); }
	    public Instr next() {
		if (modCount != Code.this.modCount)
		    throw new ConcurrentModificationException();
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
    public ArrayFactory<Instr> elementArrayFactory() { 
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
	System.out.
	@see Code#print(java.io.PrintWriter)
    */
    public void print() {
	print(new java.io.PrintWriter(System.out));
    }
    
    /** Prints the assembly instructions of <code>this</code> to
	<code>pw</code>.  Default implementation is just a wrapper
	call to <code>myPrint(pw, true, false)</code>, which turns
	each Instr into its architecture specific assembly format and
	omits Instr ID number information.
    */
    public void print(java.io.PrintWriter pw, PrintCallback<Instr> callback) {
	myPrint(pw, true, false, callback);
    }
    
    /** Simple wrapper around myPrint passing a nop PrintCallback. */
    public final void myPrint(java.io.PrintWriter apw, boolean assem) {
	myPrint(apw, assem, new PrintCallback<Instr>());
    }

    /** Displays the assembly instructions of this codeview. Attempts
     *  to do so in a well-formatted, easy to read way. <BR>
     *  XXX - currently uses generic, not so easy to read, printer.
     *
     *  @deprecated Use Code#myPrint(PrintWriter,boolean,PrintCallback)
     * 
     *  @param  pw    The PrintWriter to send the formatted output to.
     *  @param  assem If true, uses <code>toAssem(Instr)</code> to
     *          convert Instrs to assembly form.  Else just calls
     *          <code>Instr.toString()</code>. 
     *  @param  annotateID If true, prints out the ID for each Instr
     *          before printing the Instr itself.
     *  @see Code#toAssem
     */
    protected final void myPrint(java.io.PrintWriter apw, 
				 boolean assem,
				 final boolean annotateID,
				 final PrintCallback<Instr> callback) {
	myPrint(apw, assem, new PrintCallback<Instr>() {
		public void printBefore(java.io.PrintWriter pw, Instr hce ){
		    callback.printBefore(pw,hce);
		    if (annotateID) {
			pw.print( hce.getID() );
			pw.print( '\t' );
		    }
		}
		public void printAfter(java.io.PrintWriter pw, Instr hce ){
		    callback.printAfter(pw, hce);
		}
	    });
    }

    /** Displays the assembly instructions of this codeview. Attempts
     *  to do so in a well-formatted, easy to read way. <BR>
     *  XXX - currently uses generic, not so easy to read, printer.
     *
     *  @param  pw    The PrintWriter to send the formatted output to.
     *  @param  assem If true, uses <code>toAssem(Instr)</code> to
     *          convert Instrs to assembly form.  Else just calls
     *          <code>Instr.toString()</code>. 
     *  @see Code#toAssem
     */
    protected final void myPrint(java.io.PrintWriter pw, 
				 boolean assem,
				 PrintCallback<Instr> callback) {
	final HashSet outputLabels = new HashSet();
	final MultiMap labelsNeeded = new GenericMultiMap();

	for (Instr instr=instrs; instr != null; instr=instr.getNext()) {
	    StringBuffer str = new StringBuffer();

            if (instr instanceof InstrLABEL ||
		instr instanceof InstrDIRECTIVE) {

                str.append(instr.toString());

            } else {
		try {
		    String asmstr = (assem?toAssem(instr):instr.toString());
		    // adding one because reader requires sz > 0
		    BufferedReader reader = 
			new BufferedReader
			    (new StringReader(asmstr),1+asmstr.length());
		    String s = reader.readLine();
		    while (s != null) {
			str.append("\t"+ s);
			s = reader.readLine();
			if (s!=null) str.append("\n");
		    }
		} catch (IOException e ) {
		    assert false : "IOException " + e.toString() + 
				" should not be thrown during assembly"+
				" code processing.";
		}
            }

	    callback.printBefore(pw, instr);
	    pw.print(str.toString()); // FSK: was println!
	    callback.printAfter(pw, instr);
	    pw.println();             // FSK: moved after callback
        }
	
	pw.flush();

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
			assert false : "index mismatch in "+assem + 
				    " " + Arrays.asList(instr.def());
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
			assert false : "index mismatch in "+assem + 
				    " " + Arrays.asList(instr.use());
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
			assert false : "index mismatch in "+assem + 
				    " " + Arrays.asList(instr.use());
			s.append("L?");
		    }
		    break;
		}
		case '`':
		    s.append("`");
		    break;
		default:
		    assert false : "error parsing "+assem;
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
		    assert ( ! mustGetRegs ) ||
				 frame.getRegFileInfo().isRegister(temp) ||
				 registerAssigned(instr, temp) : ("final assembly output for "+
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
	    assert false;
	    return "died";
	}
    }
}

