// IIR_EntityClassEntry.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * A predefined <code>IIR_EntityClassEntry</code> represents a specific
 * kind of entity within an <code>IIR_EntityClassList</code>.  The
 * <code>IIR_EntityClassList</code> in turn appears only within a group
 * template declaration.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EntityClassEntry.java,v 1.5 1998-10-11 02:37:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EntityClassEntry extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ENTITY_CLASS_ENTRY).
     * @return <code>IR_Kind.IR_ENTITY_CLASS_ENTRY</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENTITY_CLASS_ENTRY; }
    //CONSTRUCTOR:
    public IIR_EntityClassEntry() { }
    //METHODS:  
    public void set_entity_kind(IR_Kind entity_kind)
    { _entity_kind = entity_kind; }
    public IR_Kind get_entity_kind()
    { return _entity_kind; }
 
    public void set_boxed(boolean is_boxed)
    { _boxed = is_boxed; }
    public boolean get_boxed()
    { return _boxed; }
 
    //MEMBERS:  

// PROTECTED:
    IR_Kind _entity_kind;
    boolean _boxed;
} // END class

