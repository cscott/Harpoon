package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>RETURN</code> objects are used to represent a return from 
 * a method body.
 *
 * @author   Duncan Bryce  <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version  $Id: RETURN.java,v 1.1.2.6 1999-07-30 20:20:15 pnkfelix Exp $
 */
public class RETURN extends Stm implements Typed {
    /** The value to return */
    public Exp retval;
  
    /** Constructor.
     *  @param retval  the value to return
     */
    public RETURN(TreeFactory tf, HCodeElement source, 
		  Exp retval) {
	super(tf, source, 0);
	this.retval=retval;
	Util.assert(tf == retval.tf, "This and Retval must have same tree factory");
    }		
  
    public ExpList kids() { return new ExpList(retval, null); }
    public int kind() { return TreeKind.RETURN; }
    public Stm build(ExpList kids) {
	return new RETURN(tf, this, kids.head);
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
	return new RETURN(tf, this, (Exp)retval.rename(tf, ctm));
    }

    /** @return the type of the return value expression */
    public int type() { return retval.type(); }
    public boolean isDoubleWord() { return retval.isDoubleWord(); }
    public boolean isFloatingPoint() { return retval.isFloatingPoint(); }
    
    public String toString() {
	return "RETURN(#"+retval.getID()+")";
    }

}
