// IIR_BlockStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BlockStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BlockStatement.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BlockStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_BLOCK_STATEMENT
    //CONSTRUCTOR:
    public IIR_BlockStatement() { }
    //METHODS:  
    public void set_guard_expression(IIR guard_expression)
    { _guard_expression = guard_expression; }
 
    //MEMBERS:  
    IIR_GenericList generic_clause;
    IIR_AssociationList generic_map_aspect;
    IIR_PortList port_clause;
    IIR_AssociationList port_map_aspect;
    IIR_DeclarationList block_declarative_part;
    IIR_ConcurrentStatementList block_statement_part;

// PROTECTED:
    IIR _guard_expression;
} // END class

