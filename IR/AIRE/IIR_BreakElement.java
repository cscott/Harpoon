// IIR_BreakElement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_BreakElement</code> denotes a single choice
 * within an <code>IIR_BreakElementList</code>.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BreakElement.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BreakElement extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_BREAK_ELEMENT
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

