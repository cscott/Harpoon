// IIR_ConcurrentConditionalSignalAssignment.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentConditionalSignalAssignment</code> 
 * class represents a signal assignment wherein a nested if-then-else
 * clause is evaluated to determine the waveform assigned to a target.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentConditionalSignalAssignment.java,v 1.2 1998-10-10 09:21:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentConditionalSignalAssignment extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT
    //CONSTRUCTOR:
    public IIR_ConcurrentConditionalSignalAssignment() { }
    //METHODS:  
    public void set_postponed(boolean postponed)
    { _postponed = postponed; }
    public boolean get_postponed()
    { return _postponed; }
 
    public void set_target(IIR t)
    { _target = t; }
    public IIR get_target()
    { return _target; }
 
    public void set_guarded(boolean guarded)
    { _guarded = guarded; }
    public boolean get_guarded()
    { return _guarded; }
 
    public void set_delay_mechanism(IIR_DelayMechanism delay_mechanism)
    { _delay_mechanism = delay_mechanism; }
    public IIR_DelayMechanism get_delay_mechanism()
    { return _delay_mechanism; }
 
    public void set_reject_time_expression(IIR reject_time_expression)
    { _reject_time_expression = reject_time_expression; }
    public IIR get_reject_time_expression()
    { return _reject_time_expression; }
 
    //MEMBERS:  
    public IIR_ConditionalWaveformList conditional_waveforms;

// PROTECTED:
    boolean _postponed;
    IIR _target;
    boolean _guarded;
    IIR_DelayMechanism _delay_mechanism;
    IIR _reject_time_expression;
} // END class

