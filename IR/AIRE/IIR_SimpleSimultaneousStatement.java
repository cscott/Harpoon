// IIR_SimpleSimultaneousStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SimpleSimultaneousStatement</code> class
 * described zero or more characteristic expressions.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimpleSimultaneousStatement.java,v 1.2 1998-10-10 09:21:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimpleSimultaneousStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMPLE_SIMULTANEOUS_STATEMENT
    //CONSTRUCTOR:
    public IIR_SimpleSimultaneousStatement(){}
    //METHODS:  
    public void set_left_expression(IIR left_expression)
    { _left_expression = left_expression; }
    public IIR get_left_expression()
    { return _left_expression; }
 
    public void set_right_expression(IIR right_expression)
    { _right_expression = right_expression; }
    public IIR get_right_expression()
    { return _right_expression; }
 
    public void set_tolerance_aspect(IIR tolerance_aspect)
    { _tolerance_aspect = tolerance_aspect; }
    public IIR get_tolerance_aspect()
    { return _tolerance_aspect;}
 
    //MEMBERS:  

// PROTECTED:
    IIR _left_expression;
    IIR _right_expression;
    IIR _tolerance_aspect;
} // END class

