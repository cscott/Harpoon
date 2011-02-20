// IIR_SignalAssignmentStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SignalAssignmentStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SignalAssignmentStatement.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SignalAssignmentStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIGNAL_ASSIGNMENT_STATEMENT).
     * @return <code>IR_Kind.IR_SIGNAL_ASSIGNMENT_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIGNAL_ASSIGNMENT_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_SignalAssignmentStatement() { }
    //METHODS:  
    public void set_target(IIR target)
    { _target = target; }
 
    public IIR get_target()
    { return _target; }
 
    public void set_reject_time_expression(IIR reject_time_expression)
    { _reject_time_expression = reject_time_expression; }
 
    public IIR get_reject_time_expression()
    { return _reject_time_expression; }
 
    //MEMBERS:  
    public IIR_WaveformList waveform;

// PROTECTED:
    IIR _target;
    IIR _reject_time_expression;
} // END class

