// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import java.util.Enumeration;
import harpoon.Util.UniqueVector;
import harpoon.Util.Util;
/**
 * <code>QuadSSA.Code</code> is a code view that exposes the details of
 * the java classfile bytecodes in a quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSA form; that is, every variable has exactly one definition,
 * and <code>PHI</code> functions are used where control flow merges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.7 1998-09-03 18:47:47 cananian Exp $
 */

public class Code extends HCode {
    /** The name of this code view. */
    private static final String codename = "quad-ssa";

    /** The method that this code view represents. */
    HMethod parent;
    /** The byte code underlying this code view. */
    harpoon.ClassFile.Bytecode.Code bytecode;
    /** The quadruples composing this code view. */
    Quad quads;

    /** Creates a <code>Code</code> object from a bytecode object. */
    Code(HMethod parent, harpoon.ClassFile.Bytecode.Code bytecode) 
    {
	this.parent = parent;
        this.bytecode = bytecode;
	this.quads = Translate.trans(bytecode);
	parent.putCode(this);
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
    
    /**
     * Convert from a different code view, by way of intermediates.
     * <code>QuadSSA</code> is created from the basic codeview,
     * <code>Bytecode</code>.
     */
    public static HCode convertFrom(HCode codeview) {
	// try to fetch pre-existing conversion results.
	HCode c = codeview.getMethod().getCode(codename);
	if (c!=null) return c;
	// otherwise, try to make a 'bytecode,' which is the
	// format we can convert from.
	if (!codeview.getName().equals("bytecode"))
	    codeview=harpoon.ClassFile.Bytecode.Code.convertFrom(codeview);
	// if we can't get a format we understand, we can't convert.
	if (codeview==null) return null;
	// convert from bytecode.
	if (!(codeview instanceof harpoon.ClassFile.Bytecode.Code))
	    throw new Error("getName() or convertFrom() is not working.");
	return new Code(codeview.getMethod(), 
			(harpoon.ClassFile.Bytecode.Code) codeview);
    }

    /**
     * Return an ordered list of the <code>Quad</code>s
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

	// include 'inner blocks' of try/monitor.
	if (q instanceof MONITOR)
	    traverse(((MONITOR)q).block, v);
	if (q instanceof TRY) {
	    TRY t = (TRY) q;
	    traverse(t.tryBlock, v);
	    traverse(t.catchBlock, v);
	}
	// move on to successors.
	Quad[] next = q.next();
	for (int i=0; i<next.length; i++)
	    if (next[i]!=null) // found at end of try/monitor blocks
		traverse(next[i], v);
    }
}
