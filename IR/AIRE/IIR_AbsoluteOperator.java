// IIR_AbsoluteOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AbsoluteOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AbsoluteOperator.java,v 1.1 1998-10-10 07:53:31 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AbsoluteOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ABSOLUTE_OPERATOR
    //CONSTRUCTOR:
    public IIR_AbsoluteOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

