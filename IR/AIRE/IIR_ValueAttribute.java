// IIR_ValueAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ValueAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ValueAttribute.java,v 1.2 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ValueAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_VALUE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ValueAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

