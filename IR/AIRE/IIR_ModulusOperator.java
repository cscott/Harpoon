// IIR_ModulusOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ModulusOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ModulusOperator.java,v 1.2 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ModulusOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_MODULUS_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_ModulusOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

