// IIR_SelectedNameByAll.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SelectedNameByAll</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SelectedNameByAll.java,v 1.4 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SelectedNameByAll extends IIR_Name
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SELECTED_NAME_BY_ALL).
     * @return <code>IR_Kind.IR_SELECTED_NAME_BY_ALL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SELECTED_NAME_BY_ALL; }
    //CONSTRUCTOR:
    public IIR_SelectedNameByAll() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

