// IIR_SLAOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SLAOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SLAOperator.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SLAOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SLA_OPERATOR
    //CONSTRUCTOR:
    public IIR_SLAOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

