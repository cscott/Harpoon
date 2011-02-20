// IIR_ProcedureCallStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ProcedureCallStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ProcedureCallStatement.java,v 1.5 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ProcedureCallStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PROCEDURE_CALL_STATEMENT).
     * @return <code>IR_Kind.IR_PROCEDURE_CALL_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PROCEDURE_CALL_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ProcedureCallStatement() { }
    //METHODS:  
    public void set_procedure_name(IIR procedure_name)
    { _procedure_name = procedure_name; }
 
    public IIR get_procedure_name()
    { return _procedure_name; }
 
    //MEMBERS:  
    public IIR_AssociationList actual_parameter_part;

// PROTECTED:
    IIR _procedure_name;
} // END class

