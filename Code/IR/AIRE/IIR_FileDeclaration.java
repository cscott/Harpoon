// IIR_FileDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_FileDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FileDeclaration.java,v 1.4 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FileDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FILE_DECLARATION).
     * @return <code>IR_Kind.IR_FILE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FILE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_FileDeclaration() { }
    //METHODS:  
    public void set_file_open_expression(IIR file_open_expression)
    { _file_open_expression = file_open_expression; }
 
    public IIR get_file_open_expression()
    { return _file_open_expression; }
 
    public void set_file_logical_name(IIR file_logical_name)
    { _file_logical_name = file_logical_name; }
 
    public IIR get_file_logical_name()
    { return _file_logical_name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _file_open_expression;
    IIR _file_logical_name;
} // END class

