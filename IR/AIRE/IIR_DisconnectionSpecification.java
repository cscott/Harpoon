// IIR_DisconnectionSpecification.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_DisconnectionSpecification</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DisconnectionSpecification.java,v 1.5 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DisconnectionSpecification extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_DISCONNECTION_SPECIFICATION).
     * @return <code>IR_Kind.IR_DISCONNECTION_SPECIFICATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_DISCONNECTION_SPECIFICATION; }
    //CONSTRUCTOR:
    public IIR_DisconnectionSpecification() { }
    //METHODS:  
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    public void set_time_expression(IIR time_expression)
    { _time_expression = time_expression; }
 
    public IIR get_time_expression()
    { return _time_expression; }
 
    //MEMBERS:  
    public IIR_DesignatorList guarded_signal_list;

// PROTECTED:
    IIR_TypeDefinition _type_mark;
    IIR _time_expression;
} // END class

