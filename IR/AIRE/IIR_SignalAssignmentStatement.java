// IIR_SignalAssignmentStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SignalAssignmentStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SignalAssignmentStatement.java,v 1.2 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SignalAssignmentStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIGNAL_ASSIGNMENT_STATEMENT
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

