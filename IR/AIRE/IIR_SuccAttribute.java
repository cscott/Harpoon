// IIR_SuccAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SuccAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SuccAttribute.java,v 1.2 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SuccAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SUCC_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_SuccAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

