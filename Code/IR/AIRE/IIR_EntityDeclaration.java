// IIR_EntityDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_EntityDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EntityDeclaration.java,v 1.5 1998-10-11 02:37:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EntityDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ENTITY_DECLARATION).
     * @return <code>IR_Kind.IR_ENTITY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENTITY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_EntityDeclaration() { }
    
    //METHODS:  
    public void set_last_analyzed_architecture(IIR_ArchitectureDeclaration architecture)
    { _last_analyzed_architecture = architecture; }
 
    public IIR_ArchitectureDeclaration get_last_analyzed_architecture()
    { return _last_analyzed_architecture; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ArchitectureDeclaration _last_analyzed_architecture;
} // END class

