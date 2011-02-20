// IIR_BlockStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_BlockStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BlockStatement.java,v 1.5 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BlockStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_BLOCK_STATEMENT).
     * @return <code>IR_Kind.IR_BLOCK_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_BLOCK_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_BlockStatement() { }
    //METHODS:  
    public void set_guard_expression(IIR guard_expression)
    { _guard_expression = guard_expression; }
 
    //MEMBERS:  
    public IIR_GenericList generic_clause;
    public IIR_AssociationList generic_map_aspect;
    public IIR_PortList port_clause;
    public IIR_AssociationList port_map_aspect;
    public IIR_DeclarationList block_declarative_part;
    public IIR_ConcurrentStatementList block_statement_part;

// PROTECTED:
    IIR _guard_expression;
} // END class

