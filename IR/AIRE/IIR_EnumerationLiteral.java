// IIR_EnumerationLiteral.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EnumerationLiteral</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationLiteral.java,v 1.4 1998-10-11 01:24:56 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationLiteral extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ENUMERATION_LITERAL).
     * @return <code>IR_Kind.IR_ENUMERATION_LITERAL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENUMERATION_LITERAL; }
    //CONSTRUCTOR:
    public IIR_EnumerationLiteral() { }
    //METHODS:  
    public void set_position(IIR position)
    { _position = position; }
 
    public IIR get_position()
    { return _position; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR _position;
} // END class

