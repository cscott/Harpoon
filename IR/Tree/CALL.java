// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>CALL</code> objects are statements which stand for 
 * non-native method calls.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CALL.java,v 1.1.2.13 1999-08-03 21:12:57 duncan Exp $
 * @see harpoon.IR.Quads.CALL
 * @see INVOCATION
 * @see NATIVECALL
 */
public class CALL extends INVOCATION {
    public CALL(TreeFactory tf, HCodeElement source,
		Exp retval, Exp retex, Exp func, ExpList args) {
	super(tf, source, retval, retex, func, args);
    }
  
    public boolean isNative() { return false; }

    public ExpList kids() {
        return new ExpList
	    (retval, new ExpList(retex, new ExpList(func, args))); 
    }

    public int kind() { return TreeKind.CALL; }

    public Stm build(ExpList kids) { return build(tf, kids); }

    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf && 
		    tf == kids.tail.head.tf &&
		    tf == kids.tail.tail.head.tf);
	for (ExpList e = kids.tail.tail.tail; e!=null; e=e.tail)
	    Util.assert(tf == e.head.tf);
	return new CALL(tf, this,
			kids.head,            // retval
			kids.tail.head,       // retex
			kids.tail.tail.head,  // func
			kids.tail.tail.tail); // args
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new CALL(tf, this, 
			(Exp)retval.rename(tf, ctm),
			(Exp)retex.rename(tf, ctm), 
			(Exp)func.rename(tf, ctm),
			ExpList.rename(args, tf, ctm));
  }

    public String toString() {
        ExpList list;
        StringBuffer s = new StringBuffer();
        s.append("CALL(#"+retval.getID()+", #"+retex.getID()+
                 ", #" + func.getID() + ", {");
        list = args;
        while (list != null) {
            s.append(" #"+list.head.getID());
            if (list.tail != null) {
                s.append(",");
            }
            list = list.tail;
        }
        s.append(" })");
        return new String(s);
    }
}
