// IIR_SimultaneousAlternativeByChoices.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternativeByChoices</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternativeByChoices.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousAlternativeByChoices extends IIR_SimultaneousAlternative
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES).
     * @return <code>IR_Kind.IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES; }
    //CONSTRUCTOR:
    public IIR_SimultaneousAlternativeByChoices( ) { }
    //METHODS:  
    //MEMBERS:  
    public IIR_ChoiceList choices;

// PROTECTED:
} // END class

