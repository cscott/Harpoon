// IIR_LessThanOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LessThanOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LessThanOperator.java,v 1.2 1998-10-11 00:32:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LessThanOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_LESS_THAN_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_LessThanOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

