// IIR_PredAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PredAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PredAttribute.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PredAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_PRED_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_PredAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

