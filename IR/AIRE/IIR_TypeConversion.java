// IIR_TypeConversion.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_TypeConversion</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TypeConversion.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TypeConversion extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_TYPE_CONVERSION
    //CONSTRUCTOR:
    public IIR_TypeConversion() { }

    //METHODS:  
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _type_mark;
} // END class

