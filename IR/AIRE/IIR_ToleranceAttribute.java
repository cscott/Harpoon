// IIR_ToleranceAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ToleranceAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ToleranceAttribute.java,v 1.2 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ToleranceAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_TOLERANCE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ToleranceAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

