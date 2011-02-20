// IIR_LibraryUnit.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryUnit</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryUnit.java,v 1.6 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_LibraryUnit extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

