// IIR_SubtractionOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubtractionOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubtractionOperator.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubtractionOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SUBTRACTION_OPERATOR
    //CONSTRUCTOR:
    public IIR_SubtractionOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

