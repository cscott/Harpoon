// IIR_EqualityOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EqualityOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EqualityOperator.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EqualityOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_EQUALITY_OPERATOR
    //CONSTRUCTOR:
    public IIR_EqualityOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

