// IIR_NotOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_NotOperator</code> class represents the
 * logical NOT operator and its overloadings.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NotOperator.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NotOperator extends IIR_MonadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_NOT_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_NotOperator() { }
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

