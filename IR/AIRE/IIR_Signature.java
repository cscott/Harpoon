// IIR_Signature.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Signature</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Signature.java,v 1.3 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Signature extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIGNATURE; }
    //CONSTRUCTOR:
    public IIR_Signature() { }
    //METHODS:  
    public void set_return_type(IIR_TypeDefinition return_type)
    { _return_type = return_type; }
 
    public IIR_TypeDefinition get_return_type()
    { return _return_type; }
 
    //MEMBERS:  
    public IIR_DesignatorList argument_type_list;

// PROTECTED:
    IIR_TypeDefinition _return_type;
} // END class

