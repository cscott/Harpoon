// IIR_XnorOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_XnorOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_XnorOperator.java,v 1.2 1998-10-11 00:32:29 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_XnorOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_XNOR_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_XnorOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

