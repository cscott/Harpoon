// IIR_EqualityOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EqualityOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EqualityOperator.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EqualityOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_EQUALITY_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_EqualityOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

