// IIR_ROLOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ROLOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ROLOperator.java,v 1.1 1998-10-10 07:53:41 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ROLOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ROL_OPERATOR
    //CONSTRUCTOR:
    public IIR_ROLOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

