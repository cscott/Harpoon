// IIR_BaseAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BaseAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BaseAttribute.java,v 1.2 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BaseAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_BASE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_BaseAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

