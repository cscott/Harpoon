// IIR_ExitStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ExitStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ExitStatement.java,v 1.4 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ExitStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_EXIT_STATEMENT).
     * @return <code>IR_Kind.IR_EXIT_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_EXIT_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ExitStatement() { }
    //METHODS:  
    public void set_enclosing_loop(IIR_SequentialStatement enclosing_loop)
    { _enclosing_loop = enclosing_loop; }
 
    public IIR_SequentialStatement get_enclosing_loop()
    { return _enclosing_loop; }
 
    public void set_condition(IIR condition)
    { _condition = condition; }
 
    public IIR get_condition()
    { return _condition; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_SequentialStatement _enclosing_loop;
    IIR _condition;
} // END class

