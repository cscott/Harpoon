// IIR_RemainderOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RemainderOperator</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RemainderOperator.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RemainderOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_REMAINDER_OPERATOR
    //CONSTRUCTOR:
    public IIR_RemainderOperator(){}
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

