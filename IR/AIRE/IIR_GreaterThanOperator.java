// IIR_GreaterThanOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GreaterThanOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GreaterThanOperator.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GreaterThanOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_GREATER_THAN_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_GreaterThanOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

