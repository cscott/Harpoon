// IIR_AttributeDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AttributeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AttributeDeclaration.java,v 1.3 1998-10-11 01:24:53 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AttributeDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ATTRIBUTE_DECLARATION).
     * @return <code>IR_Kind.IR_ATTRIBUTE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ATTRIBUTE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_AttributeDeclaration() { }
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_TypeDefinition get_subtype()
    { return _subtype; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

