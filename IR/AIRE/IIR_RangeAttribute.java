// IIR_RangeAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RangeAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RangeAttribute.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RangeAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_RANGE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_RangeAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

