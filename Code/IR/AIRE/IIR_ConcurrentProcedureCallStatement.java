// IIR_ConcurrentProcedureCallStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentProcedureCallStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentProcedureCallStatement.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentProcedureCallStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONCURRENT_PROCEDURE_CALL_STATEMENT).
     * @return <code>IR_Kind.IR_CONCURRENT_PROCEDURE_CALL_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_PROCEDURE_CALL_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ConcurrentProcedureCallStatement() { }
    //METHODS:  
    public void set_postponed(boolean postponed)
    { _postponed = postponed; }
 
    public boolean get_postponed()
    { return _postponed; }
 
    public void set_procedure_name(IIR procedure_name)
    { _procedure_name = procedure_name; }
 
    //MEMBERS:  
    public IIR_AssociationList actual_parameter_part;
    public IIR_SequentialStatementList process_statement_part;

// PROTECTED:
    boolean _postponed;
    IIR _procedure_name;
} // END class

