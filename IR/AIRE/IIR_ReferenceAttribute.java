// IIR_ReferenceAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ReferenceAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ReferenceAttribute.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ReferenceAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_REFERENCE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ReferenceAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

