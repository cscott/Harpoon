// IIR_PackageBodyDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_PackageBodyDeclaration</code> class represents
 * the optional implementation part of a package declaration.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PackageBodyDeclaration.java,v 1.4 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PackageBodyDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PACKAGE_BODY_DECLARATION).
     * @return <code>IR_Kind.IR_PACKAGE_BODY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PACKAGE_BODY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_PackageBodyDeclaration() { }
    
    //METHODS:  
    /** The associate method pairs a named package body declaration up
     *  with the corresponding package declaration. */
    public void associate() {
	throw new Error("unimplemented"); // FIXME!
    }
 
    //MEMBERS:  

// PROTECTED:
} // END class

