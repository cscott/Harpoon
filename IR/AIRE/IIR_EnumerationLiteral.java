// IIR_EnumerationLiteral.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EnumerationLiteral</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationLiteral.java,v 1.3 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationLiteral extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

