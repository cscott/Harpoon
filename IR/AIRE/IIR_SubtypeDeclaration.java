// IIR_SubtypeDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubtypeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubtypeDeclaration.java,v 1.3 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubtypeDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SUBTYPE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_SubtypeDeclaration() { }
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

