// IIR_NullStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_NullStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NullStatement.java,v 1.4 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NullStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_NULL_STATEMENT).
     * @return <code>IR_Kind.IR_NULL_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_NULL_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_NullStatement() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

