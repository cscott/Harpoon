// IIR_LessThanOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LessThanOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LessThanOperator.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LessThanOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LESS_THAN_OPERATOR
    //CONSTRUCTOR:
    public IIR_LessThanOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

