// IIR_AdditionOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AdditionOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AdditionOperator.java,v 1.2 1998-10-11 00:32:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AdditionOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ADDITION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_AdditionOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

