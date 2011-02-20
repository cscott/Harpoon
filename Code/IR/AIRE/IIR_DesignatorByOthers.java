// IIR_DesignatorByOthers.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignatorByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignatorByOthers.java,v 1.4 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignatorByOthers extends IIR_Designator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_DESIGNATOR_BY_OTHERS).
     * @return <code>IR_Kind.IR_DESIGNATOR_BY_OTHERS</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_DESIGNATOR_BY_OTHERS; }
    //CONSTRUCTOR:
    public IIR_DesignatorByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

