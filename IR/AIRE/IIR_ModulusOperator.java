// IIR_ModulusOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ModulusOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ModulusOperator.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ModulusOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_MODULUS_OPERATOR
    //CONSTRUCTOR:
    public IIR_ModulusOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

