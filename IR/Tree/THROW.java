package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>THROW</code> objects are used to represent a thrown exception.
 *
 * @author   Duncan Bryce  <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version  $Id: THROW.java,v 1.1.2.7 1999-08-03 21:12:58 duncan Exp $
 */
public class THROW extends Stm implements Typed {
    /** The exceptional value to return */
    public Exp retex;

    /** Constructor 
     *  @param retex  the exceptional value to return 
     */
    public THROW(TreeFactory tf, HCodeElement source, 
		 Exp retex) {
	super(tf, source, 0);
	this.retex=retex;
	Util.assert(retex.type()==POINTER);
	Util.assert(tf == retex.tf, "This and Retex must have same tree factory");
    }		
  
    public ExpList kids() { return new ExpList(retex, null); }
    public int kind() { return TreeKind.THROW; }

    public Stm build(ExpList kids) { return build(tf, kids); } 
    public Stm build(TreeFactory tf, ExpList kids) { 
	Util.assert(tf == kids.head.tf);
	return new THROW(tf, this, kids.head);
    }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
	return new THROW(tf, this, (Exp)retex.rename(tf, ctm));
    }

    /** @return <code>Type.POINTER</code> */
    public int type() { return POINTER; }
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, POINTER); }
    public boolean isFloatingPoint() { return false; }
    
    public String toString() {
	return "THROW(#"+retex.getID()+")";
    }
}
