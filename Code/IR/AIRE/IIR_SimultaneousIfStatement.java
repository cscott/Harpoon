// IIR_SimultaneousIfStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousIfStatement</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousIfStatement.java,v 1.5 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousIfStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIMULTANEOUS_IF_STATEMENT).
     * @return <code>IR_Kind.IR_SIMULTANEOUS_IF_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMULTANEOUS_IF_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_SimultaneousIfStatement(){}
    //METHODS:  
    public void set_condition(IIR condition){
       _condition = condition;
    }
 
    public IIR get_condition(){return _condition;}
 
    public void set_elsif(IIR_SimultaneousElsif elsif){
       _elsif = elsif; 
    }
 
    public IIR_SimultaneousElsif get_elsif(){return _elsif;}
 
    //MEMBERS:  
    public IIR_SimultaneousStatementList then_statement_list;
    public IIR_SimultaneousStatementList else_statement_list;

// PROTECTED:
    IIR _condition;
    IIR_SimultaneousElsif _elsif;
} // END class

