// IIR_SelectedWaveform.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SelectedWaveform</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SelectedWaveform.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SelectedWaveform extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SELECTED_WAVEFORM).
     * @return <code>IR_Kind.IR_SELECTED_WAVEFORM</code>
     */
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

