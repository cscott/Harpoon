// IIR_BreakElement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_BreakElement</code> denotes a single choice
 * within an <code>IIR_BreakElementList</code>.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BreakElement.java,v 1.2 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BreakElement extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_BREAK_ELEMENT; }
    //CONSTRUCTOR:
    public IIR_BreakElement() { }
    //METHODS:  
    public void set_quantity_name(IIR value)
    { _quantity_name = value; }
 
    public IIR get_quantity_name()
    { return _quantity_name; }
 
    public void set_expression(IIR value)
    { _expression = value; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _quantity_name;
    IIR _expression;
} // END class

