// IIR_AndOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AndOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AndOperator.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AndOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_AND_OPERATOR
    //CONSTRUCTOR:
    public IIR_AndOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

