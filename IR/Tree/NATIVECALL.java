// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>NATIVECALL</code> objects are statements which stand for
 * native method calls.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NATIVECALL.java,v 1.1.2.5 1999-06-23 23:19:50 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see INVOCATION
 */
public class NATIVECALL extends INVOCATION {
    /** Constructor. */
    public NATIVECALL(TreeFactory tf, HCodeElement source,
		Exp retval, Exp retex, Exp func, ExpList args) {
	super(tf, source, retval, retex, func, args);
    }

    public boolean isNative() { return true; }
  
    public ExpList kids() { 
        return new ExpList
	    (retval, new ExpList(retex, new ExpList(func, args))); 
    }
  
    public Stm build(ExpList kids) {
	return new NATIVECALL(tf, this,
			      kids.head,            // retval
			      kids.tail.head,       // retex
			      kids.tail.tail.head,  // func
			      kids.tail.tail.tail); // args
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new NATIVECALL(tf, this, 
			      (Exp)retval.rename(tf, ctm),
			      (Exp)retex.rename(tf, ctm), 
			      (Exp)func.rename(tf, ctm),
			      ExpList.rename(args, tf, ctm));
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        ExpList list = args;

        s.append("NATIVECALL(# " + retval.getID() + ", #" + retex.getID()+
                 ", #" + func.getID() + ", {");
        while (list != null) {
            s.append(" #" + list.head.getID());
            if (list.tail != null) 
                s.append(",");
            list = list.tail;
        }
        s.append(" })");
        return new String(s);
    }
}
