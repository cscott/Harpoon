// IIR_SRAOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SRAOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SRAOperator.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SRAOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SRA_OPERATOR
    //CONSTRUCTOR:
    public IIR_SRAOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

