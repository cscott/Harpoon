// IIR_FileDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FileDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FileDeclaration.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FileDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

