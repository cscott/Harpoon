// IIR_ROROperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ROROperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ROROperator.java,v 1.1 1998-10-10 07:53:41 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ROROperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ROR_OPERATOR
    //CONSTRUCTOR:
    public IIR_ROROperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

