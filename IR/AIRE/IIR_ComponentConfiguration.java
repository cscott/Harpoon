// IIR_ComponentConfiguration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentConfiguration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentConfiguration.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentConfiguration extends IIR_ConfigurationItem
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_COMPONENT_CONFIGURATION).
     * @return <code>IR_Kind.IR_COMPONENT_CONFIGURATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_COMPONENT_CONFIGURATION; }
    //CONSTRUCTOR:
    public IIR_ComponentConfiguration() { }
    //METHODS:  
    public void set_component_name(IIR component_name)
    { _component_name = component_name; }
 
    public IIR get_component_name()
    { return _component_name; }
 
    public void set_entity_aspect(IIR_LibraryUnit entity_aspect)
    { _entity_aspect = entity_aspect; }
 
    public IIR_LibraryUnit get_entity_aspect()
    { return _entity_aspect; }
 
    public void set_block_configuration(IIR_BlockConfiguration block_configuration)
    { _block_configuration = block_configuration; }
 
    public IIR_BlockConfiguration get_block_configuration()
    { return _block_configuration; }
 
    //MEMBERS:  
    public IIR_DesignatorList instantiation_list;
    public IIR_AssociationList generic_map_aspect;
    public IIR_AssociationList port_map_aspect;

// PROTECTED:
    IIR _component_name;
    IIR_LibraryUnit _entity_aspect;
    IIR_BlockConfiguration _block_configuration;
} // END class

