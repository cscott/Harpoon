// IIR_FreeQuantityDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_FreeQuantityDeclaration</code> class.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FreeQuantityDeclaration.java,v 1.4 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FreeQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FREE_QUANTITY_DECLARATION).
     * @return <code>IR_Kind.IR_FREE_QUANTITY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FREE_QUANTITY_DECLARATION; }

    /** The constructor method initializes a terminal declaration with
     *  an unspecified source location, and unspecified declarator, and
     *  an unspecified nature. */
    public IIR_FreeQuantityDeclaration() { }
    //METHODS:  
    public IIR_NatureDefinition get_subnature_indication()
    { return _subnature_indication; }
    public void set_subnature_indication(IIR_NatureDefinition subnature)
    { _subnature_indication = subnature; }

    public IIR get_value()
    { return _value; }
    public void set_value(IIR value)
    { _value = value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
    IIR _value;
} // END class

