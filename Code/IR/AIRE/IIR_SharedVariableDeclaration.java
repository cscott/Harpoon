// IIR_SharedVariableDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SharedVariableDeclaration</code> class represents
 * variables which may take on a sequence of values, assigned from more
 * than one execution thread.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SharedVariableDeclaration.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SharedVariableDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SHARED_VARIABLE_DECLARATION).
     * @return <code>IR_Kind.IR_SHARED_VARIABLE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SHARED_VARIABLE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_SharedVariableDeclaration() { }

    //METHODS:  
    public void set_value (IIR value)
    { _value = value; }
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
} // END class

