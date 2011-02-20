// IIR_AssociationElementByExpression.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined class <code>IIR_AssociationElementByExpression</code> 
 * represents wither an association between a formal and an expicit actual
 * expression, or an association between elements of a composite type
 * and their values within an aggregate.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssociationElementByExpression.java,v 1.4 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssociationElementByExpression extends IIR_AssociationElement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ASSOCIATION_ELEMENT_BY_EXPRESSION).
     * @return <code>IR_Kind.IR_ASSOCIATION_ELEMENT_BY_EXPRESSION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ASSOCIATION_ELEMENT_BY_EXPRESSION; }

    /** The constructor initializes an association by expression object
     *  with undefined source location, undefined formal, undefined
     *  actual, undefined next value. */
    public IIR_AssociationElementByExpression() { }

    //METHODS:  
    public void set_actual( IIR actual) {
	_actual = actual;
    }
 
    public IIR get_actual() {
	return _actual;
    }
 
    //MEMBERS:  

// PROTECTED:
    IIR _actual;
} // END class

