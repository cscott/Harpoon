// IIR_NandOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NandOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NandOperator.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NandOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NAND_OPERATOR
    //CONSTRUCTOR:
    public IIR_NandOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

