// IIR_InequalityOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_InequalityOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_InequalityOperator.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_InequalityOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_INEQUALITY_OPERATOR
    //CONSTRUCTOR:
    public IIR_InequalityOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

