// IIR_SRLOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SRLOperator</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SRLOperator.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SRLOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SRL_OPERATOR
    //CONSTRUCTOR:
    public IIR_SRLOperator(){}
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

