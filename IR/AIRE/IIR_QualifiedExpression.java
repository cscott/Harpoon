// IIR_QualifiedExpression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_QualifiedExpression</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_QualifiedExpression.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_QualifiedExpression extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_QUALIFIED_EXPRESSION; }
    //CONSTRUCTOR:
    public IIR_QualifiedExpression() { }
    //METHODS:  
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _type_mark;
    IIR _expression;
} // END class

