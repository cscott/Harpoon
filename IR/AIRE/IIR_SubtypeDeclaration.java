// IIR_SubtypeDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubtypeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubtypeDeclaration.java,v 1.4 1998-10-11 01:25:04 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubtypeDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SUBTYPE_DECLARATION).
     * @return <code>IR_Kind.IR_SUBTYPE_DECLARATION</code>
     */
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

