// IIR_ForLoopStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ForLoopStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ForLoopStatement.java,v 1.5 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ForLoopStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FOR_LOOP_STATEMENT).
     * @return <code>IR_Kind.IR_FOR_LOOP_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FOR_LOOP_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ForLoopStatement() { }
    //METHODS:  
    public void set_iteration_scheme(IIR_ConstantDeclaration iterator)
    { _iteration_scheme = iterator; }
 
    public IIR_ConstantDeclaration get_iteration_scheme()
    { return _iteration_scheme; }
 
    //MEMBERS:  
    public IIR_SequentialStatementList sequence_of_statements;
    public IIR_DeclarationList loop_declarations;

// PROTECTED:
    IIR_ConstantDeclaration _iteration_scheme;
} // END class

