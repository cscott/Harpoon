// IIR_ROROperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ROROperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ROROperator.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ROROperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ROR_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_ROROperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

