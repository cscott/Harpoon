// IIR_ConfigurationDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConfigurationDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConfigurationDeclaration.java,v 1.5 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConfigurationDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONFIGURATION_DECLARATION).
     * @return <code>IR_Kind.IR_CONFIGURATION_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONFIGURATION_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_ConfigurationDeclaration() { }
    //METHODS:  
    public void set_block_configuration(IIR_BlockConfiguration block_configuration)
    { _block_configuration = block_configuration; }
 
    public IIR_BlockConfiguration get_block_configuration()
    { return _block_configuration; }
 
    public void set_entity(IIR_EntityDeclaration entity)
    { _entity = entity; }
 
    public IIR_EntityDeclaration get_entity()
    { return _entity; }
 
    //MEMBERS:  
    public IIR_DeclarationList configuration_declarative_part;

// PROTECTED:
    IIR_BlockConfiguration _block_configuration;
    IIR_EntityDeclaration _entity;
} // END class

