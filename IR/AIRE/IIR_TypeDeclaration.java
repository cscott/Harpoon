// IIR_TypeDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_TypeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TypeDeclaration.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TypeDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_TYPE_DECLARATION
    //CONSTRUCTOR:
    public IIR_TypeDeclaration() { }
    //METHODS:  
    public void set_type(IIR_TypeDefinition type)
    { _type = type; }
 
    public IIR_TypeDefinition get_type()
    { return _type; }
 
    //MEMBERS:  
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_TypeDefinition _type;
} // END class

