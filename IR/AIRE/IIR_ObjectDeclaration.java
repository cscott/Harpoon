// IIR_ObjectDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ObjectDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ObjectDeclaration.java,v 1.5 1998-10-11 01:24:59 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ObjectDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
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

