package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>THROW</code> objects are used to represent a thrown exception.
 *
 * @author   Duncan Bryce  <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version  $Id: THROW.java,v 1.1.2.4 1999-06-29 07:34:53 cananian Exp $
 */
public class THROW extends Stm implements Typed {
    /** The exceptional value to return */
    public Exp retex;

    /** Constructor 
     *  @param retex  the exceptional value to return 
     */
    public THROW(TreeFactory tf, HCodeElement source, 
		 Exp retex) {
	super(tf, source);
	this.retex=retex;
	Util.assert(retex.type()==POINTER);
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

    /** @return <code>Type.POINTER</code> */
    public int type() { return POINTER; }
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, POINTER); }
    public boolean isFloatingPoint() { return false; }
    
    public String toString() {
	return "THROW(#"+retex.getID()+")";
    }
}
