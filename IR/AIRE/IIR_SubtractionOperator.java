// IIR_SubtractionOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubtractionOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubtractionOperator.java,v 1.2 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubtractionOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SUBTRACTION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SubtractionOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

