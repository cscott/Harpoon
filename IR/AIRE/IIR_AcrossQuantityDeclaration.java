// IIR_AcrossQuantityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_AcrossQuantityDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AcrossQuantityDeclaration.java,v 1.1 1998-10-10 09:21:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AcrossQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ACROSS_QUANTITY_DECLARATION
    //CONSTRUCTOR:
    public IIR_AcrossQuantityDeclaration() { }
    //METHODS:  
    public void set_expression(IIR expression)
    { _expression = expression; }
    public IIR get_expression()
    { return _expression; }
 
    public void set_tolerance(IIR tolerance)
    { _tolerance = tolerance; }
    public IIR get_tolerance()
    { return _tolerance; }
 
    public void set_plus_terminal_name(IIR plus_terminal_name)
    { _plus_terminal_name = plus_terminal_name; }
    public IIR get_plus_terminal_name()
    { return _plus_terminal_name; }
 
    public void set_minus_terminal_name(IIR minus_terminal_name)
    { _minus_terminal_name = minus_terminal_name; }
    public IIR get_minus_terminal_name()
    { return _minus_terminal_name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _expression;
    IIR _tolerance;
    IIR _plus_terminal_name;
    IIR _minus_terminal_name;
} // END class

