// IIR_SLLOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SLLOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SLLOperator.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SLLOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SLL_OPERATOR
    //CONSTRUCTOR:
    public IIR_SLLOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

