// IIR_AscendingAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AscendingAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AscendingAttribute.java,v 1.2 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AscendingAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ASCENDING_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_AscendingAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

