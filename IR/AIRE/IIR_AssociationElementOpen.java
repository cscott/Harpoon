// IIR_AssociationElementOpen.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_AssociationElementOpen</code> represents
 * either an association between a formal and an implicit actual
 * expression or between the elements of a composite type and the value
 * associated with the specified elements within the aggregate.  The implicit
 * actual value is derived from (1) a delayed binding, (2) an initializer
 * associated with the formal interface declaration, or (3) the
 * (sub)type of the declaration itself.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssociationElementOpen.java,v 1.4 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssociationElementOpen extends IIR_AssociationElement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ASSOCIATION_ELEMENT_OPEN).
     * @return <code>IR_Kind.IR_ASSOCIATION_ELEMENT_OPEN</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ASSOCIATION_ELEMENT_OPEN; }

    /** The constructor initializes an association element by open object
     * with an undefined source location, an undefined formal, an undefined
     * actual, and undefined next value. */
    public IIR_AssociationElementOpen() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

