// IIR_FileTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FileTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FileTypeDefinition.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FileTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_FILE_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_FileTypeDefinition() { }
    //METHODS:  
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _type_mark;
} // END class

