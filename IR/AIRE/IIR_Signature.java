// IIR_Signature.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Signature</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Signature.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Signature extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIGNATURE
    //CONSTRUCTOR:
    public IIR_Signature() { }
    //METHODS:  
    public void set_return_type(IIR_TypeDefinition return_type)
    { _return_type = return_type; }
 
    public IIR_TypeDefinition get_return_type()
    { return _return_type; }
 
    //MEMBERS:  
    IIR_DesignatorList argument_type_list;

// PROTECTED:
    IIR_TypeDefinition _return_type;
} // END class

