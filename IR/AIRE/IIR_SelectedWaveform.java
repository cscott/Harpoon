// IIR_SelectedWaveform.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SelectedWaveform</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SelectedWaveform.java,v 1.3 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SelectedWaveform extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SELECTED_WAVEFORM; }
    //CONSTRUCTOR:
    public IIR_SelectedWaveform() { }
    //METHODS:  
    public void set_choice(IIR choice)
    { _choice = choice; }
 
    public IIR get_choice()
    { return _choice; }
 
    //MEMBERS:  
    public IIR_WaveformList waveform;

// PROTECTED:
    IIR _choice;
} // END class

