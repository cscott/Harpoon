// IIR_SLLOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SLLOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SLLOperator.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SLLOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SLL_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SLLOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

