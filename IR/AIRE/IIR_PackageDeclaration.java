// IIR_PackageDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_PackageDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PackageDeclaration.java,v 1.5 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PackageDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PACKAGE_DECLARATION).
     * @return <code>IR_Kind.IR_PACKAGE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PACKAGE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_PackageDeclaration() { }
    //METHODS:  
    public void set_package_body( IIR_PackageBodyDeclaration package_body)
    { _package_body = package_body; }
    public IIR_PackageBodyDeclaration get_package_body()
    { return _package_body; }
 
    //MEMBERS:  
    public IIR_DeclarationList package_declarative_part;

// PROTECTED:
    IIR_PackageBodyDeclaration _package_body;
} // END class

