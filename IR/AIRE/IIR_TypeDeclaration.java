// IIR_TypeDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_TypeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TypeDeclaration.java,v 1.3 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TypeDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_TYPE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_TypeDeclaration() { }
    //METHODS:  
    public void set_type(IIR_TypeDefinition type)
    { _type = type; }
 
    public IIR_TypeDefinition get_type()
    { return _type; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_TypeDefinition _type;
} // END class

