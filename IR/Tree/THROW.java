package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;

/**
 * <code>THROW</code> objects are used to represent a thrown exception.
 *
 * @author   Duncan Bryce  <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version  $Id: THROW.java,v 1.1.2.3 1999-06-28 18:49:16 duncan Exp $
 */
public class THROW extends Stm {
    /** The exceptional value to return */
    public Exp retex;

    /** Constructor 
     *  @param retex  the exceptional value to return 
     */
    public THROW(TreeFactory tf, HCodeElement source, 
		 Exp retex) {
	super(tf, source);
	this.retex=retex;
    }		
  
    public ExpList kids() { return new ExpList(retex, null); }
    public int kind() { return TreeKind.THROW; }
    public Stm build(ExpList kids) {
	return new THROW(tf, this, kids.head);
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
	return new THROW(tf, this, (Exp)retex.rename(tf, ctm));
    }

    public String toString() {
	return "THROW(#"+retex.getID()+")";
    }
}
