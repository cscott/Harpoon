// IIR_StableAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_StableAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_StableAttribute.java,v 1.2 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_StableAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_STABLE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_StableAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

