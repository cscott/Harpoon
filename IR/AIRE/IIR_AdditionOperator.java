// IIR_AdditionOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AdditionOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AdditionOperator.java,v 1.1 1998-10-10 07:53:31 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AdditionOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ADDITION_OPERATOR
    //CONSTRUCTOR:
    public IIR_AdditionOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

