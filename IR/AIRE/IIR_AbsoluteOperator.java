// IIR_AbsoluteOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AbsoluteOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AbsoluteOperator.java,v 1.2 1998-10-11 00:32:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AbsoluteOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ABSOLUTE_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_AbsoluteOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

