// IIR_AliasDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_AliasDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AliasDeclaration.java,v 1.4 1998-10-11 02:37:12 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AliasDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ALIAS_DECLARATION).
     * @return <code>IR_Kind.IR_ALIAS_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ALIAS_DECLARATION; }
    //CONSTRUCTOR:
    /** The constructor method initializes an alias declaration with
     *  an unspecified declarator, an unspecified subtype, and an
     *  unspecified name. */
    public IIR_AliasDeclaration() { }
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_TypeDefinition get_subtype()
    { return _subtype; }
 
    public void set_name(IIR name)
    { _name = name; }
 
    public IIR get_name()
    { return _name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype = null;
    IIR _name = null;
} // END class

