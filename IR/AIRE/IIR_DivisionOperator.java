// IIR_DivisionOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DivisionOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DivisionOperator.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DivisionOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_DIVISION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_DivisionOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

