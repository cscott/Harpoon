// IIR_ArchitectureDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ArchitectureDeclaration</code> class represents
 * one of potentially several implementations of an entity.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ArchitectureDeclaration.java,v 1.4 1998-10-11 02:37:12 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ArchitectureDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ARCHITECTURE_DECLARATION).
     * @return <code>IR_Kind.IR_ARCHITECTURE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ARCHITECTURE_DECLARATION; }

    /** The constructor method initializes an architecture declaration using
     *  an unspecified source location, an unspecified architecture
     *  declarator, and unspecified entry name, no architecture declarations,
     *  no architecture statements, and no attributes. */
    public IIR_ArchitectureDeclaration() { }

    //METHODS:  
    public void set_entity( IIR_EntityDeclaration entity)
    { _entity = entity; }
 
    public IIR_EntityDeclaration get_entity()
    { return _entity; }
 
    //MEMBERS:  
    public IIR_DeclarationList architectecture_declarative_part;
    public IIR_StatementList architecture_statement_part;

// PROTECTED:
    IIR_EntityDeclaration _entity;
} // END class

