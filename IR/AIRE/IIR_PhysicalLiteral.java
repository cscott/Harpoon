// IIR_PhysicalLiteral.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_PhysicalLiteral</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PhysicalLiteral.java,v 1.4 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PhysicalLiteral extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PHYSICAL_LITERAL).
     * @return <code>IR_Kind.IR_PHYSICAL_LITERAL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PHYSICAL_LITERAL; }
    //CONSTRUCTOR:
    public IIR_PhysicalLiteral() { }
    //METHODS:  
    public void set_abstract_literal(IIR abstract_literal)
    { _abstract_literal = abstract_literal; }
 
    public IIR get_abstract_literal()
    { return _abstract_literal; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _abstract_literal;
} // END class

