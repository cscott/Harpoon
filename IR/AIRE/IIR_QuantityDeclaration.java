// IIR_QuantityDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_QuantityDeclaration</code> class.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_QuantityDeclaration.java,v 1.4 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_QuantityDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

