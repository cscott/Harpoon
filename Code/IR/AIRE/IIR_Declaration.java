// IIR_Declaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Declaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Declaration.java,v 1.5 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Declaration extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_declarator(IIR_TextLiteral identifier)
    { _declarator = identifier; }
 
    public IIR_TextLiteral get_declarator()
    { return _declarator; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TextLiteral _declarator;
} // END class

