// IIR_NorOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NorOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NorOperator.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NorOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_NOR_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_NorOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

