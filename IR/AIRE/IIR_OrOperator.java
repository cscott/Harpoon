// IIR_OrOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_OrOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_OrOperator.java,v 1.1 1998-10-10 07:53:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_OrOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_OR_OPERATOR
    //CONSTRUCTOR:
    public IIR_OrOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

