// IIR_SimpleNameAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimpleNameAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimpleNameAttribute.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimpleNameAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMPLE_NAME_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_SimpleNameAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

