// IIR_PosAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PosAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PosAttribute.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PosAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_POS_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_PosAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

