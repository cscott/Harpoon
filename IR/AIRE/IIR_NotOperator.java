// IIR_NotOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_NotOperator</code> class represents the
 * logical NOT operator and its overloadings.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NotOperator.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NotOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NOT_OPERATOR
    //CONSTRUCTOR:
    public IIR_NotOperator() { }
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

