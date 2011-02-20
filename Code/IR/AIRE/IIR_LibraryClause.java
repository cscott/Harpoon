// IIR_LibraryClause.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryClause</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryClause.java,v 1.4 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LibraryClause extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_LIBRARY_CLAUSE).
     * @return <code>IR_Kind.IR_LIBRARY_CLAUSE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_LIBRARY_CLAUSE; }
    //CONSTRUCTOR:
    public IIR_LibraryClause() { }
    //METHODS:  
    public void set_logical_name(IIR_LibraryDeclaration logical_name)
    { _logical_name = logical_name; }
 
    public IIR_LibraryDeclaration get_logical_name()
    { return _logical_name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_LibraryDeclaration _logical_name;
} // END class

