// IIR_ConcatenationOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcatenationOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcatenationOperator.java,v 1.2 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcatenationOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCATENATION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_ConcatenationOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

