// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Stack;
/**
 * <code>QuadSSA.Code</code> is a code view that exposes the details of
 * the java classfile bytecodes in a quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSA form; that is, every variable has exactly one definition,
 * and <code>PHI</code> functions are used where control flow merges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.22 1998-09-21 01:57:44 cananian Exp $
 */

public class Code extends HCode {
    /** The name of this code view. */
    private static final String codename = "quad-ssa";

    /** The method that this code view represents. */
    HMethod parent;
    /** The byte code underlying this code view. */
    harpoon.IR.Bytecode.Code bytecode;
    /** The quadruples composing this code view. */
    Quad quads;

    /** Creates a <code>Code</code> object from a bytecode object. */
    Code(harpoon.IR.Bytecode.Code bytecode) 
    {
	this.parent = bytecode.getMethod();
        this.bytecode = bytecode;
	//harpoon.Temp.Temp.clear(); /* debug */
	this.quads = Translate.trans(bytecode);
	CleanUp.cleanup1(this); // cleanup null predecessors of phis.
	FixupFunc.fixup(this);
	CleanUp.cleanup2(this); // cleanup unused phi/sigmas.
    }
    /**
     * Return the <code>HMethod</code> this codeview
     * belongs to.
     */
    public HMethod getMethod() { return parent; }

    /**
     * Return the name of this code view.
     * @return the string <code>"quad-ssa"</code>.
     */
    public String getName() { return codename; }
    
    public static void register() {
	HCodeFactory f = new HCodeFactory() {
	    public HCode convert(HMethod m) {
		return new Code( (harpoon.IR.Bytecode.Code)
				 m.getCode("bytecode"));
	    }
	    public String getCodeName() {
		return codename;
	    }
	};
	HMethod.register(f);
    }

    /** Returns the root of the control flow graph. */
    public HCodeElement getRootElement() { return quads; }
    /** Returns the leaves of the control flow graph. */
    public HCodeElement[] getLeafElements() {
	HEADER h = (HEADER) getRootElement();
	return new Quad[] { h.footer };
    }

    /**
     * Returns an ordered list of the <code>Quad</code>s
     * making up this code view.  The root of the graph
     * is in element 0 of the array.
     */
    public HCodeElement[] getElements() { 
	UniqueVector v = new UniqueVector();
	traverse(quads, v);
	HCodeElement[] elements = new Quad[v.size()];
	v.copyInto(elements);
	return (HCodeElement[]) elements;
    }
    /** scan through quad graph and keep a list of the quads found. */
    private void traverse(Quad q, UniqueVector v) {
	// If this is a 'real' node, add it to the list.
	if (v.contains(q)) return;
	v.addElement(q);

	// move on to successors.
	Quad[] next = q.next();
	for (int i=0; i<next.length; i++)
	    traverse(next[i], v);
    }

    public Enumeration getElementsE() {
	return new Enumeration() {
	    Stack s = new Stack();
	    { s.push(quads); } // initialize stack.
	    public boolean hasMoreElements() { return !s.isEmpty(); }
	    public Object nextElement() {
		Quad q = (Quad) s.pop();
		// push successors on stack before returning.
		Quad[] next = q.next();
		for (int i=next.length-1; i>=0; i--)
		    s.push(next[i]);
		// okay.
		return q;
	    }
	};
    }
}
