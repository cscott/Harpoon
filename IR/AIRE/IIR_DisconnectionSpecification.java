// IIR_DisconnectionSpecification.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DisconnectionSpecification</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DisconnectionSpecification.java,v 1.2 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DisconnectionSpecification extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DISCONNECTION_SPECIFICATION
    //CONSTRUCTOR:
    public IIR_DisconnectionSpecification() { }
    //METHODS:  
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    public void set_time_expression(IIR time_expression)
    { _time_expression = time_expression; }
 
    public IIR get_time_expression()
    { return _time_expression; }
 
    //MEMBERS:  
    public IIR_DesignatorList guarded_signal_list;

// PROTECTED:
    IIR_TypeDefinition _type_mark;
    IIR _time_expression;
} // END class

