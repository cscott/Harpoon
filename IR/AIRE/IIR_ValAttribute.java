// IIR_ValAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ValAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ValAttribute.java,v 1.2 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ValAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_VAL_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ValAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

