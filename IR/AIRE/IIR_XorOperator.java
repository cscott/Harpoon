// IIR_XorOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_XorOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_XorOperator.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_XorOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_XOR_OPERATOR
    //CONSTRUCTOR:
    public IIR_XorOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

