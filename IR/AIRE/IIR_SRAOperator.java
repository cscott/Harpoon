// IIR_SRAOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SRAOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SRAOperator.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SRAOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SRA_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SRAOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

