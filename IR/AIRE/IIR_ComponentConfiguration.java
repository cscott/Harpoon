// IIR_ComponentConfiguration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentConfiguration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentConfiguration.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentConfiguration extends IIR_ConfigurationItem
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_COMPONENT_CONFIGURATION
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
    IIR_DesignatorList instantiation_list;
    IIR_AssociationList generic_map_aspect;
    IIR_AssociationList port_map_aspect;

// PROTECTED:
    IIR _component_name;
    IIR_LibraryUnit _entity_aspect;
    IIR_BlockConfiguration _block_configuration;
} // END class

