// IIR_RemainderOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RemainderOperator</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RemainderOperator.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RemainderOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_REMAINDER_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_RemainderOperator(){}
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

