// IIR_Statement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Statement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Statement.java,v 1.5 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Statement extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_label(IIR_Label label)
    { _label = label; }
 
    public IIR_Label get_label()
    { return _label; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_Label _label;
} // END class

