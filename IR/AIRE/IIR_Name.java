// IIR_Name.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_Name</code> class represents the general class
 * of referents to explicitly or implicitly declared named entities.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Name.java,v 1.4 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Name extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    //METHODS:  
    static IIR_Declaration[] lookup(IIR_TextLiteral identifier) {
	throw new Error("unimplemented."); // FIXME!
    }
 
    public void set_prefix(IIR prefix)
    { _prefix = prefix; }
 
    public IIR get_prefix()
    { return _prefix; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _prefix;
} // END class

