// IIR_UseClause.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_UseClause</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_UseClause.java,v 1.4 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_UseClause extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_USE_CLAUSE).
     * @return <code>IR_Kind.IR_USE_CLAUSE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_USE_CLAUSE; }
    //CONSTRUCTOR:
    public IIR_UseClause() { }
    
    //METHODS:  
    public void set_selected_name(IIR_Name selected_name)
    { _selected_name = selected_name; }
 
    public IIR_Name get_selected_name()
    { return _selected_name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_Name _selected_name;
} // END class

