// IIR_SRLOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SRLOperator</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SRLOperator.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SRLOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SRL_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SRLOperator(){}
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

