// IIR_TransactionAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_TransactionAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TransactionAttribute.java,v 1.2 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TransactionAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_TRANSACTION_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_TransactionAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

