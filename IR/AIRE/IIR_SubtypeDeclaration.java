// IIR_SubtypeDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubtypeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubtypeDeclaration.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubtypeDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SUBTYPE_DECLARATION
    //CONSTRUCTOR:
    public IIR_SubtypeDeclaration() { }
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_SubtypeDefinition get_subtype()
    { return _subtype; }
 
    //MEMBERS:  
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

