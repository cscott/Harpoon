// IIR_IdentityOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IdentityOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IdentityOperator.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IdentityOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_IDENTITY_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_IdentityOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

