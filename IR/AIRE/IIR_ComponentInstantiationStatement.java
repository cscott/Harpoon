// IIR_ComponentInstantiationStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentInstantiationStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentInstantiationStatement.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentInstantiationStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_COMPONENT_INSTANTIATION_STATEMENT).
     * @return <code>IR_Kind.IR_COMPONENT_INSTANTIATION_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_COMPONENT_INSTANTIATION_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ComponentInstantiationStatement() { }
    
    //METHODS:  
    public void set_instantiated_unit(IIR instantiated_unit)
    { _instantiated_unit = instantiated_unit; }
 
    public IIR get_instantiated_unit()
    { return _instantiated_unit; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _instantiated_unit;
} // END class

