// IIR_TerminalInterfaceDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_TerminalInterfaceDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TerminalInterfaceDeclaration.java,v 1.4 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TerminalInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_TERMINAL_INTERFACE_DECLARATION).
     * @return <code>IR_Kind.IR_TERMINAL_INTERFACE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_TERMINAL_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_TerminalInterfaceDeclaration() { }
    //METHODS:  
    public IIR_NatureDefinition get_subnature_indication()
    { return _subnature_indication; }
    public void set_subnature_indication(IIR_NatureDefinition subnature)
    { _subnature_indication = subnature; }

    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
} // END class

