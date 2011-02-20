// IIR_Literal.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Literal</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Literal.java,v 1.5 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Literal extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

