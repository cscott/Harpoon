// IIR_ObjectDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ObjectDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ObjectDeclaration.java,v 1.1 1998-10-10 07:53:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ObjectDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_TypeDefinition get_subtype()
    { return _subtype; }
 
    //MEMBERS:  
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

