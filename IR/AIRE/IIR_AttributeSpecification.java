// IIR_AttributeSpecification.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_AttributeSpecification</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AttributeSpecification.java,v 1.5 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AttributeSpecification extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ATTRIBUTE_SPECIFICATION).
     * @return <code>IR_Kind.IR_ATTRIBUTE_SPECIFICATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ATTRIBUTE_SPECIFICATION; }
    //CONSTRUCTOR:
    public IIR_AttributeSpecification() { }
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    public void set_entity_class(IIR_Identifier entity_class)
    { _entity_class = entity_class; }
 
    public IIR_Identifier get_entity_class()
    { return _entity_class; }
 
    //MEMBERS:  
    public IIR_DesignatorList entity_name_list;

// PROTECTED:
    IIR _value;
    IIR_Identifier _entity_class;
} // END class

