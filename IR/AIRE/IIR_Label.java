// IIR_Label.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Label</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Label.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Label extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LABEL
    //CONSTRUCTOR:
    public IIR_Label() { }
    //METHODS:  
    public void set_statement(IIR_SequentialStatement statement)
    { _statement = statement; }
 
    public IIR_Statement get_statement()
    { return _statement; }
 
    //MEMBERS:  
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_SequentialStatement _statement;
} // END class

