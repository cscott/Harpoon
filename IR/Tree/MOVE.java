// MOVE.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * <code>MOVE</code> statements assign a value to a location.
 * <code>MOVE(TEMP <i>t</i>, <i>e</i>)</code> evaluates expression <i>e</i>
 * and assigns its value into temporary <i>t</i>.
 * <code>MOVE(MEM(<i>e1</i>, <i>k</i>), <i>e2</i>)</code> evaluates
 * expression <i>e1</i> yielding address <i>a</i>, then evaluates <i>e2</i>
 * and assigns its value into memory starting at address <i>a</i>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MOVE.java,v 1.1.2.11 1999-07-28 20:45:15 pnkfelix Exp $
 */
public class MOVE extends Stm implements Typed {
    /** The expression giving the destination for the computed value. */
    public Exp dst;
    /** The expression for the computed value. */
    public Exp src;
    /** Constructor. 
     * <p>The type of the <code>src</code> expression and of the
     * <code>dst</code> expression must be identical.  (Use
     * a <code>UNOP</code> to convert, if they are necessary.)
     */
    public MOVE(TreeFactory tf, HCodeElement source,
		Exp dst, Exp src) {
	super(tf, source);
	this.dst=dst; this.src=src;
	Util.assert(dst!=null && src!=null, "Dest and Source cannot be null");
	Util.assert(dst.type()==src.type(), 
		    "Dest (type:"+Type.toString(dst.type()) + 
		    ") and Source (type:" + Type.toString(src.type()) + 
		    ") must have same type");
    }
  
    protected Set defSet() { 
	Set def = new HashSet();
	if (dst.kind()==TreeKind.TEMP) def.add(((TEMP)dst).temp);
	return def;
    }
	
    protected Set useSet() { 
	Set uses = new HashSet();
	uses.addAll(src.useSet());
	if (!(dst.kind()==TreeKind.TEMP)) uses.addAll(dst.useSet());

	return uses;
    }

    public ExpList kids() {
        if (dst.kind()==TreeKind.MEM)
	   return new ExpList(((MEM)dst).exp, new ExpList(src,null));
	else return new ExpList(src,null);
    }

    public int kind() { return TreeKind.MOVE; }

    public Stm build(ExpList kids) {
        if (dst.kind()==TreeKind.MEM)
	    return new MOVE(tf, this, dst.build(new ExpList(kids.head, null)),
			    kids.tail.head);
	else return new MOVE(tf, this, dst, kids.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); } 

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new MOVE(tf, this,
			(Exp)dst.rename(tf, ctm),
			(Exp)src.rename(tf, ctm));
    }

    /** @return the type of <code>dst</code> expression. */
    public int type() { return dst.type(); }
    public boolean isDoubleWord() { return dst.isDoubleWord(); }
    public boolean isFloatingPoint() { return dst.isFloatingPoint(); }

    public String toString() {
        return "MOVE(#"+dst.getID()+", #"+src.getID()+")";
    }
}

