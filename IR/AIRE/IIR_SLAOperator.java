// IIR_SLAOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SLAOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SLAOperator.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SLAOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SLA_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SLAOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

