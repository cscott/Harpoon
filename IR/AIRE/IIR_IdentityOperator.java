// IIR_IdentityOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IdentityOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IdentityOperator.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IdentityOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_IDENTITY_OPERATOR
    //CONSTRUCTOR:
    public IIR_IdentityOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

