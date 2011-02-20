// IIR_Label.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Label</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Label.java,v 1.5 1998-10-11 02:37:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Label extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_LABEL).
     * @return <code>IR_Kind.IR_LABEL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_LABEL; }
    //CONSTRUCTOR:
    public IIR_Label() { }
    //METHODS:  
    public void set_statement(IIR_SequentialStatement statement)
    { _statement = statement; }
 
    public IIR_Statement get_statement()
    { return _statement; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_SequentialStatement _statement;
} // END class

