// IIR_ObjectDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ObjectDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ObjectDeclaration.java,v 1.3 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ObjectDeclaration extends IIR_Declaration
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
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

