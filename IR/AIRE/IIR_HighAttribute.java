// IIR_HighAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_HighAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_HighAttribute.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_HighAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_HIGH_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_HighAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

