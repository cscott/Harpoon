package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;

/**
 * <code>RETURN</code> objects are used to represent a return from 
 * a method body.
 *
 * @author   Duncan Bryce  <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version  $Id: RETURN.java,v 1.1.2.1 1999-02-18 21:59:53 duncan Exp $
 */
public class RETURN extends Stm {
  /** The value to return */
  public Exp retval;
  
  /** Constructor.
   *  @param retval  the value to return
   */
  public RETURN(TreeFactory tf, HCodeElement source, 
		Exp retval) {
    super(tf, source);
    this.retval=retval;
  }		
  
  public ExpList kids() { return new ExpList(retval, null); }
  public Stm build(ExpList kids) {
    return new RETURN(tf, this, kids.head);
  }

  /** Accept a visitor */
  public void visit(TreeVisitor v) { v.visit(this); }

  public Tree rename(TreeFactory tf, CloningTempMap ctm) {
    return new RETURN(tf, this, (Exp)retval.rename(tf, ctm));
  }

}
