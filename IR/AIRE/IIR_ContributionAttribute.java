// IIR_ContributionAttribute.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ContributionAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ContributionAttribute.java,v 1.4 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ContributionAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONTRIBUTION_ATTRIBUTE).
     * @return <code>IR_Kind.IR_CONTRIBUTION_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONTRIBUTION_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ContributionAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

