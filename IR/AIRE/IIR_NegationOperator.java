// IIR_NegationOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NegationOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NegationOperator.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NegationOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NEGATION_OPERATOR
    //CONSTRUCTOR:
    public IIR_NegationOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

