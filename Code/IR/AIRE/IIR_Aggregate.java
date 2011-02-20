// IIR_Aggregate.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Aggregate</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Aggregate.java,v 1.4 1998-10-11 02:37:11 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Aggregate extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_AGGREGATE).
     * @return <code>IR_Kind.IR_AGGREGATE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_AGGREGATE; }
    //CONSTRUCTOR:
    public IIR_Aggregate() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

