// IIR_ThroughQuantityDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ThroughQuantityDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ThroughQuantityDeclaration.java,v 1.4 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ThroughQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_THROUGH_QUANTITY_DECLARATION).
     * @return <code>IR_Kind.IR_THROUGH_QUANTITY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_THROUGH_QUANTITY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_ThroughQuantityDeclaration(){}
    //METHODS:  
    public void set_across_aspect_expression(IIR across_aspect_expression) {
	_across_aspect_expression = across_aspect_expression;
    }
    public IIR get_across_aspect_expression()
    { return _across_aspect_expression; }
 
    public void set_across_aspect_tolerance(IIR across_aspect_tolerance) {
	_across_aspect_tolerance = across_aspect_tolerance;
    }
    public IIR get_across_aspect_tolerance()
    { return _across_aspect_tolerance; }
 
    public void set_expression(IIR expression)
    { _expression = expression; }
    public IIR get_expression()
    { return _expression; }
 
    public void set_tolerance(IIR tolerance)
    { _tolerance = tolerance;}
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
    IIR _across_aspect_expression;
    IIR _across_aspect_tolerance;
    IIR _expression;
    IIR _tolerance;
    IIR _plus_terminal_name;
    IIR _minus_terminal_name;
} // END class

