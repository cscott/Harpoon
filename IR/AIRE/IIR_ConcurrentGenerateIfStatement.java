// IIR_ConcurrentGenerateIfStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentGenerateIfStatement</code> represents
 * a block which is either elaborated once or not at all, depending on the
 * value of a boolean condition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentGenerateIfStatement.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentGenerateIfStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONCURRENT_GENERATE_IF_STATEMENT).
     * @return <code>IR_Kind.IR_CONCURRENT_GENERATE_IF_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_GENERATE_IF_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ConcurrentGenerateIfStatement() { }
    
    //METHODS:  
    public void set_if_condition(IIR condition)
    { _if_condition = condition; }
 
    public IIR get_if_condition()
    { return _if_condition; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _if_condition;
} // END class

